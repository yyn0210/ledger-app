package com.ledger.app.modules.recurring.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 周期类型枚举单元测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@DisplayName("RecurringType 枚举测试")
class RecurringTypeTest {

    @Test
    @DisplayName("枚举值 - DAILY")
    void testDaily() {
        assertEquals(1, RecurringType.DAILY.getCode());
        assertEquals("每日", RecurringType.DAILY.getName());
        assertEquals(86400, RecurringType.DAILY.getIntervalSeconds());
    }

    @Test
    @DisplayName("枚举值 - WEEKLY")
    void testWeekly() {
        assertEquals(2, RecurringType.WEEKLY.getCode());
        assertEquals("每周", RecurringType.WEEKLY.getName());
        assertEquals(604800, RecurringType.WEEKLY.getIntervalSeconds());
    }

    @Test
    @DisplayName("枚举值 - BIWEEKLY")
    void testBiWeekly() {
        assertEquals(3, RecurringType.BIWEEKLY.getCode());
        assertEquals("每两周", RecurringType.BIWEEKLY.getName());
        assertEquals(1209600, RecurringType.BIWEEKLY.getIntervalSeconds());
    }

    @Test
    @DisplayName("枚举值 - MONTHLY")
    void testMonthly() {
        assertEquals(4, RecurringType.MONTHLY.getCode());
        assertEquals("每月", RecurringType.MONTHLY.getName());
        assertEquals(2592000, RecurringType.MONTHLY.getIntervalSeconds());
    }

    @Test
    @DisplayName("枚举值 - QUARTERLY")
    void testQuarterly() {
        assertEquals(5, RecurringType.QUARTERLY.getCode());
        assertEquals("每季度", RecurringType.QUARTERLY.getName());
        assertEquals(7776000, RecurringType.QUARTERLY.getIntervalSeconds());
    }

    @Test
    @DisplayName("枚举值 - YEARLY")
    void testYearly() {
        assertEquals(6, RecurringType.YEARLY.getCode());
        assertEquals("每年", RecurringType.YEARLY.getName());
        assertEquals(31536000, RecurringType.YEARLY.getIntervalSeconds());
    }

    @Test
    @DisplayName("fromCode - 有效值")
    void fromCode_Valid() {
        assertEquals(RecurringType.DAILY, RecurringType.fromCode(1));
        assertEquals(RecurringType.WEEKLY, RecurringType.fromCode(2));
        assertEquals(RecurringType.MONTHLY, RecurringType.fromCode(4));
    }

    @Test
    @DisplayName("fromCode - 无效值")
    void fromCode_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecurringType.fromCode(99);
        });
    }

    @Test
    @DisplayName("fromName - 有效值")
    void fromName_Valid() {
        assertEquals(RecurringType.DAILY, RecurringType.fromName("每日"));
        assertEquals(RecurringType.WEEKLY, RecurringType.fromName("每周"));
    }

    @Test
    @DisplayName("fromName - 无效值")
    void fromName_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecurringType.fromName("无效类型");
        });
    }
}
