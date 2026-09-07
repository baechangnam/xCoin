package com.kr.forbidden;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import android.view.accessibility.AccessibilityManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import android.view.View;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class MainActivity extends AppCompatActivity {
    private EditText sitesInput;
    private EditText packagesInput;
    private TextView monitoringStatus;
    private EditText warningTitleInput;
    private EditText warningSubtitleInput;
    private EditText warningReasonInput;
    private EditText searchKeywordsInput;
    private SwitchMaterial searchDetectionSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);
        applyWindowInsets();

        sitesInput = findViewById(R.id.blockedSitesInput);
        packagesInput = findViewById(R.id.blockedPackagesInput);
        monitoringStatus = findViewById(R.id.monitoringStatus);
        searchKeywordsInput = findViewById(R.id.searchKeywordsInput);
        searchDetectionSwitch = findViewById(R.id.searchDetectionSwitch);
        searchKeywordsInput.setText(RuleRepository.getSearchKeywords(this));
        searchDetectionSwitch.setChecked(RuleRepository.isSearchDetectionEnabled(this));
        searchDetectionSwitch.setOnCheckedChangeListener((button, enabled) -> {
            RuleRepository.saveSearchSettings(this, enabled, searchKeywordsInput.getText().toString());
            Toast.makeText(this, enabled ? R.string.search_enabled_toast : R.string.search_disabled_toast,
                    Toast.LENGTH_SHORT).show();
        });
        warningTitleInput = findViewById(R.id.warningTitleInput);
        warningSubtitleInput = findViewById(R.id.warningSubtitleInput);
        warningReasonInput = findViewById(R.id.warningReasonInput);
        warningTitleInput.setText(WarningPreferences.title(this));
        warningSubtitleInput.setText(WarningPreferences.subtitle(this));
        warningReasonInput.setText(WarningPreferences.reason(this));
        Button saveButton = findViewById(R.id.saveButton);
        Button selectCoinSitesButton = findViewById(R.id.selectCoinSitesButton);
        Button selectCoinAppsButton = findViewById(R.id.selectCoinAppsButton);
        Button accessibilityButton = findViewById(R.id.openAccessibilityButton);
        Button notificationButton = findViewById(R.id.openNotificationButton);
        Button testWarningButton = findViewById(R.id.testWarningButton);

        sitesInput.setText(RuleRepository.getBlockedSitesText(this));
        packagesInput.setText(RuleRepository.getBlockedPackagesText(this));

        saveButton.setOnClickListener(v -> saveRules());
        selectCoinSitesButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.coin_sites_title)
                .setItems(R.array.coin_site_categories, (dialog, which) -> showCoinSites(which))
                .setNegativeButton(android.R.string.cancel, null)
                .show());
        selectCoinAppsButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.coin_apps_title)
                .setItems(R.array.coin_app_categories, (dialog, which) -> showCoinApps(which))
                .setNegativeButton(android.R.string.cancel, null)
                .show());
        accessibilityButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        notificationButton.setOnClickListener(v -> openNotificationSettings());
        testWarningButton.setOnClickListener(v -> startActivity(new Intent(this, WarningActivity.class)
                .putExtra(WarningActivity.EXTRA_PREVIEW, true)
                .putExtra(WarningPreferences.TITLE, warningTitleInput.getText().toString())
                .putExtra(WarningPreferences.SUBTITLE, warningSubtitleInput.getText().toString())
                .putExtra(WarningPreferences.REASON, warningReasonInput.getText().toString())));

        requestNotificationPermissionIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMonitoringStatus();
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.mainRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets keyboard = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            // Absolute padding prevents accumulation when the keyboard or rotation changes.
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
        boolean night = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), root);
        controller.setAppearanceLightStatusBars(!night);
        controller.setAppearanceLightNavigationBars(!night);
        ViewCompat.requestApplyInsets(root);
    }

    private boolean isMonitoringEnabled() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (manager == null) {
            return false;
        }
        for (AccessibilityServiceInfo info : manager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            android.content.pm.ServiceInfo service = info.getResolveInfo().serviceInfo;
            if (getPackageName().equals(service.packageName)
                    && ForbiddenAccessibilityService.class.getName().equals(service.name)) {
                return true;
            }
        }
        return false;
    }

    private void updateMonitoringStatus() {
        boolean enabled = isMonitoringEnabled();
        monitoringStatus.setText(enabled
                ? R.string.monitoring_enabled : R.string.monitoring_disabled);
        TextView description = findViewById(R.id.monitoringDescription);
        description.setText(enabled ? R.string.monitoring_enabled_help : R.string.monitoring_disabled_help);
    }

    private void saveRules() {
        RuleRepository.saveSearchSettings(this, searchDetectionSwitch.isChecked(),
                searchKeywordsInput.getText().toString());
        WarningPreferences.save(this, warningTitleInput.getText().toString(),
                warningSubtitleInput.getText().toString(), warningReasonInput.getText().toString());
        RuleRepository.saveRules(
                this,
                sitesInput.getText().toString(),
                packagesInput.getText().toString()
        );
        Toast.makeText(this, R.string.rules_saved, Toast.LENGTH_SHORT).show();
        updateMonitoringStatus();
        if (!isMonitoringEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.monitoring_setup_title)
                    .setMessage(R.string.monitoring_setup_message)
                    .setPositiveButton(R.string.open_accessibility_settings, (dialog, which) ->
                            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                    .setNegativeButton(R.string.monitoring_setup_later, null)
                    .show();
        }
    }

    private void showCoinSites(int category) {
        List<CoinSiteCatalog.Site> available = new ArrayList<>();
        String current = sitesInput.getText().toString();
        for (CoinSiteCatalog.Site site : CoinSiteCatalog.sites(category)) {
            if (!CoinSiteCatalog.contains(current, site.domain)) {
                available.add(site);
            }
        }
        if (available.isEmpty()) {
            Toast.makeText(this, R.string.coin_sites_already_added, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] labels = new String[available.size()];
        List<String> values = new ArrayList<>();
        for (int i = 0; i < available.size(); i++) {
            CoinSiteCatalog.Site site = available.get(i);
            labels[i] = site.name + "\n" + site.domain;
            values.add(site.domain);
        }
        showPresetPicker(getResources().getStringArray(R.array.coin_site_categories)[category],
                labels, values, sitesInput, CoinSiteCatalog::append,
                R.string.coin_sites_select_required, R.string.coin_sites_added);
    }

    private void showCoinApps(int category) {
        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();
        String current = packagesInput.getText().toString();
        for (CoinAppCatalog.App app : CoinAppCatalog.apps(category)) {
            if (!CoinAppCatalog.contains(current, app.packageName)) {
                labels.add(app.name);
                values.add(app.packageName);
            }
        }
        if (values.isEmpty()) {
            Toast.makeText(this, R.string.coin_apps_already_added, Toast.LENGTH_SHORT).show();
            return;
        }
        showPresetPicker(getResources().getStringArray(R.array.coin_app_categories)[category],
                labels.toArray(new String[0]), values, packagesInput, CoinAppCatalog::append,
                R.string.coin_apps_select_required, R.string.coin_apps_added);
    }

    private void showPresetPicker(String title, String[] labels, List<String> values,
                                  EditText target, BiFunction<String, List<String>, String> append,
                                  int selectionRequiredMessage, int addedMessage) {
        boolean[] checked = new boolean[labels.length];
        AlertDialog picker = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMultiChoiceItems(labels, checked, (dialog, which, selected) -> checked[which] = selected)
                .setPositiveButton(R.string.coin_sites_add, null)
                .setNeutralButton(R.string.coin_sites_select_all, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        picker.setOnShowListener(dialog -> {
            picker.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                for (int i = 0; i < checked.length; i++) {
                    checked[i] = true;
                    picker.getListView().setItemChecked(i, true);
                }
            });
            picker.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                List<String> selected = new ArrayList<>();
                for (int i = 0; i < checked.length; i++) {
                    if (checked[i]) {
                        selected.add(values.get(i));
                    }
                }
                if (selected.isEmpty()) {
                    Toast.makeText(this, selectionRequiredMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                target.setText(append.apply(target.getText().toString(), selected));
                Toast.makeText(this, addedMessage, Toast.LENGTH_LONG).show();
                picker.dismiss();
            });
        });
        picker.show();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }

    private void openNotificationSettings() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + getPackageName()));
        }
        startActivity(intent);
    }
}
