package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.message.MessageTemplateRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.message.MessageTemplateResponse;
import com.ask.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.MESSAGE_TEMPLATES)
public class MessageTemplateController {

    private final MessageTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<MessageTemplateResponse>> createTemplate(
            @Valid @RequestBody MessageTemplateRequest request) {
        log.info("Creating message template: {}", request.getName());
        MessageTemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Message template created successfully", ApiPaths.MESSAGE_TEMPLATES));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<MessageTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody MessageTemplateRequest request) {
        log.info("Updating message template ID: {}", id);
        MessageTemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Message template updated successfully", ApiPaths.MESSAGE_TEMPLATES + "/" + id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MessageTemplateResponse>>> getAllTemplates() {
        log.info("Fetching all message templates");
        List<MessageTemplateResponse> response = templateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.MESSAGE_TEMPLATES));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MessageTemplateResponse>> getTemplate(@PathVariable Long id) {
        log.info("Fetching message template ID: {}", id);
        MessageTemplateResponse response = templateService.getTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.MESSAGE_TEMPLATES + "/" + id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        log.info("Deleting message template ID: {}", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Message template deleted successfully", ApiPaths.MESSAGE_TEMPLATES + "/" + id));
    }
}
