package com.wch.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wch.file.mapper.FileBodyMapper;
import com.wch.file.model.po.FileBodyPO;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 3:16
 */
@Service
public class FileBodyService extends ServiceImpl<FileBodyMapper, FileBodyPO> implements IService<FileBodyPO> {

}