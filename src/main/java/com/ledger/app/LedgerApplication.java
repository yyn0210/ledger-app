package com.ledger.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 全平台智能记账应用 - 后端主启动类
 */
@SpringBootApplication
@MapperScan("com.ledger.app.modules.*.mapper")
public class LedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerApplication.class, args);
    }
}
