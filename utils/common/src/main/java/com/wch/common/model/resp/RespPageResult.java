package com.wch.common.model.resp;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author: Jie Bugui
 * @create: 2026-05-01 1:04
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RespPageResult<T> implements Serializable {

    private Integer code;

    private String message;

    private List<T> list;

    private Long page;

    private Long rows;

    private Long total;

    private Long totalPage;

    public RespPageResult(List<T> list, Long page, Long rows, Long total, RespCode respCode) {
        this.list = list;
        this.page = page;
        this.rows = rows;
        this.total = total;
        this.code = respCode.getCode();
        this.message = respCode.getMessage();
    }

    public static <T> RespPageResult<T> success(Page<T> page) {
        RespPageResult<T> result = new RespPageResult<>();
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            result.setList(page.getRecords());
        }
        result.setTotalPage(page.getPages());
        result.setPage(page.getCurrent());
        result.setRows(page.getSize());
        result.setTotal(page.getTotal());
        result.setCode(RespCode.SUCCESS.getCode());
        result.setMessage(RespCode.SUCCESS.getMessage());
        return result;
    }

    public static <T> RespPageResult<T> success(List<T> list) {
        long count = list != null && !list.isEmpty() ? (long) list.size() : 0L;
        return new RespPageResult<>(list, 0L, count, count, RespCode.SUCCESS);
    }

    public static <T> RespPageResult<T> success(List<T> list, Long page, Long rows, Long total) {
        return new RespPageResult<>(list, page, rows, total, RespCode.SUCCESS);
    }

    public static <T> RespPageResult<T> error() {
        return new RespPageResult<>(null, null, null, null, RespCode.ERROR);
    }

    public static <T> RespPageResult<T> fail() {
        return new RespPageResult<>(null, null, null, null, RespCode.BAD_REQUEST);
    }

}
