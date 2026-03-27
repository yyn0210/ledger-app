package com.ledger.app.modules.export.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.app.modules.export.entity.ExportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 导出记录 Mapper
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Mapper
public interface ExportRecordRepository extends BaseMapper<ExportRecord> {

    /**
     * 查询用户的导出记录列表
     */
    @Select("SELECT * FROM export_record WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<ExportRecord> findByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 查询用户的导出记录列表（按账本筛选）
     */
    @Select("SELECT * FROM export_record WHERE user_id = #{userId} AND book_id = #{bookId} AND deleted = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<ExportRecord> findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("limit") Integer limit);
}
