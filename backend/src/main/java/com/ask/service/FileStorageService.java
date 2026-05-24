package com.ask.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for local file storage operations.
 */
public interface FileStorageService {

    /**
     * Stores a file in the specified subdirectory within the upload base path.
     * Generates a unique filename using UUID to prevent collisions.
     *
     * @param file the multipart file to store
     * @param subDir the target subdirectory (e.g., "profile-photos", "kyc-docs")
     * @return the unique generated filename
     */
    String storeFile(MultipartFile file, String subDir);

    /**
     * Loads a file as a Spring Resource from the specified subdirectory.
     *
     * @param fileName the name of the file to load
     * @param subDir the subdirectory where the file is stored
     * @return the file as a Resource
     */
    Resource loadFileAsResource(String fileName, String subDir);

    /**
     * Deletes a file from the specified subdirectory.
     *
     * @param fileName the name of the file to delete
     * @param subDir the subdirectory where the file is stored
     */
    void deleteFile(String fileName, String subDir);
}
