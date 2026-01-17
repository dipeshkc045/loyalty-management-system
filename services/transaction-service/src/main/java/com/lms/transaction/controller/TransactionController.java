package com.lms.transaction.controller;

import com.lms.transaction.event.TransactionEvent;
import com.lms.transaction.event.TransactionEventProducer;
import com.lms.transaction.model.Transaction;
import com.lms.transaction.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for recording customer transactions and retrieving transaction summaries")
public class TransactionController {
        private final TransactionRepository transactionRepository;
        private final TransactionEventProducer eventProducer;
        private final com.lms.transaction.client.MemberClient memberClient;

        @PostMapping
        @Operation(summary = "Create transaction", description = "Record a new customer transaction and publish event for reward processing")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Transaction created successfully", content = @Content(schema = @Schema(implementation = Transaction.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid transaction data or member does not exist", content = @Content),
                        @ApiResponse(responseCode = "422", description = "Validation failed (e.g., negative amount)", content = @Content)
        })
        public ResponseEntity<?> createTransaction(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Transaction details", required = true, content = @Content(schema = @Schema(implementation = Transaction.class))) @Valid @RequestBody Transaction transaction) {
                if (!memberClient.memberExists(transaction.getMemberId())) {
                        return ResponseEntity.badRequest()
                                        .body("Member with ID " + transaction.getMemberId() + " does not exist.");
                }

                Transaction saved = transactionRepository.save(transaction);

                eventProducer.sendTransactionCreated(TransactionEvent.builder()
                                .transactionId(saved.getId())
                                .memberId(saved.getMemberId())
                                .amount(saved.getAmount())
                                .paymentMethod(saved.getPaymentMethod())
                                .productCategory(saved.getProductCategory())
                                .build());

                return ResponseEntity.ok(saved);
        }

        @GetMapping
        @Operation(summary = "Get all transactions", description = "Retrieve a list of all transactions")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "List of transactions retrieved successfully", content = @Content(schema = @Schema(implementation = Transaction.class)))
        })
        public ResponseEntity<java.util.List<Transaction>> getAllTransactions() {
                return ResponseEntity.ok(transactionRepository
                                .findAll(org.springframework.data.domain.Sort.by(
                                                org.springframework.data.domain.Sort.Direction.DESC, "transactionDate"))
                                .stream().limit(50).collect(java.util.stream.Collectors.toList()));
        }

        @GetMapping("/member/{memberId}")
        @Operation(summary = "Get transactions by member", description = "Retrieve a list of transactions for a specific member")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "List of member transactions retrieved successfully", content = @Content(schema = @Schema(implementation = Transaction.class)))
        })
        public ResponseEntity<java.util.List<Transaction>> getTransactionsByMember(
                        @Parameter(description = "Member ID", required = true, example = "1") @PathVariable Long memberId) {
                return ResponseEntity.ok(transactionRepository.findAllByMemberIdOrderByTransactionDateDesc(memberId));
        }

        @GetMapping("/summary/{memberId}")
        @Operation(summary = "Get transaction summary", description = "Retrieve transaction summary for a member within a specified period")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Summary retrieved successfully", content = @Content(schema = @Schema(implementation = com.lms.transaction.model.TransactionSummary.class)))
        })
        public ResponseEntity<com.lms.transaction.model.TransactionSummary> getSummary(
                        @Parameter(description = "Member ID", required = true, example = "1") @PathVariable Long memberId,
                        @Parameter(description = "Time period (MONTHLY, QUARTERLY, YEARLY) or YYYY-MM", example = "MONTHLY") @RequestParam(required = false) String period,
                        @Parameter(description = "Specific month (YYYY-MM)", example = "2026-01") @RequestParam(required = false) String month) {

                // Handle specific month request (from Tier Evaluation Service)
                if (month != null && month.matches("\\d{4}-\\d{2}")) {
                        java.time.YearMonth ym = java.time.YearMonth.parse(month);
                        return transactionRepository
                                        .getMonthlySummary(memberId, ym.atDay(1).atStartOfDay(),
                                                        ym.atEndOfMonth().atTime(23, 59, 59))
                                        .map(ResponseEntity::ok)
                                        .orElse(ResponseEntity.ok(
                                                        new com.lms.transaction.model.TransactionSummary(memberId, 0L,
                                                                        java.math.BigDecimal.ZERO)));
                }

                // Handle period shortcut
                if (period != null && period.matches("\\d{4}-\\d{2}")) {
                        java.time.YearMonth ym = java.time.YearMonth.parse(period);
                        return transactionRepository
                                        .getMonthlySummary(memberId, ym.atDay(1).atStartOfDay(),
                                                        ym.atEndOfMonth().atTime(23, 59, 59))
                                        .map(ResponseEntity::ok)
                                        .orElse(ResponseEntity.ok(
                                                        new com.lms.transaction.model.TransactionSummary(memberId, 0L,
                                                                        java.math.BigDecimal.ZERO)));
                }

                java.time.LocalDateTime since = java.time.LocalDateTime.now().minusMonths(1); // Default
                if ("QUARTERLY".equalsIgnoreCase(period))
                        since = java.time.LocalDateTime.now().minusMonths(3);
                else if ("YEARLY".equalsIgnoreCase(period))
                        since = java.time.LocalDateTime.now().minusYears(1);

                return transactionRepository.getSummary(memberId, since)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity
                                                .ok(new com.lms.transaction.model.TransactionSummary(memberId, 0L,
                                                                java.math.BigDecimal.ZERO)));
        }

        @DeleteMapping
        @Operation(summary = "Delete all transactions", description = "Remove all transactions from the system (Dev/Cleanup only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "All transactions deleted successfully")
        })
        public ResponseEntity<Void> deleteAllTransactions() {
                transactionRepository.deleteAll();
                return ResponseEntity.noContent().build();
        }
}
