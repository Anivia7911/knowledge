package com.wch.common.model.po;

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
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    @TableLogic
    private Integer deleted;
}
