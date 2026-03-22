package com.ledger.app.common.constant;

/**
 * 常量定义
 */
public class Constants {

    /**
     * Redis Key 前缀
     */
    public static class RedisKey {
        public static final String USER_TOKEN_PREFIX = "user:token:";
        public static final String USER_INFO_PREFIX = "user:info:";
    }

    /**
     * 响应码
     */
    public static class ResponseCode {
        public static final int SUCCESS = 200;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int ERROR = 500;
    }

    /**
     * 交易类型
     */
    public static class TransactionType {
        public static final String INCOME = "income";
        public static final String EXPENSE = "expense";
    }
}
