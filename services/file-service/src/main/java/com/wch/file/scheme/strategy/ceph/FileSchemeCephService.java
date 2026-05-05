package com.wch.file.scheme.strategy.ceph;

import com.wch.file.scheme.FileSchemeStrategy;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: Jie Bugui
 * @create: 2026-04-29 15:00
 */
@Service
public class FileSchemeCephService implements FileSchemeStrategy {

    private Container container;

//    @Autowired
//    void setService(
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

    @Override
    public void saveChunk(MultipartFile file, String md5, Integer chunk, Integer chunks, String name) throws Exception {

    }
}
