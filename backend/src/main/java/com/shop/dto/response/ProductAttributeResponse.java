package com.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品属性响应DTO
 */
@Data
@Schema(description = "商品属性响应")
public class ProductAttributeResponse {

    @Schema(description = "属性ID", example = "1")
    private Long id;

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "属性名称", example = "颜色")
    private String attributeName;

    @Schema(description = "属性值", example = "红色")
    private String attributeValue;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否显示", example = "true")
    private Boolean show;
}