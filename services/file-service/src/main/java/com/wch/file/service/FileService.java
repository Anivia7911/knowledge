package com.wch.file.service;

import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 4:06
 */
@Service
public class FileService {
    private Container container;

//    @Autowired
//    void setBean(
//            Container container
//    ) {
//        this.container = container;
//    }

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
