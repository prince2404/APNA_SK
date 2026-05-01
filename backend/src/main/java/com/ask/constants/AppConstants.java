package com.ask.constants;

/**
 * Application-wide constants for timeouts, limits, sizes, and default values.
 * All hardcoded values must be defined here — never inline in code.
 */
public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // --- Pagination ---
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // --- Health Card ---
    public static final int MAX_FAMILY_MEMBERS = 5;
    public static final String HEALTH_CARD_PREFIX = "ASK-HC-";

    // --- Bill ---
    public static final String BILL_NUMBER_PREFIX = "ASK-";
    public static final int BILL_SEQUENCE_LENGTH = 4;

    // --- Return ---
    public static final String CONFIG_RETURN_WINDOW_DAYS = "RETURN_WINDOW_DAYS";
    public static final int DEFAULT_RETURN_WINDOW_DAYS = 7;

    // --- OTP ---
    public static final int OTP_LENGTH = 6;
    public static final int OTP_EXPIRY_MINUTES = 5;

    // --- File Upload ---
    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp"};
    public static final String[] ALLOWED_DOCUMENT_TYPES = {"application/pdf", "image/jpeg", "image/png"};

    // --- Stock ---
    public static final int LOW_STOCK_EXPIRY_WARNING_DAYS_30 = 30;
    public static final int LOW_STOCK_EXPIRY_WARNING_DAYS_60 = 60;
    public static final int LOW_STOCK_EXPIRY_WARNING_DAYS_90 = 90;

    // --- Audit ---
    public static final int AUDIT_LOG_RETENTION_YEARS = 1;
}
