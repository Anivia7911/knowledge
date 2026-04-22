package com.wch.file.service;

import com.wch.file.mapper.FileHeaderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Bugui
 * @create: 2026-04-22 18:00
 */
@Service
public class FileHeaderService {
    @Autowired
    private FileHeaderMapper fileHeaderMapper;
}
