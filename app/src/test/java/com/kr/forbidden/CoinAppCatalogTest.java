package com.kr.forbidden;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class CoinAppCatalogTest {
    @Test
    public void appendPreservesDraftAndSkipsDuplicatesAcrossSeparators() {
        String original = "custom.app, COM.BINANCE.DEV\r\n live.coinness ";
        assertEquals(original + "\ncom.dunamu.exchange", CoinAppCatalog.append(original,
                Arrays.asList("com.binance.dev", "live.coinness", "com.dunamu.exchange", "com.dunamu.exchange")));
    }

    @Test
    public void packageMatchingIsExactNotDomainMatching() {
        assertFalse(CoinAppCatalog.contains("com.binance.dev.beta\nwww.com.binance.dev", "com.binance.dev"));
        assertFalse(CoinAppCatalog.contains("https://live.coinness/path", "live.coinness"));
    }

    @Test
    public void emptySelectionDoesNotChangeDraft() {
        assertEquals("custom.app,\n", CoinAppCatalog.append("custom.app,\n", Collections.emptyList()));
        assertEquals("live.coinness", CoinAppCatalog.append("", Collections.singletonList("live.coinness")));
    }
}
