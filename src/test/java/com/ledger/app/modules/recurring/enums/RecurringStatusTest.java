package com.ledger.app.modules.recurring.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 周期账单状态枚举单元测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@DisplayName("RecurringStatus 枚举测试")
class RecurringStatusTest {

    @Test
    @DisplayName("枚举值 - ACTIVE")
    void testActive() {
        assertEquals(1, RecurringStatus.ACTIVE.getCode());
        assertEquals("执行中", RecurringStatus.ACTIVE.getName());
    }

    @Test
    @DisplayName("枚举值 - PAUSED")
    void testPaused() {
        assertEquals(2, RecurringStatus.PAUSED.getCode());
        assertEquals("已暂停", RecurringStatus.PAUSED.getName());
    }

    @Test
    @DisplayName("枚举值 - COMPLETED")
    void testCompleted() {
        assertEquals(3, RecurringStatus.COMPLETED.getCode());
        assertEquals("已完成", RecurringStatus.COMPLETED.getName());
    }

    @Test
    @DisplayName("枚举值 - CANCELLED")
    void testCancelled() {
        assertEquals(4, RecurringStatus.CANCELLED.getCode());
        assertEquals("已取消", RecurringStatus.CANCELLED.getName());
    }

    @Test
    @DisplayName("fromCode - 有效值")
    void fromCode_Valid() {
        assertEquals(RecurringStatus.ACTIVE, RecurringStatus.fromCode(1));
        assertEquals(RecurringStatus.PAUSED, RecurringStatus.fromCode(2));
        assertEquals(RecurringStatus.COMPLETED, RecurringStatus.fromCode(3));
        assertEquals(RecurringStatus.CANCELLED, RecurringStatus.fromCode(4));
    }

    @Test
    @DisplayName("fromCode - 无效值")
    void fromCode_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecurringStatus.fromCode(99);
        });
    }
}
