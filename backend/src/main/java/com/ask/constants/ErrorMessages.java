package com.ask.constants;

/**
 * All error message strings used across the application.
 * Centralised here so no error message is ever hardcoded in service or controller layers.
 */
public final class ErrorMessages {

    private ErrorMessages() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // --- Auth ---
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String ACCOUNT_LOCKED = "Account is locked due to too many failed login attempts. Please try again after %d minutes";
    public static final String ACCOUNT_INACTIVE = "Your account has been deactivated. Please contact your administrator";
    public static final String TOKEN_EXPIRED = "Your session has expired. Please log in again";
    public static final String TOKEN_INVALID = "Invalid authentication token";
    public static final String REFRESH_TOKEN_INVALID = "Invalid or expired refresh token";
    public static final String REFRESH_TOKEN_REVOKED = "This session has been revoked";
    public static final String OTP_INVALID = "Invalid OTP. Please try again";
    public static final String OTP_EXPIRED = "OTP has expired. Please request a new one";
    public static final String TWO_FA_REQUIRED = "Two-factor authentication is required for your account";
    public static final String PASSWORD_CHANGE_REQUIRED = "You must change your temporary password before continuing";

    // --- Access Control ---
    public static final String ACCESS_DENIED = "You do not have permission to perform this action";
    public static final String GEOGRAPHIC_SCOPE_VIOLATION = "You do not have access to data in this geographic region";
    public static final String ROLE_HIERARCHY_VIOLATION = "You cannot manage users at or above your own role level";
    public static final String PERMISSION_DENIED = "You do not have the required permission: %s";

    // --- Resource ---
    public static final String RESOURCE_NOT_FOUND = "%s not found with %s: %s";
    public static final String DUPLICATE_RESOURCE = "%s already exists with %s: %s";

    // --- User ---
    public static final String USER_NOT_FOUND = "User not found";
    public static final String EMAIL_ALREADY_EXISTS = "A user with this email already exists";
    public static final String PHONE_ALREADY_EXISTS = "A user with this phone number already exists";
    public static final String CANNOT_DEACTIVATE_SELF = "You cannot deactivate your own account";
    public static final String ONLY_SUPER_ADMIN_CAN_DEACTIVATE = "Only the Super Admin can deactivate or reactivate users";

    // --- Aadhaar ---
    public static final String AADHAAR_FULL_NUMBER_REJECTED = "Full Aadhaar number is not accepted. Please provide only the last 4 digits";

    // --- Geography ---
    public static final String STATE_NOT_FOUND = "State not found";
    public static final String DISTRICT_NOT_FOUND = "District not found";
    public static final String BLOCK_NOT_FOUND = "Block not found";
    public static final String STORE_NOT_FOUND = "Store not found";

    // --- Validation ---
    public static final String FIELD_REQUIRED = "%s is required";
    public static final String FIELD_INVALID = "%s is invalid";
    public static final String PAGE_SIZE_EXCEEDED = "Page size cannot exceed " + com.ask.constants.AppConstants.MAX_PAGE_SIZE;

    // --- General ---
    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later";
    public static final String BAD_REQUEST = "Invalid request. Please check your input and try again";
    public static final String METHOD_NOT_ALLOWED = "This HTTP method is not supported for this endpoint";
}
