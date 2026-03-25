package com.ledger.app.modules.book.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.app.modules.book.entity.BookMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 账本成员 Mapper
 */
@Mapper
public interface BookMemberRepository extends BaseMapper<BookMember> {

    /**
     * 查询账本的所有成员
     */
    @Select("SELECT * FROM book_members WHERE book_id = #{bookId} AND deleted = 0 ORDER BY joined_at ASC")
    List<BookMember> selectByBookId(Long bookId);

    /**
     * 查询用户在账本中的角色
     */
    @Select("SELECT role FROM book_members WHERE book_id = #{bookId} AND user_id = #{userId} AND deleted = 0")
    Integer selectRoleByBookIdAndUserId(Long bookId, Long userId);

    /**
     * 查询用户是否已是成员
     */
    @Select("SELECT COUNT(*) FROM book_members WHERE book_id = #{bookId} AND user_id = #{userId} AND deleted = 0")
    int countByBookIdAndUserId(Long bookId, Long userId);
}
