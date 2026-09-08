package com.kr.forbidden;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AccessibilityConsentTest {
    private Context context;

    @Before
    public void prepare() {
        // Separate test preferences: never modify the installed app user's consent.
        context = new ContextWrapper(InstrumentationRegistry.getInstrumentation().getTargetContext()) {
            @Override
            public SharedPreferences getSharedPreferences(String name, int mode) {
                return super.getSharedPreferences("test_only_" + name, mode);
            }
        };
        AccessibilityConsent.revoke(context);
    }

    @After
    public void cleanUp() {
        AccessibilityConsent.revoke(context);
    }

    @Test
    public void noConsentUntilExplicitGrantAndRevocationStopsConsent() {
        assertFalse(AccessibilityConsent.isGranted(context));
        AccessibilityConsent.grant(context);
        assertTrue(AccessibilityConsent.isGranted(context));
        AccessibilityConsent.revoke(context);
        assertFalse(AccessibilityConsent.isGranted(context));
    }

    @Test
    public void otherDisclosureVersionDoesNotAuthorizeCurrentDataUse() {
        context.getSharedPreferences("accessibility_consent", Context.MODE_PRIVATE)
                .edit().putInt("accepted_version", -1).commit();
        assertFalse(AccessibilityConsent.isGranted(context));
    }
}
