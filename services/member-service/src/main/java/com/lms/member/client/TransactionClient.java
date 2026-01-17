package com.lms.member.client;

import lombok.Builder;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "transaction-service", url = "${application.config.transaction-url}")
public interface TransactionClient {

    @GetMapping("/api/v1/transactions/summary/{memberId}")
    MonthlyTransactionSummary getMonthlySummary(
            @PathVariable("memberId") Long memberId,
            @RequestParam("month") String month);

    @Data
    @Builder
    class MonthlyTransactionSummary {
        private BigDecimal totalAmount;
        private Integer transactionCount;
    }
}
