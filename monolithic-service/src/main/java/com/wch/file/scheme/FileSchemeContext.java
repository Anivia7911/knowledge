package com.wch.file.scheme;

import com.wch.file.scheme.strategy.local.FileSchemeLocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 14:54
 */
@Component
public class FileSchemeContext {

    private final FileSchemeStrategy localService;
    private final Map<String, FileSchemeStrategy> map = new ConcurrentHashMap<String, FileSchemeStrategy>();

    @Autowired
    public FileSchemeContext(List<FileSchemeStrategy> list, FileSchemeLocalService localService) {
        list.forEach(v -> map.put(v.getClass().getName(), v));
        this.localService = localService;
    }

    public FileSchemeStrategy getFileSchemeStrategy(String scheme) {
        return map.getOrDefault(scheme, localService);
    }
}
