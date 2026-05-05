package com.wch.file;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("file-service")
public interface FileClient {
}
