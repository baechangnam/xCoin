package com.kr.forbidden;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AccessibilityConsentDialog extends DialogFragment {
    static final String TAG = "accessibility_disclosure";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.consent_title)
                .setMessage(R.string.consent_message)
                .setPositiveButton(R.string.consent_accept, (dialog, which) -> {
                    AccessibilityConsent.grant(requireContext());
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                })
                .setNegativeButton(R.string.monitoring_setup_later, (dialog, which) -> {
                    // Dismissal is not consent. Settings and preview remain available.
                })
                .create();
    }
}
