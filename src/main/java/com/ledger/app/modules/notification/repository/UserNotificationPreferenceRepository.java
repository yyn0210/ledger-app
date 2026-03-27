package com.ledger.app.modules.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.app.modules.notification.entity.UserNotificationPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户通知偏好 Mapper
 */
@Mapper
public interface UserNotificationPreferenceRepository extends BaseMapper<UserNotificationPreference> {
}
