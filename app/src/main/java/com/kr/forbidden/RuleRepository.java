package com.kr.forbidden;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class RuleRepository {
    private static final String PREF_NAME = "forbidden_rules";
    private static final String KEY_BLOCKED_SITES = "blocked_sites";
    private static final String KEY_BLOCKED_PACKAGES = "blocked_packages";
    private static final String KEY_SEARCH_ENABLED = "search_detection_enabled";
    private static final String KEY_SEARCH_KEYWORDS = "search_keywords";

    private RuleRepository() {
    }

    public static Set<String> getBlockedSites(Context context) {
        return parseEntries(getPreferences(context).getString(KEY_BLOCKED_SITES, ""));
    }

    public static boolean isSearchDetectionEnabled(Context context) {
        return getPreferences(context).getBoolean(KEY_SEARCH_ENABLED, false);
    }

    public static String getSearchKeywords(Context context) {
        return getPreferences(context).getString(KEY_SEARCH_KEYWORDS,
                context.getString(R.string.search_default_keywords));
    }

    public static void saveSearchSettings(Context context, boolean enabled, String keywords) {
        getPreferences(context).edit().putBoolean(KEY_SEARCH_ENABLED, enabled)
                .putString(KEY_SEARCH_KEYWORDS, keywords).apply();
    }

    public static Set<String> getBlockedPackages(Context context) {
        return parseEntries(getPreferences(context).getString(KEY_BLOCKED_PACKAGES, ""));
    }

    public static void saveRules(Context context, String blockedSites, String blockedPackages) {
        getPreferences(context)
                .edit()
                .putString(KEY_BLOCKED_SITES, blockedSites == null ? "" : blockedSites)
                .putString(KEY_BLOCKED_PACKAGES, blockedPackages == null ? "" : blockedPackages)
                .apply();
    }

    public static String getBlockedSitesText(Context context) {
        return getPreferences(context).getString(KEY_BLOCKED_SITES, "");
    }

    public static String getBlockedPackagesText(Context context) {
        return getPreferences(context).getString(KEY_BLOCKED_PACKAGES, "");
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static Set<String> parseEntries(String rawValue) {
        Set<String> values = new LinkedHashSet<>();
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return values;
        }

        String[] parts = rawValue.split("[,\\n]");
        for (String part : parts) {
            String normalized = part.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
                values.add(normalized);
            }
        }
        return values;
    }
}
