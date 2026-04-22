package com.wch.file.service;

import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 16:17
 */
@Component
public class FileHandleService {

    private Container container;

    @Autowired
    void setBean(
            Container container
    ) {
        this.container = container;
    }

    /**
     * 文件上传
     */
    public void upload(String filename, byte[] buffer) {
        //获取容器
        StoredObject object = container.getObject(filename);

        //文件上传
        object.uploadObject(buffer);
    }


    /**
     * 文件下载
     */
    public byte[] download(String filename) {
        //获取容器
        StoredObject object = container.getObject(filename);
        return object.downloadObject();
    }
}
