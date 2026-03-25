package com.ledger.app.common.result;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
@Builder
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页条数
     */
    private Long pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    public PageResult() {
    }

    public PageResult(Long pageNum, Long pageSize, Long total, Long pages, List<T> list, Boolean hasPrevious, Boolean hasNext) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pages;
        this.list = list;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
    }

    /**
     * 从 MyBatis Plus Page 构建 PageResult
     */
    public static <T> PageResult<T> fromPage(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return PageResult.<T>builder()
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .list(page.getRecords())
                .hasPrevious(page.getCurrent() > 1)
                .hasNext(page.getCurrent() < page.getPages())
                .build();
    }
}
