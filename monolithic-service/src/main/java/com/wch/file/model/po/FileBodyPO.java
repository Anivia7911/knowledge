package com.wch.file.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wch.common.model.po.BasePO;
import com.wch.common.trivial.enumdata.FileSchemeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 16:48
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_body")
public class FileBodyPO extends BasePO {

    /*
     * 大小
     * */
    private long size;

    /*
     * MD5
     * */
    private String md5;

    /*
     * 文件类型
     * */
    private String type;

    /*
     * 文件协议
     * */
    private FileSchemeEnum scheme;

    /*
     * 版本
     * */
    private Integer version;

    /*
     * 文件存储路径
     * */
    private String path;
}
