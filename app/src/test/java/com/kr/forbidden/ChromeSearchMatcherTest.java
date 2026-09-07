package com.kr.forbidden;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChromeSearchMatcherTest {
    @Test
    public void matchesEncodedKoreanGoogleQuery() {
        assertEquals("코인", ChromeSearchMatcher.match(
                "https://www.google.com/search?q=%EB%B9%84%ED%8A%B8%EC%BD%94%EC%9D%B8+%EC%8B%9C%EC%84%B8", "코인\nbinance"));
    }

    @Test
    public void supportsMobileAndDesktopSearchEndpoints() {
        String[] addresses = {
                "www.google.co.kr/search?q=binance",
                "https://search.naver.com/search.naver?where=nexearch&query=binance",
                "https://m.search.naver.com/search.naver?query=binance",
                "https://search.daum.net/search?w=tot&q=binance",
                "https://m.search.daum.net/search?q=binance",
                "https://www.bing.com/search?q=binance"
        };
        for (String address : addresses) {
            assertEquals(address, "binance", ChromeSearchMatcher.match(address, "binance"));
        }
    }

    @Test
    public void doesNotScanArbitraryUrlsOrNonSearchParameters() {
        String[] addresses = {
                "https://example.com/?q=binance",
                "https://www.google.com/search?q=weather&source=binance",
                "https://www.google.com/search?q=weather#binance",
                "https://www.google.com/maps?q=binance",
                "https://www.google.com.evil.example/search?q=binance",
                "https://www.google.com@evil.example/search?q=binance",
                "https://binance.com", "binance.com", "chrome://binance",
                "https://www.google.com/search?q=%ZZ"
        };
        for (String address : addresses) {
            assertEquals(address, "", ChromeSearchMatcher.match(address, "binance"));
        }
    }

    @Test
    public void handlesCommittedPlainTermsCaseAndUnicodeNormalization() {
        assertEquals("binance", ChromeSearchMatcher.match("ＢＩＮＡＮＣＥ", "binance"));
        assertEquals("코인 선물", ChromeSearchMatcher.match("코인   선물 거래", "코인 선물"));
        assertEquals("코인", ChromeSearchMatcher.match("비트코인 시세", "코인"));
    }

    @Test
    public void preservesEncodedSeparatorsAndChecksRepeatedQueryValues() {
        assertEquals("a&b", ChromeSearchMatcher.extractQuery("https://www.google.com/search?q=a%26b"));
        assertEquals("binance", ChromeSearchMatcher.match(
                "https://www.google.com/search?q=weather&q=BINANCE", "binance"));
    }

    @Test
    public void handlesEmptyRulesAndUnrelatedQueries() {
        assertEquals("", ChromeSearchMatcher.match("코인", " , \n "));
        assertEquals("", ChromeSearchMatcher.match("날씨", "코인,binance"));
        assertEquals("", ChromeSearchMatcher.match(null, "코인"));
        assertEquals("", ChromeSearchMatcher.match("코인", null));
    }
}
