package com.lms.reward.client;

import com.lms.reward.model.TransactionSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "transaction-service", url = "${app.transaction-service-url:http://localhost:8082}")
public interface TransactionClient {
    @GetMapping("/api/v1/transactions/summary/{memberId}")
    TransactionSummary getSummary(@PathVariable("memberId") Long memberId, @RequestParam("period") String period);
}
