package com.wch.common.model.req;

import lombok.Data;

/**
 * @author: Jie Bugui
 * @create: 2026-05-01 2:04
 */
@Data
public class PageQuery {
    private Long page = 1L;

    private Long rows = 10L;
}
