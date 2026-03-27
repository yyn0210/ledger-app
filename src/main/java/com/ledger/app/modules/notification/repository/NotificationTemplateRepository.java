package com.ledger.app.modules.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.app.modules.notification.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知模板 Mapper
 */
@Mapper
public interface NotificationTemplateRepository extends BaseMapper<NotificationTemplate> {
}
