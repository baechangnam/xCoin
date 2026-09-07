package com.kr.forbidden;

import android.content.Context;
import android.content.SharedPreferences;

final class WarningPreferences {
    static final String TITLE = "warning_title";
    static final String SUBTITLE = "warning_subtitle";
    static final String REASON = "warning_reason";

    private WarningPreferences() { }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences("warning_preferences", Context.MODE_PRIVATE);
    }

    static String title(Context context) {
        return orDefault(preferences(context).getString(TITLE, ""), context.getString(R.string.warning_default_title));
    }

    static String subtitle(Context context) {
        return orDefault(preferences(context).getString(SUBTITLE, ""), context.getString(R.string.warning_default_subtitle));
    }

    static String reason(Context context) {
        return preferences(context).getString(REASON, "");
    }

    static void save(Context context, String title, String subtitle, String reason) {
        preferences(context).edit().putString(TITLE, title.trim())
                .putString(SUBTITLE, subtitle.trim()).putString(REASON, reason.trim()).apply();
    }

    static String orDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
