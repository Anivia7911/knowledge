package com.wch.rag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wch.rag.mapper.ChatConversationMapper;
import com.wch.rag.model.po.ChatConversationPO;
import org.springframework.stereotype.Service;

@Service
public class ChatConversationService extends ServiceImpl<ChatConversationMapper, ChatConversationPO> implements IService<ChatConversationPO> {
}
