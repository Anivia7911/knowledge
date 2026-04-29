package com.wch.common.model.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: Jie Bugui
 * @create: 2026-04-22 17:56
 */
@Data
public class BasePO implements Serializable {
    private Long id;

    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    private String updateUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDate;

    @TableLogic
    private Integer deleted;
}
