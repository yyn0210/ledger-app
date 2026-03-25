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
<<<<<<< HEAD
=======

    /**
     * 插入或更新用户通知偏好
     */
    @org.apache.ibatis.annotations.Insert("INSERT INTO user_notification_preference (user_id, book_id, email_enabled, sms_enabled, in_app_enabled, push_enabled, subscribed_types, deleted) VALUES (#{userId}, #{bookId}, #{emailEnabled}, #{smsEnabled}, #{inAppEnabled}, #{pushEnabled}, #{subscribedTypes}, #{deleted})")
    int insertPreference(UserNotificationPreference preference);

    @org.apache.ibatis.annotations.Update("UPDATE user_notification_preference SET email_enabled=#{emailEnabled}, sms_enabled=#{smsEnabled}, in_app_enabled=#{inAppEnabled}, push_enabled=#{pushEnabled} WHERE user_id=#{userId} AND book_id=#{bookId}")
    int updatePreference(UserNotificationPreference preference);

    /**
     * 根据 ID 查询通知模板
     */
    @Select("SELECT * FROM notification_template WHERE id = #{id} AND deleted = 0")
    NotificationTemplate selectTemplateById(Long id);

    /**
     * 插入或更新通知模板
     */
    @org.apache.ibatis.annotations.Insert("INSERT INTO notification_template (name, code, type, content, title_template, biz_type, is_enabled, deleted) VALUES (#{name}, #{code}, #{type}, #{content}, #{titleTemplate}, #{bizType}, #{isEnabled}, #{deleted})")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTemplate(NotificationTemplate template);

    @org.apache.ibatis.annotations.Update("UPDATE notification_template SET name=#{name}, type=#{type}, content=#{content}, title_template=#{titleTemplate}, biz_type=#{bizType}, is_enabled=#{isEnabled} WHERE id=#{id}")
    int updateTemplate(NotificationTemplate template);

    /**
     * 删除通知模板
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM notification_template WHERE id=#{id}")
    int deleteTemplate(Long id);

    /**
     * 批量更新通知
     */
    default int updateBatchById(List<Notification> notifications) {
        int count = 0;
        for (Notification notification : notifications) {
            count += updateById(notification);
        }
        return count;
    }
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
}
