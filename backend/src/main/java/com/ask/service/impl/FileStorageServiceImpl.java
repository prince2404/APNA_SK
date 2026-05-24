package com.ask.service.impl;

import com.ask.exception.InvalidRequestException;
import com.ask.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${ask.upload.dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            log.error("Could not create the upload directory: {}", uploadDir, e);
            throw new RuntimeException("Could not create the upload directory structure", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new InvalidRequestException("Cannot store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new InvalidRequestException("Filename contains invalid path sequence: " + originalFilename);
        }

        // Generate unique filename
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        try {
            Path targetDir = this.fileStorageLocation.resolve(subDir).normalize();
            Files.createDirectories(targetDir);

            Path targetLocation = targetDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFilename;
        } catch (IOException e) {
            log.error("Failed to store file {}", originalFilename, e);
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName, String subDir) {
        try {
            Path filePath = this.fileStorageLocation.resolve(subDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException e) {
            log.error("File malformed URL: {}", fileName, e);
            throw new ResourceNotFoundException("File not found: " + fileName, e);
        }
    }

    @Override
    public void deleteFile(String fileName, String subDir) {
        try {
            Path filePath = this.fileStorageLocation.resolve(subDir).resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", fileName, e.getMessage());
        }
    }

    // Helper exception class in case ResourceNotFoundException is needed locally or imported
    private static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
        public ResourceNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
