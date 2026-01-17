package com.lms.transaction.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberClient {
    private final RestTemplate restTemplate;
    private static final String MEMBER_SERVICE_URL = "http://localhost:8081/api/v1/members/";

    public boolean memberExists(Long memberId) {
        try {
            restTemplate.getForEntity(MEMBER_SERVICE_URL + memberId, Object.class);
            return true;
        } catch (Exception e) {
            log.warn("Member {} not found or error calling member-service", memberId);
            return false;
        }
    }
}
