package com.wch.rag.service;

import com.wch.rag.config.RagConfig;
import com.wch.rag.model.po.AiModelPO;
import com.wch.rag.model.po.ChatConversationPO;
import com.wch.rag.model.po.ChatMessagePO;
import com.wch.rag.model.req.ChatRequest;
import com.wch.rag.model.req.ChatResponse;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiChatService {

    private final ChatModel defaultChatModel;
    private final AiModelService aiModelService;
    private final ChatConversationService conversationService;
    private final ChatMessageService messageService;
    private final RagVectorService ragVectorService;

    @Autowired
    public AiChatService(
            @Nullable ChatModel defaultChatModel,
            AiModelService aiModelService,
            ChatConversationService conversationService,
            ChatMessageService messageService,
            RagVectorService ragVectorService
    ) {
        this.defaultChatModel = defaultChatModel;
        this.aiModelService = aiModelService;
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.ragVectorService = ragVectorService;
    }

    /**
     * 发送对话消息
     */
    @Transactional(rollbackFor = Throwable.class)
    public ChatResponse chat(ChatRequest request) {
        // 获取或创建会话
        ChatConversationPO conversation;
        if (request.getConversationId() != null) {
            conversation = conversationService.getById(request.getConversationId());
            if (conversation == null) {
                throw new RuntimeException("会话不存在");
            }
        } else {
            conversation = new ChatConversationPO();
            conversation.setTitle(truncateTitle(request.getMessage()));
            conversation.setModelId(request.getModelId());
            conversationService.save(conversation);
        }

        // 持久化本次使用的知识库选择，供下次打开该会话时恢复（跨浏览器生效）
        String kbCsv = toKbCsv(request.getKnowledgeBaseIds());
        conversationService.lambdaUpdate()
                .eq(ChatConversationPO::getId, conversation.getId())
                .set(ChatConversationPO::getKnowledgeBaseIds, kbCsv)
                .update();
        conversation.setKnowledgeBaseIds(kbCsv);

        // 保存用户消息
        ChatMessagePO userMsg = new ChatMessagePO();
        userMsg.setConversationId(conversation.getId());
        userMsg.setRole("user");
        userMsg.setContent(request.getMessage());
        messageService.save(userMsg);

        // 获取 ChatModel
        ChatModel chatModel = resolveChatModel(request.getModelId());

        // 构建消息列表（包含历史上下文与RAG检索内容）
        List<dev.langchain4j.data.message.ChatMessage> messages = buildMessages(conversation.getId(), request.getMessage(), request.getKnowledgeBaseIds());

        // 调用 AI 模型
        String reply;
        try {
            var chatResponse = chatModel.chat(messages);
            reply = chatResponse.aiMessage().text();
        } catch (Exception e) {
            log.error("AI模型调用失败: {}", e.getMessage(), e);
            reply = "抱歉，AI模型调用失败，请稍后重试。错误信息: " + e.getMessage();
        }

        // 保存 AI 回复
        ChatMessagePO aiMsg = new ChatMessagePO();
        aiMsg.setConversationId(conversation.getId());
        aiMsg.setRole("assistant");
        aiMsg.setContent(reply);
        messageService.save(aiMsg);

        return new ChatResponse(conversation.getId(), reply);
    }

    /**
     * 获取会话历史消息
     */
    public List<ChatMessagePO> getHistory(Long conversationId) {
        return messageService.listByConversationId(conversationId);
    }

    /**
     * 删除会话
     */
    @Transactional(rollbackFor = Throwable.class)
    public void deleteConversation(Long conversationId) {
        conversationService.removeById(conversationId);
        messageService.lambdaUpdate()
                .eq(ChatMessagePO::getConversationId, conversationId)
                .remove();
    }

    /**
     * 获取会话列表
     */
    public List<ChatConversationPO> listConversations() {
        // 按雪花ID降序保证顺序稳定（updateDate为秒级精度，同秒内顺序不可靠）
        return conversationService.lambdaQuery()
                .orderByDesc(ChatConversationPO::getId)
                .list();
    }

    private ChatModel resolveChatModel(Long modelId) {
        if (modelId != null) {
            AiModelPO model = aiModelService.getById(modelId);
            if (model != null && Boolean.TRUE.equals(model.getEnabled())) {
                return RagConfig.buildChatModel(model);
            }
        }
        // 实时从数据库加载默认模型，保证后台新增/编辑模型配置后立即生效
        AiModelPO model = aiModelService.lambdaQuery()
                .eq(AiModelPO::getEnabled, true)
                .eq(AiModelPO::getDefaultModel, true)
                .eq(AiModelPO::getDeleted, 0)
                .list()
                .stream()
                .findFirst()
                .orElse(null);
        if (model != null) {
            return RagConfig.buildChatModel(model);
        }
        if (defaultChatModel != null) {
            return defaultChatModel;
        }
        throw new RuntimeException("未配置可用的AI模型，请先在模型管理中添加并启用默认模型");
    }

    private List<dev.langchain4j.data.message.ChatMessage> buildMessages(Long conversationId, String currentMessage, List<Long> knowledgeBaseIds) {
        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();

        // 系统提示（选择知识库时启用RAG检索增强）
        messages.add(new SystemMessage(buildSystemPrompt(currentMessage, knowledgeBaseIds)));

        // 历史消息（最近10条，不含当前消息）
        List<ChatMessagePO> history = messageService.listByConversationId(conversationId);
        int start = Math.max(0, history.size() - 10);
        for (int i = start; i < history.size() - 1; i++) {
            ChatMessagePO msg = history.get(i);
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AiMessage(msg.getContent()));
            }
        }

        // 当前用户消息
        messages.add(new UserMessage(currentMessage));
        return messages;
    }

    private String truncateTitle(String message) {
        if (message == null) return "新对话";
        return message.length() > 30 ? message.substring(0, 30) + "..." : message;
    }

    /**
     * 知识库ID列表转逗号分隔字符串；未选择时返回空字符串（明确清空选择也会持久化）
     */
    private String toKbCsv(List<Long> knowledgeBaseIds) {
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            return "";
        }
        return knowledgeBaseIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * 构建系统提示：选择了知识库时，先检索相关分片作为参考资料（RAG）
     */
    private String buildSystemPrompt(String question, List<Long> knowledgeBaseIds) {
        String base = "你是一个知识库助手，请根据用户的问题提供准确、有帮助的回答。";
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            return base;
        }
        try {
            List<RagVectorService.Chunk> chunks = ragVectorService.search(knowledgeBaseIds, question, 5);
            if (chunks.isEmpty()) {
                return base + "\n当前知识库中没有检索到与问题相关的内容，请如实告知用户。";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("你是一个知识库助手。请优先依据下面的参考资料回答用户问题；如果参考资料中没有相关内容，请如实告知。回答时不要提及“参考资料”的存在。\n\n参考资料：\n");
            for (int i = 0; i < chunks.size(); i++) {
                RagVectorService.Chunk chunk = chunks.get(i);
                sb.append("[").append(i + 1).append("]");
                if (chunk.getFileName() != null && !chunk.getFileName().isBlank()) {
                    sb.append("（来源: ").append(chunk.getFileName()).append("）");
                }
                sb.append(" ").append(chunk.getText()).append("\n\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("RAG检索失败，降级为普通对话: {}", e.getMessage());
            return base;
        }
    }
}
