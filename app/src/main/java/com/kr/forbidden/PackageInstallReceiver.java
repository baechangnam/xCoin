package com.kr.forbidden;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Set;

public class PackageInstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            return;
        }

        Uri data = intent.getData();
        if (data == null) {
            return;
        }

        String packageName = data.getSchemeSpecificPart();
        if (packageName == null || packageName.isEmpty()) {
            return;
        }

        Set<String> blockedPackages = RuleRepository.getBlockedPackages(context);
        if (!blockedPackages.contains(packageName.toLowerCase())) {
            return;
        }

        AlertDispatcher.showAlert(
                context,
                context.getString(R.string.package_blocked_title),
                context.getString(R.string.package_blocked_message, packageName)
        );
    }
}
