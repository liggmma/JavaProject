package luxdine.example.luxdine.common.util;

public final class OtpSessionKeys {
    private OtpSessionKeys() {}
    public static final String PREFIX           = "auth.otp.";
    public static final String HASH             = PREFIX + "hash";
    public static final String EXPIRES          = PREFIX + "expiresAt";
    public static final String EMAIL            = PREFIX + "email";
    public static final String ATTEMPTS         = PREFIX + "attempts";
    public static final String LOCK_UNTIL       = PREFIX + "lockUntil";
    public static final String VERIFIED         = PREFIX + "verified";
    public static final String VERIFIED_EMAIL   = PREFIX + "verifiedEmail";
    public static final String PENDING_REG    = PREFIX + "pendingReg";

    public static final String SMS_HASH      = "OTP_SMS_HASH";
    public static final String SMS_EXPIRES   = "OTP_SMS_EXPIRES";
    public static final String SMS_PHONE     = "OTP_SMS_PHONE";
    public static final String SMS_ATTEMPTS  = "OTP_SMS_ATTEMPTS";
    public static final String SMS_LOCK_UNTIL= "OTP_SMS_LOCK_UNTIL";

    public static final String EC_PREFIX      = "profile.email.change.";
    public static final String EC_HASH        = EC_PREFIX + "hash";
    public static final String EC_EXPIRES     = EC_PREFIX + "expiresAt";
    public static final String EC_EMAIL       = EC_PREFIX + "newEmail";
    public static final String EC_ATTEMPTS    = EC_PREFIX + "attempts";
    public static final String EC_LOCK_UNTIL  = EC_PREFIX + "lockUntil";
}