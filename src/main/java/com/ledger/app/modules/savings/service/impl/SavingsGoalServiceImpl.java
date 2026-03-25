package com.ledger.app.modules.savings.service.impl;

import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.savings.dto.request.CreateSavingsGoalRequest;
import com.ledger.app.modules.savings.dto.request.UpdateSavingsGoalRequest;
import com.ledger.app.modules.savings.dto.response.SavingsGoalResponse;
import com.ledger.app.modules.savings.entity.SavingsGoal;
import com.ledger.app.modules.savings.enums.SavingsStatus;
import com.ledger.app.modules.savings.repository.SavingsGoalRepository;
import com.ledger.app.modules.savings.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 储蓄目标服务实现
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getSavingsGoals(Long bookId, Long userId, String status) {
        List<SavingsGoal> goals;

        if (status == null) {
            goals = savingsGoalRepository.findByBookIdAndUserId(bookId, userId);
        } else {
            Integer statusCode = parseStatus(status);
            if (statusCode == 1) {
                goals = savingsGoalRepository.findActiveByBookId(bookId, userId);
            } else if (statusCode == 2) {
                goals = savingsGoalRepository.findCompleted(bookId, userId);
            } else {
                goals = savingsGoalRepository.findByBookIdAndUserId(bookId, userId);
            }
        }

        return goals.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsGoalResponse getSavingsGoalById(Long id, Long userId) {
        SavingsGoal goal = savingsGoalRepository.selectById(id);
        if (goal == null || goal.getDeleted() != 0) {
            throw new BusinessException("储蓄目标不存在");
        }
        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该储蓄目标");
        }
        return buildResponse(goal);
    }

    @Override
    @Transactional
    public SavingsGoalResponse createSavingsGoal(Long userId, CreateSavingsGoalRequest request) {
        // 验证目标日期
        if (request.getTargetDate().isBefore(LocalDate.now())) {
            throw new BusinessException("目标日期不能早于今天");
        }

        // 验证目标金额
        if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("目标金额必须大于 0");
        }

        SavingsGoal goal = SavingsGoal.builder()
                .bookId(request.getBookId())
                .userId(userId)
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .savedAmount(BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .monthlyAmount(request.getMonthlyAmount())
                .accountId(request.getAccountId())
                .status(SavingsStatus.ACTIVE.getCode())
                .note(request.getNote())
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        savingsGoalRepository.insert(goal);
        log.info("创建储蓄目标成功：userId={}, goalId={}, name={}", userId, goal.getId(), goal.getName());

        return buildResponse(goal);
    }

    @Override
    @Transactional
    public SavingsGoalResponse updateSavingsGoal(Long id, Long userId, UpdateSavingsGoalRequest request) {
        SavingsGoal goal = getByIdEntity(id, userId);

        if (request.getName() != null) {
            goal.setName(request.getName());
        }
        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getSavedAmount() != null) {
            goal.setSavedAmount(request.getSavedAmount());
            // 检查是否完成
            if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
                goal.setStatus(SavingsStatus.COMPLETED.getCode());
            }
        }
        if (request.getMonthlyAmount() != null) {
            goal.setMonthlyAmount(request.getMonthlyAmount());
        }
        if (request.getNote() != null) {
            goal.setNote(request.getNote());
        }
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }

        goal.setUpdatedAt(LocalDateTime.now());
        savingsGoalRepository.updateById(goal);
        log.info("更新储蓄目标成功：userId={}, goalId={}", userId, id);

        return buildResponse(goal);
    }

    @Override
    @Transactional
    public void deleteSavingsGoal(Long id, Long userId) {
        SavingsGoal goal = getByIdEntity(id, userId);
        savingsGoalRepository.deleteById(id);
        log.info("删除储蓄目标成功：goalId={}", id);
    }

    @Override
    @Transactional
    public SavingsGoalResponse updateProgress(Long id, Long userId, BigDecimal amount) {
        SavingsGoal goal = getByIdEntity(id, userId);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("存入金额不能为负数");
        }

        goal.setSavedAmount(goal.getSavedAmount().add(amount));
        goal.setUpdatedAt(LocalDateTime.now());

        // 检查是否完成
        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsStatus.COMPLETED.getCode());
            log.info("储蓄目标完成：goalId={}, targetAmount={}, savedAmount={}",
                    goal.getId(), goal.getTargetAmount(), goal.getSavedAmount());
        }

        savingsGoalRepository.updateById(goal);
        return buildResponse(goal);
    }

    @Override
    @Transactional
    public void completeSavingsGoal(Long id, Long userId) {
        SavingsGoal goal = getByIdEntity(id, userId);
        goal.setStatus(SavingsStatus.COMPLETED.getCode());
        goal.setUpdatedAt(LocalDateTime.now());
        savingsGoalRepository.updateById(goal);
        log.info("手动完成储蓄目标：goalId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsSummary getSummary(Long bookId, Long userId) {
        BigDecimal totalSaved = savingsGoalRepository.sumTotalSaved(bookId);
        List<SavingsGoal> allGoals = savingsGoalRepository.findByBookIdAndUserId(bookId, userId);

        int totalGoals = allGoals.size();
        int completedGoals = (int) allGoals.stream()
                .filter(g -> g.getStatus() != null && g.getStatus() == 2)
                .count();
        int activeGoals = (int) allGoals.stream()
                .filter(g -> g.getStatus() != null && g.getStatus() == 1)
                .count();

        return new SavingsSummaryImpl(totalSaved, totalGoals, completedGoals, activeGoals);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getExpiringGoals(Long bookId, Long userId) {
        List<SavingsGoal> goals = savingsGoalRepository.findExpiringSoon(bookId, userId);
        return goals.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取储蓄目标实体（带权限验证）
     */
    private SavingsGoal getByIdEntity(Long id, Long userId) {
        SavingsGoal goal = savingsGoalRepository.selectById(id);
        if (goal == null || goal.getDeleted() != 0) {
            throw new BusinessException("储蓄目标不存在");
        }
        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该储蓄目标");
        }
        return goal;
    }

    /**
     * 解析状态码
     */
    private Integer parseStatus(String status) {
        try {
            return Integer.parseInt(status);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 构建响应对象
     */
    private SavingsGoalResponse buildResponse(SavingsGoal goal) {
        BigDecimal progress = BigDecimal.ZERO;
        if (goal.getTargetAmount() != null && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = goal.getSavedAmount()
                    .multiply(new BigDecimal("100"))
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);
        }

        SavingsStatus status = SavingsStatus.fromCode(goal.getStatus());

        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .bookId(goal.getBookId())
                .name(goal.getName())
                .targetAmount(goal.getTargetAmount())
                .savedAmount(goal.getSavedAmount())
                .progress(progress)
                .targetDate(goal.getTargetDate())
                .monthlyAmount(goal.getMonthlyAmount())
                .accountId(goal.getAccountId())
                .status(goal.getStatus())
                .statusName(status.getName())
                .note(goal.getNote())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    /**
     * 汇总统计实现
     */
    private static class SavingsSummaryImpl implements SavingsSummary {
        private final BigDecimal totalSaved;
        private final Integer totalGoals;
        private final Integer completedGoals;
        private final Integer activeGoals;

        SavingsSummaryImpl(BigDecimal totalSaved, Integer totalGoals, Integer completedGoals, Integer activeGoals) {
            this.totalSaved = totalSaved;
            this.totalGoals = totalGoals;
            this.completedGoals = completedGoals;
            this.activeGoals = activeGoals;
        }

        @Override
        public BigDecimal getTotalSaved() {
            return totalSaved;
        }

        @Override
        public Integer getTotalGoals() {
            return totalGoals;
        }

        @Override
        public Integer getCompletedGoals() {
            return completedGoals;
        }

        @Override
        public Integer getActiveGoals() {
            return activeGoals;
        }
    }
}
