package com.wch.rag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wch.rag.mapper.ChatMessageMapper;
import com.wch.rag.model.po.ChatMessagePO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessagePO> implements IService<ChatMessagePO> {

    public List<ChatMessagePO> listByConversationId(Long conversationId) {
        LambdaQueryWrapper<ChatMessagePO> wrapper = new LambdaQueryWrapper<>();
        // 按雪花ID升序保证消息顺序稳定（createDate为秒级精度，同秒内顺序不可靠）
        wrapper.eq(ChatMessagePO::getConversationId, conversationId)
                .orderByAsc(ChatMessagePO::getId);
        return list(wrapper);
    }
}
