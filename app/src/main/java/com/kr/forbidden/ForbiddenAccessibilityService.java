package com.kr.forbidden;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ForbiddenAccessibilityService extends AccessibilityService {
    private static final String CHROME_PACKAGE = "com.android.chrome";
    private static final long ALERT_THROTTLE_MS = 10000L;
    private static final String URL_BAR_VIEW_ID = "com.android.chrome:id/url_bar";
    private static final String SEARCH_BOX_VIEW_ID = "com.android.chrome:id/search_box_text";

    private long lastAlertAt;
    private String lastAlertKey = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }

        String packageName = event.getPackageName().toString().toLowerCase(Locale.ROOT);
        // Content events can arrive from background windows. Act on the active app.
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null && root.getPackageName() != null) {
            packageName = root.getPackageName().toString().toLowerCase(Locale.ROOT);
        } else if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }
        if (handleBlockedAppEvent(packageName)) {
            return;
        }
        if (CHROME_PACKAGE.equals(packageName)) {
            handleChromeEvent();
        }
    }

    @Override
    public void onInterrupt() {
    }

    private void handleChromeEvent() {
        Set<String> blockedSites = RuleRepository.getBlockedSites(this);
        boolean searchEnabled = RuleRepository.isSearchDetectionEnabled(this);
        if (blockedSites.isEmpty() && !searchEnabled) {
            return;
        }

        if (searchEnabled && handleChromeSearch()) {
            return;
        }

        String currentAddress = extractChromeAddress();
        if (currentAddress.isEmpty()) {
            return;
        }

        for (String blockedSite : blockedSites) {
            if (matchesBlockedSite(currentAddress, blockedSite)) {
                maybeAlert("site:" + blockedSite, getString(R.string.site_blocked_title),
                        getString(R.string.site_blocked_message, blockedSite));
                return;
            }
        }
    }

    private boolean handleChromeSearch() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null || !CHROME_PACKAGE.contentEquals(
                root.getPackageName() == null ? "" : root.getPackageName())) {
            return false;
        }
        String keywords = RuleRepository.getSearchKeywords(this);
        for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByViewId(URL_BAR_VIEW_ID)) {
            // Do not interrupt typing or read search suggestions / webpage text.
            if (node == null || node.isFocused() || !node.isVisibleToUser() || node.getText() == null) {
                continue;
            }
            String matched = ChromeSearchMatcher.match(node.getText().toString(), keywords);
            if (!matched.isEmpty()) {
                maybeAlert("search:" + matched, getString(R.string.search_detected_title),
                        getString(R.string.search_detected_message, matched));
                return true;
            }
        }
        return false;
    }

    private boolean handleBlockedAppEvent(String packageName) {
        if (getPackageName().equals(packageName)) {
            return false;
        }

        Set<String> blockedPackages = RuleRepository.getBlockedPackages(this);
        if (!blockedPackages.contains(packageName)) {
            return false;
        }

        // Suppress repeated alerts only; every re-entry still needs a HOME action.
        if (!performGlobalAction(GLOBAL_ACTION_HOME)) {
            Intent home = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(home);
            } catch (RuntimeException error) {
                Log.w("Forbidden", "Unable to leave blocked app", error);
            }
        }
        maybeAlert(
                "app:" + packageName,
                getString(R.string.app_launch_blocked_title),
                getString(R.string.app_launch_blocked_message, packageName)
        );
        return true;
    }

    private String extractChromeAddress() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return "";
        }

        List<String> candidates = new ArrayList<>();
        addTextsFromViewId(root, URL_BAR_VIEW_ID, candidates);
        addTextsFromViewId(root, SEARCH_BOX_VIEW_ID, candidates);
        for (String candidate : candidates) {
            String normalized = normalizeAddress(candidate);
            if (!normalized.isEmpty()) {
                return normalized;
            }
        }
        return "";
    }

    private void addTextsFromViewId(AccessibilityNodeInfo root, String viewId, List<String> out) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(viewId);
        if (nodes == null) {
            return;
        }

        for (AccessibilityNodeInfo node : nodes) {
            if (node == null) {
                continue;
            }
            CharSequence text = node.getText();
            if (text != null) {
                out.add(text.toString());
            }
        }
    }

    private boolean matchesBlockedSite(String address, String blockedSite) {
        String normalizedRule = normalizeRule(blockedSite);
        if (normalizedRule.isEmpty()) {
            return false;
        }

        String host = extractHost(address);
        if (host.isEmpty()) {
            return false;
        }

        return host.equals(normalizedRule) || host.endsWith("." + normalizedRule);
    }

    private String normalizeRule(String blockedSite) {
        String normalized = blockedSite.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("http://")) {
            normalized = normalized.substring(7);
        } else if (normalized.startsWith("https://")) {
            normalized = normalized.substring(8);
        }

        int slashIndex = normalized.indexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(0, slashIndex);
        }
        if (normalized.startsWith("www.")) {
            normalized = normalized.substring(4);
        }
        return normalized;
    }

    private String normalizeAddress(String candidate) {
        String normalized = candidate == null ? "" : candidate.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return "";
        }

        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        return normalized;
    }

    private String extractHost(String address) {
        try {
            Uri uri = Uri.parse(address);
            String host = uri.getHost();
            if (host == null) {
                return "";
            }
            host = host.toLowerCase(Locale.ROOT);
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private void maybeAlert(String alertKey, String title, String message) {
        long now = SystemClock.elapsedRealtime();
        if (alertKey.equals(lastAlertKey) && now - lastAlertAt < ALERT_THROTTLE_MS) {
            return;
        }

        lastAlertKey = alertKey;
        lastAlertAt = now;
        AlertDispatcher.showAlert(this, title, message);
    }
}
