package com.lms.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "APIs for triggering manual and systemic loyalty events")
public class EventController {
        private final RabbitTemplate rabbitTemplate;

        @PostMapping("/trigger")
        @Operation(summary = "Trigger generic event", description = "Trigger a generic loyalty event that will be processed by rules")
        public ResponseEntity<String> processGenericEvent(@RequestBody Map<String, Object> event) {
                if (!event.containsKey("eventType")) {
                        return ResponseEntity.badRequest().body("Missing eventType");
                }

                long uniqueTxId = (System.currentTimeMillis() << 20)
                                | (java.util.concurrent.ThreadLocalRandom.current().nextLong() & 0xFFFFF);

                // Ensure transactionId is present if not provided
                if (!event.containsKey("transactionId")) {
                        event.put("transactionId", uniqueTxId);
                }

                if (!event.containsKey("timestamp")) {
                        event.put("timestamp", java.time.LocalDateTime.now().toString());
                }

                rabbitTemplate.convertAndSend("event.occurrence.queue", event);

                return ResponseEntity.ok("Event published: " + event.get("eventType"));
        }

        @PostMapping("/onboard")
        @Operation(summary = "Trigger onboarding event", description = "Award welcome bonus points to a new member")
        public ResponseEntity<String> processOnboarding(@RequestBody Map<String, Object> event) {
                event.put("eventType", "ONBOARDING");
                return processGenericEvent(event);
        }

        @PostMapping("/referral")
        @Operation(summary = "Trigger referral event", description = "Award referral bonuses")
        public ResponseEntity<String> processReferral(@RequestBody Map<String, Object> event) {
                event.put("eventType", "REFERRAL");
                return processGenericEvent(event);
        }
}
