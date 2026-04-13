package com.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 商品属性请求DTO
 */
@Data
@Schema(description = "商品属性请求")
public class ProductAttributeRequest {

    @Schema(description = "属性名称", required = true, example = "颜色")
    @NotBlank(message = "属性名称不能为空")
    private String attributeName;

    @Schema(description = "属性值", required = true, example = "红色")
    @NotBlank(message = "属性值不能为空")
    private String attributeValue;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder = 0;
}