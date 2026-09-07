package com.kr.forbidden;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class CoinSiteCatalogTest {
    @Test
    public void appendPreservesManualRulesAndSkipsEquivalentUrls() {
        String original = "custom.example, HTTPS://WWW.UPBIT.COM/trade\nmanual.example";
        assertEquals(original + "\nbithumb.com", CoinSiteCatalog.append(original,
                Arrays.asList("upbit.com", "bithumb.com", "bithumb.com")));
    }

    @Test
    public void similarDomainsAndSubdomainOnlyRulesDoNotHideRootPreset() {
        assertFalse(CoinSiteCatalog.contains("fakeupbit.com\nupbit.com.example\nm.upbit.com", "upbit.com"));
    }

    @Test
    public void emptySelectionLeavesDraftUntouched() {
        String original = "  custom.example,\n";
        assertEquals(original, CoinSiteCatalog.append(original, Collections.emptyList()));
        assertEquals("upbit.com", CoinSiteCatalog.append("", Collections.singletonList("upbit.com")));
    }
}
