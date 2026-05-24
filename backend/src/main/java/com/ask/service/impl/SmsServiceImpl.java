package com.ask.service.impl;

import com.ask.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private final RestTemplate restTemplate;
    private final String authKey;
    private final String senderId;
    private final String flowId;

    public SmsServiceImpl(
            @Value("${ask.msg91.auth-key:}") String authKey,
            @Value("${ask.msg91.sender-id:ASKERP}") String senderId,
            @Value("${ask.msg91.flow-id:}") String flowId) {
        this.restTemplate = new RestTemplate();
        this.authKey = authKey;
        this.senderId = senderId;
        this.flowId = flowId;
    }

    @Async
    @Override
    public void sendSms(String phone, String message) {
        if (authKey == null || authKey.trim().isEmpty()) {
            log.info("[SMS DRY RUN] Phone: {}, Message: {}", phone, message);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authkey", authKey);



            // Flow API payload format for MSG91
            Map<String, Object> payload = new HashMap<>();
            payload.put("flow_id", flowId);
            payload.put("sender", senderId);

            Map<String, String> recipient = new HashMap<>();
            recipient.put("mobiles", phone);
            recipient.put("VAR1", message); // assuming template has variable VAR1

            payload.put("recipients", List.of(recipient));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            String url = "https://control.msg91.com/api/v5/flow/";
            
            log.info("Sending MSG91 SMS to mobile: {}", phone);
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            log.error("Failed to send MSG91 SMS to {}: {}", phone, e.getMessage());
        }
    }
}
