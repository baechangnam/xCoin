package com.kr.forbidden;

import android.content.Context;

/** Versioned consent; excluded from backup so another installation must consent again. */
final class AccessibilityConsent {
    private static final String PREFERENCES = "accessibility_consent";
    private static final int DISCLOSURE_VERSION = 1;

    private AccessibilityConsent() { }

    static boolean isGranted(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .getInt("accepted_version", 0) == DISCLOSURE_VERSION;
    }

    static void grant(Context context) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
                .putInt("accepted_version", DISCLOSURE_VERSION).apply();
    }

    static void revoke(Context context) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
