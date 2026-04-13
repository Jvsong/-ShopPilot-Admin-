package com.shop.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 查询条件构建工具类
 * 用于简化LambdaQueryWrapper的构建，减少重复的条件判断代码
 */
public class QueryBuilder {

    private QueryBuilder() {
        // 工具类，禁止实例化
    }

    /**
     * 构建字符串模糊查询条件
     *
     * @param wrapper LambdaQueryWrapper
     * @param value   字符串值
     * @param column  实体列引用
     * @param <T>     实体类型
     * @param <R>     列类型
     */
    public static <T> void likeIfPresent(LambdaQueryWrapper<T> wrapper, String value, SFunction<T, ?> column) {
        if (StringUtils.hasText(value)) {
            wrapper.like(column, value);
        }
    }

    /**
     * 构建字符串精确查询条件
     *
     * @param wrapper LambdaQueryWrapper
     * @param value   字符串值
     * @param column  实体列引用
     * @param <T>     实体类型
     * @param <R>     列类型
     */
    public static <T> void eqIfPresent(LambdaQueryWrapper<T> wrapper, String value, SFunction<T, ?> column) {
        if (StringUtils.hasText(value)) {
            wrapper.eq(column, value);
        }
    }

    /**
     * 构建数值相等查询条件
     *
     * @param wrapper LambdaQueryWrapper
     * @param value   数值
     * @param column  实体列引用
     * @param <T>     实体类型
     * @param <R>     列类型
     */
    public static <T> void eqIfNotNull(LambdaQueryWrapper<T> wrapper, Object value, SFunction<T, ?> column) {
        if (value != null) {
            wrapper.eq(column, value);
        }
    }

    /**
     * 构建大于等于查询条件（用于时间范围）
     *
     * @param wrapper LambdaQueryWrapper
     * @param value   时间值
     * @param column  实体列引用
     * @param <T>     实体类型
     */
    public static <T> void geIfNotNull(LambdaQueryWrapper<T> wrapper, LocalDateTime value, SFunction<T, ?> column) {
        if (value != null) {
            wrapper.ge(column, value);
        }
    }

    /**
     * 构建小于等于查询条件（用于时间范围）
     *
     * @param wrapper LambdaQueryWrapper
     * @param value   时间值
     * @param column  实体列引用
     * @param <T>     实体类型
     */
    public static <T> void leIfNotNull(LambdaQueryWrapper<T> wrapper, LocalDateTime value, SFunction<T, ?> column) {
        if (value != null) {
            wrapper.le(column, value);
        }
    }

    /**
     * 构建大于等于查询条件（用于数值范围）
     *
     * @param wrapper LambdaQueryWrapper
     * @param value   数值
     * @param column  实体列引用
     * @param <T>     实体类型
     * @param <R>     列类型
     */
    public static <T> void geIfNotNull(LambdaQueryWrapper<T> wrapper, Comparable<?> value, SFunction<T, ?> column) {
        if (value != null) {
            wrapper.ge(column, value);
        }
    }

    /**
     * 构建小于等于查询条件（用于数值范围）
     *
     * @param wrapper LambdaQueryWrapper
     * @param value   数值
     * @param column  实体列引用
     * @param <T>     实体类型
     * @param <R>     列类型
     */
    public static <T> void leIfNotNull(LambdaQueryWrapper<T> wrapper, Comparable<?> value, SFunction<T, ?> column) {
        if (value != null) {
            wrapper.le(column, value);
        }
    }

    /**
     * 构建排序条件
     *
     * @param wrapper        LambdaQueryWrapper
     * @param sortBy         排序字段
     * @param sortDirection  排序方向
     * @param defaultColumn  默认排序列
     * @param sortMappings   排序字段映射（字段名 -> 列引用）
     * @param <T>            实体类型
     */
    @SafeVarargs
    public static <T> void applySort(LambdaQueryWrapper<T> wrapper,
                                     String sortBy,
                                     String sortDirection,
                                     SFunction<T, ?> defaultColumn,
                                     java.util.Map.Entry<String, SFunction<T, ?>>... sortMappings) {
        boolean asc = "asc".equalsIgnoreCase(sortDirection);

        if (StringUtils.hasText(sortBy)) {
            java.util.Map<String, SFunction<T, ?>> mapping = new java.util.HashMap<>();
            for (java.util.Map.Entry<String, SFunction<T, ?>> entry : sortMappings) {
                mapping.put(entry.getKey(), entry.getValue());
            }

            SFunction<T, ?> column = mapping.get(sortBy);
            if (column != null) {
                wrapper.orderBy(true, asc, column);
            } else {
                // 默认排序
                wrapper.orderBy(true, asc, defaultColumn);
            }
        } else {
            // 默认降序排序
            wrapper.orderByDesc(defaultColumn);
        }
    }

    /**
     * 构建软删除条件（默认查询未删除的记录）
     *
     * @param wrapper LambdaQueryWrapper
     * @param column  软删除列引用
     * @param <T>     实体类型
     * @param <R>     列类型
     */
    public static <T> void notDeleted(LambdaQueryWrapper<T> wrapper, SFunction<T, ?> column) {
        wrapper.eq(column, 0);
    }

    /**
     * 构建软删除条件（可指定是否包含已删除记录）
     *
     * @param wrapper         LambdaQueryWrapper
     * @param column          软删除列引用
     * @param includeDeleted  是否包含已删除记录
     * @param <T>             实体类型
     * @param <R>             列类型
     */
    public static <T> void deletedCondition(LambdaQueryWrapper<T> wrapper, SFunction<T, ?> column, Boolean includeDeleted) {
        if (includeDeleted == null || !includeDeleted) {
            wrapper.eq(column, 0);
        }
    }

    /**
     * 链式构建器模式
     */
    public static class Builder<T> {
        private final LambdaQueryWrapper<T> wrapper;

        private Builder(LambdaQueryWrapper<T> wrapper) {
            this.wrapper = wrapper;
        }

        public static <T> Builder<T> of(LambdaQueryWrapper<T> wrapper) {
            return new Builder<>(wrapper);
        }

        public Builder<T> likeIfPresent(String value, SFunction<T, ?> column) {
            QueryBuilder.likeIfPresent(wrapper, value, column);
            return this;
        }

        public Builder<T> eqIfPresent(String value, SFunction<T, ?> column) {
            QueryBuilder.eqIfPresent(wrapper, value, column);
            return this;
        }

        public Builder<T> eqIfNotNull(Object value, SFunction<T, ?> column) {
            QueryBuilder.eqIfNotNull(wrapper, value, column);
            return this;
        }

        public Builder<T> geIfNotNull(LocalDateTime value, SFunction<T, ?> column) {
            QueryBuilder.geIfNotNull(wrapper, value, column);
            return this;
        }

        public Builder<T> leIfNotNull(LocalDateTime value, SFunction<T, ?> column) {
            QueryBuilder.leIfNotNull(wrapper, value, column);
            return this;
        }

        public Builder<T> geIfNotNull(Comparable<?> value, SFunction<T, ?> column) {
            QueryBuilder.geIfNotNull(wrapper, value, column);
            return this;
        }

        public Builder<T> leIfNotNull(Comparable<?> value, SFunction<T, ?> column) {
            QueryBuilder.leIfNotNull(wrapper, value, column);
            return this;
        }

        public Builder<T> notDeleted(SFunction<T, ?> column) {
            QueryBuilder.notDeleted(wrapper, column);
            return this;
        }

        public Builder<T> build(Consumer<LambdaQueryWrapper<T>> customizer) {
            if (customizer != null) {
                customizer.accept(wrapper);
            }
            return this;
        }

        public LambdaQueryWrapper<T> getWrapper() {
            return wrapper;
        }
    }
}