package com.shop.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 分页响应格式
 */
@Data
public class PageResponse<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 创建分页响应
     */
    public static <T> PageResponse<T> of(List<T> list, Long total, Integer page, Integer size) {
        PageResponse<T> response = new PageResponse<>();
        response.setList(list);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        response.setPages((int) Math.ceil((double) total / size));
        return response;
    }
}