package com.ledger.app.modules.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.app.modules.notification.entity.Notification;
import com.ledger.app.modules.notification.entity.NotificationTemplate;
import com.ledger.app.modules.notification.entity.UserNotificationPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 通知 Mapper
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Mapper
public interface NotificationRepository extends BaseMapper<Notification> {

    /**
     * 查询用户的通知列表
     */
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<Notification> findByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 查询用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND status != 4 AND deleted = 0")
    Integer countUnread(@Param("userId") Long userId);

    /**
     * 根据编码查询模板
     */
    @Select("SELECT * FROM notification_template WHERE code = #{code} AND deleted = 0 AND is_enabled = 1")
    NotificationTemplate findByCode(@Param("code") String code);

    /**
     * 查询模板列表
     */
    @Select("SELECT * FROM notification_template WHERE deleted = 0 ORDER BY created_at DESC")
    List<NotificationTemplate> findAll();

    /**
     * 查询用户通知偏好
     */
    @Select("SELECT * FROM user_notification_preference WHERE user_id = #{userId} AND book_id = #{bookId} AND deleted = 0 LIMIT 1")
    UserNotificationPreference findPreference(@Param("userId") Long userId, @Param("bookId") Long bookId);
}
