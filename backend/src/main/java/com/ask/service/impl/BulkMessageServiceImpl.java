package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.message.BulkMessageRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.message.BulkMessageLogResponse;
import com.ask.entity.BulkMessageLog;
import com.ask.entity.MessageTemplate;
import com.ask.entity.User;
import com.ask.enums.BulkMessageStatus;
import com.ask.enums.MessageChannel;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.BulkMessageLogRepository;
import com.ask.repository.MessageTemplateRepository;
import com.ask.repository.UserRepository;
import com.ask.service.BulkMessageService;
import com.ask.service.EmailService;
import com.ask.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkMessageServiceImpl implements BulkMessageService {

    private final UserRepository userRepository;
    private final MessageTemplateRepository templateRepository;
    private final BulkMessageLogRepository logRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final EntityManager entityManager;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void ensureAdmin(User user) {
        String role = user.getRole().getName();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.SYSTEM_ADMIN.equals(role)) {
            throw new AccessDeniedException("Access denied. Admin role required for bulk messaging.");
        }
    }

    @Override
    @Transactional
    public BulkMessageLogResponse sendBulkMessage(BulkMessageRequest request, String senderEmail) {
        User sender = getCurrentUser(senderEmail);
        ensureAdmin(sender);

        String rawContent = "";
        if (request.getTemplateId() != null) {
            MessageTemplate template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("MessageTemplate", "id", request.getTemplateId()));
            rawContent = template.getContent();
        } else if (request.getCustomText() != null && !request.getCustomText().trim().isEmpty()) {
            rawContent = request.getCustomText().trim();
        } else {
            throw new BusinessRuleException("Either templateId or customText must be provided");
        }

        // 1. Resolve Target Users
        List<User> targets = resolveTargets(request);

        // 2. Dispatch
        int sentCount = 0;
        for (User u : targets) {
            String content = rawContent
                    .replace("{{name}}", u.getFullName())
                    .replace("{{role}}", u.getRole().getDisplayName() != null ? u.getRole().getDisplayName() : u.getRole().getName());

            if (request.getChannel() == MessageChannel.EMAIL && u.getEmail() != null) {
                emailService.sendSimpleEmail(u.getEmail(), "Apna Swasthya Kendra Broadcast", content);
                sentCount++;
            } else if (request.getChannel() == MessageChannel.SMS && u.getPhone() != null) {
                smsService.sendSms(u.getPhone(), content);
                sentCount++;
            }
        }

        // 3. Save Log
        BulkMessageLog messageLog = BulkMessageLog.builder()
                .sender(sender)
                .channel(request.getChannel())
                .targetCriteria(buildTargetCriteriaString(request))
                .content(rawContent)
                .sentCount(sentCount)
                .status(sentCount > 0 ? BulkMessageStatus.SUCCESS : BulkMessageStatus.FAILED)
                .build();

        BulkMessageLog saved = logRepository.save(messageLog);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BulkMessageLogResponse> getBulkMessageHistory(int page, int size, String senderEmail) {
        User sender = getCurrentUser(senderEmail);
        ensureAdmin(sender);

        Page<BulkMessageLog> logsPage = logRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        List<BulkMessageLogResponse> content = logsPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.of(logsPage, content);
    }

    private List<User> resolveTargets(BulkMessageRequest req) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE u.status = 'ACTIVE' ");
        Map<String, Object> params = new HashMap<>();

        if (req.getTargetRole() != null && !req.getTargetRole().trim().isEmpty()) {
            jpql.append("AND u.role.name = :roleName ");
            params.put("roleName", req.getTargetRole().trim());
        }
        if (req.getStateId() != null) {
            jpql.append("AND u.state.id = :stateId ");
            params.put("stateId", req.getStateId());
        }
        if (req.getDistrictId() != null) {
            jpql.append("AND u.district.id = :districtId ");
            params.put("districtId", req.getDistrictId());
        }
        if (req.getBlockId() != null) {
            jpql.append("AND u.block.id = :blockId ");
            params.put("blockId", req.getBlockId());
        }
        if (req.getStoreId() != null) {
            jpql.append("AND u.store.id = :storeId ");
            params.put("storeId", req.getStoreId());
        }

        TypedQuery<User> query = entityManager.createQuery(jpql.toString(), User.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.getResultList();
    }

    private String buildTargetCriteriaString(BulkMessageRequest req) {
        List<String> parts = new ArrayList<>();
        if (req.getTargetRole() != null && !req.getTargetRole().trim().isEmpty()) {
            parts.add("Role: " + req.getTargetRole());
        }
        if (req.getStateId() != null) {
            parts.add("State ID: " + req.getStateId());
        }
        if (req.getDistrictId() != null) {
            parts.add("District ID: " + req.getDistrictId());
        }
        if (req.getBlockId() != null) {
            parts.add("Block ID: " + req.getBlockId());
        }
        if (req.getStoreId() != null) {
            parts.add("Store ID: " + req.getStoreId());
        }
        if (parts.isEmpty()) {
            return "Broadcast (All Active Users)";
        }
        return String.join(", ", parts);
    }

    private BulkMessageLogResponse toResponse(BulkMessageLog log) {
        return BulkMessageLogResponse.builder()
                .id(log.getId())
                .senderName(log.getSender().getFullName())
                .channel(log.getChannel())
                .targetCriteria(log.getTargetCriteria())
                .content(log.getContent())
                .sentCount(log.getSentCount())
                .status(log.getStatus())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
