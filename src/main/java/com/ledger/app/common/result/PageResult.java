package com.ledger.app.common.result;

<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

=======
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
import java.util.List;

/**
 * 分页响应结果
 */
@Data
@Builder
<<<<<<< HEAD
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
=======
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
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1

    /**
     * 数据列表
     */
    private List<T> list;

    /**
<<<<<<< HEAD
     * 总数
     */
    private Long total;

    public void setTotal(Long total) {
        this.total = total;
    }

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer size;
=======
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
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
}
