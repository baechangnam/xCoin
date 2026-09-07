package com.kr.forbidden;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class WarningActivity extends AppCompatActivity {
    static final String EXTRA_PREVIEW = "warning_preview";
    private boolean preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_warning);
        View root = findViewById(R.id.warningRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightNavigationBars(false);
        ViewCompat.requestApplyInsets(root);
        findViewById(R.id.homeButton).setOnClickListener(v -> goHome());
        findViewById(R.id.closeButton).setOnClickListener(v -> finish());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (preview) {
                    finish();
                } else {
                    goHome();
                }
            }
        });
        bindWarning();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        bindWarning();
    }

    private void bindWarning() {
        Intent intent = getIntent();
        preview = intent.getBooleanExtra(EXTRA_PREVIEW, false);
        String title = preview
                ? WarningPreferences.orDefault(intent.getStringExtra(WarningPreferences.TITLE),
                        getString(R.string.warning_default_title))
                : WarningPreferences.title(this);
        String subtitle = preview
                ? WarningPreferences.orDefault(intent.getStringExtra(WarningPreferences.SUBTITLE),
                        getString(R.string.warning_default_subtitle))
                : WarningPreferences.subtitle(this);
        String reason = preview ? intent.getStringExtra(WarningPreferences.REASON) : WarningPreferences.reason(this);
        ((TextView) findViewById(R.id.warningTitle)).setText(title);
        ((TextView) findViewById(R.id.warningMessage)).setText(subtitle);
        ((TextView) findViewById(R.id.warningBadge)).setText(
                preview ? R.string.warning_preview_badge : R.string.warning_stop_badge);
        boolean hasReason = reason != null && !reason.trim().isEmpty();
        findViewById(R.id.warningReasonCard).setVisibility(hasReason ? View.VISIBLE : View.GONE);
        ((TextView) findViewById(R.id.warningReason)).setText(hasReason ? reason.trim() : "");
        String context = preview ? getString(R.string.warning_preview_help)
                : intent.getStringExtra(AlertDispatcher.EXTRA_MESSAGE);
        TextView contextView = findViewById(R.id.warningContext);
        contextView.setText(context);
        contextView.setVisibility(context == null || context.isEmpty() ? View.GONE : View.VISIBLE);
        findViewById(R.id.closeButton).setVisibility(preview ? View.VISIBLE : View.GONE);
    }

    private void goHome() {
        startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
}

