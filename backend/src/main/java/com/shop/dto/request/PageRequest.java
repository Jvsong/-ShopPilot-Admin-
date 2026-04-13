package com.shop.dto.request;

import lombok.Data;

/**
 * 分页请求基类
 */
@Data
public class PageRequest {

    /**
     * 当前页码，默认1
     */
    private Integer page = 1;

    /**
     * 每页大小，默认10
     */
    private Integer size = 10;

    /**
     * 获取偏移量
     */
    public Integer getOffset() {
        return (page - 1) * size;
    }
}