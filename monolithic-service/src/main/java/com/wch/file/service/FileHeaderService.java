package com.wch.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wch.file.mapper.FileHeaderMapper;
import com.wch.file.model.po.FileHeaderPO;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Bugui
 * @create: 2026-04-22 18:00
 */
@Service
public class FileHeaderService extends ServiceImpl<FileHeaderMapper, FileHeaderPO> implements IService<FileHeaderPO>{

}
