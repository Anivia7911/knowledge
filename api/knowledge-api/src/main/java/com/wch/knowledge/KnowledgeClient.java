package com.wch.knowledge;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("knowledge-service")
public interface KnowledgeClient {
}
