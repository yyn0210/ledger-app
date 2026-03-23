package com.ledger.app.modules.auth.repository;

import com.ledger.app.modules.auth.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0")
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    User findByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM users WHERE phone = #{phone} AND deleted = 0")
    User findByPhone(String phone);
}
