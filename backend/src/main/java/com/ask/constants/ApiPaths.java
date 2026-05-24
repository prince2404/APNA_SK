package com.ask.constants;

/**
 * All API path constants.
 * Every controller uses these instead of hardcoded strings.
 */
public final class ApiPaths {

    private ApiPaths() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // --- Base ---
    public static final String V1 = "/v1";

    // --- Auth ---
    public static final String AUTH = V1 + "/auth";
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_REFRESH = "/refresh";
    public static final String AUTH_LOGOUT = "/logout";
    public static final String AUTH_VERIFY_OTP = "/verify-otp";
    public static final String AUTH_RESEND_OTP = "/resend-otp";
    public static final String AUTH_CHANGE_PASSWORD = "/change-password";

    // --- Users ---
    public static final String USERS = V1 + "/users";

    // --- Geography ---
    public static final String STATES = V1 + "/states";
    public static final String DISTRICTS = V1 + "/districts";
    public static final String BLOCKS = V1 + "/blocks";
    public static final String STORES = V1 + "/stores";

    // --- Patients ---
    public static final String PATIENTS = V1 + "/patients";
    public static final String PATIENTS_BULK_UPLOAD = "/bulk-upload";
    public static final String HOSPITALS = V1 + "/hospitals";

    // --- Health Cards ---
    public static final String HEALTH_CARDS = V1 + "/health-cards";

    // --- Products ---
    public static final String PRODUCT_CATEGORIES = V1 + "/product-categories";
    public static final String PRODUCTS = V1 + "/products";

    // --- Inventory ---
    public static final String STOCK_CENTRAL = V1 + "/stock/central";
    public static final String TRANSFER_ORDERS = V1 + "/transfer-orders";
    public static final String STOCK_STORE = V1 + "/stock/store";
    public static final String STOCK_REQUESTS = V1 + "/stock-requests";
    public static final String STOCK_ADJUSTMENTS = V1 + "/stock/adjustments";

    // --- Billing ---
    public static final String BILLS = V1 + "/bills";
    public static final String SCHEMES = V1 + "/schemes";

    // --- Commission ---
    public static final String COMMISSIONS = V1 + "/commissions";
    public static final String COMMISSION_CONFIG = V1 + "/commissions/config";

    // --- Notifications ---
    public static final String NOTIFICATIONS = V1 + "/notifications";

    // --- Messaging ---
    public static final String MESSAGES = V1 + "/messages";
    public static final String MESSAGE_TEMPLATES = V1 + "/message-templates";

    // --- Reports ---
    public static final String REPORTS = V1 + "/reports";

    // --- Profile ---
    public static final String PROFILE = V1 + "/profile";

    // --- Sessions ---
    public static final String SESSIONS = V1 + "/sessions";

    // --- Permissions ---
    public static final String PERMISSIONS = V1 + "/permissions";
    public static final String PERMISSION_REQUESTS = V1 + "/permission-requests";

    // --- System Config ---
    public static final String SYSTEM_CONFIG = V1 + "/system-config";

    // --- Dashboard ---
    public static final String DASHBOARD = V1 + "/dashboard";
}
