package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.response.common.ApiResponse;
import com.ask.entity.SystemConfig;
import com.ask.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing system-wide configuration key-value pairs.
 * Only accessible by Super Admin.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.SYSTEM_CONFIG)
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigRepository systemConfigRepository;

    /**
     * GET /api/v1/system-config — Get all system configuration entries.
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SystemConfig>>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(configs, ApiPaths.SYSTEM_CONFIG));
    }

    /**
     * PUT /api/v1/system-config — Bulk update system configuration entries.
     */
    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateConfigs(@RequestBody ConfigUpdateRequest request) {
        if (request.getConfigs() == null || request.getConfigs().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(null, "No changes to save", ApiPaths.SYSTEM_CONFIG));
        }

        for (ConfigEntry entry : request.getConfigs()) {
            systemConfigRepository.findByConfigKey(entry.getConfigKey()).ifPresent(config -> {
                config.setConfigValue(entry.getConfigValue());
                systemConfigRepository.save(config);
            });
        }

        return ResponseEntity.ok(ApiResponse.success(null, "Settings saved successfully", ApiPaths.SYSTEM_CONFIG));
    }

    // --- Request DTOs (inner classes for simplicity) ---

    @lombok.Data
    public static class ConfigUpdateRequest {
        private List<ConfigEntry> configs;
    }

    @lombok.Data
    public static class ConfigEntry {
        private String configKey;
        private String configValue;
    }
}
