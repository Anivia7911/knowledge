package com.wch.file.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wch.common.model.po.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 16:48
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_header")
public class FileHeaderPO extends BasePO {
    private String name;
}
