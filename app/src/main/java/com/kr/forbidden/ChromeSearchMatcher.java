package com.kr.forbidden;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;

/** Only receives Chrome's unfocused address-bar text, never page content. */
final class ChromeSearchMatcher {
    private ChromeSearchMatcher() { }

    static String match(String addressBarText, String keywords) {
        String query = normalize(extractQuery(addressBarText));
        if (query.isEmpty() || keywords == null) {
            return "";
        }
        for (String entry : keywords.split("[,\\n]")) {
            String keyword = normalize(entry);
            if (!keyword.isEmpty() && query.contains(keyword)) {
                return entry.trim();
            }
        }
        return "";
    }

    static String extractQuery(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        String value = text.trim();
        // Chrome may display the committed search terms instead of the search URL.
        // Text containing URL syntax is never treated as a plain search phrase.
        if (!value.matches(".*[.:/\\\\?&#@].*")) {
            return value;
        }
        try {
            URI uri = new URI((value.contains("://") ? value : "https://" + value).replace(" ", "%20"));
            if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
                return "";
            }
            String host = uri.getHost();
            if (host == null || uri.getUserInfo() != null) {
                return "";
            }
            host = host.toLowerCase(Locale.ROOT);
            String path = uri.getPath();
            String parameter;
            if ((host.equals("google.com") || host.equals("www.google.com")
                    || host.equals("google.co.kr") || host.equals("www.google.co.kr"))
                    && "/search".equals(path)) {
                parameter = "q";
            } else if ((host.equals("search.naver.com") || host.equals("m.search.naver.com"))
                    && "/search.naver".equals(path)) {
                parameter = "query";
            } else if ((host.equals("search.daum.net") || host.equals("m.search.daum.net"))
                    && "/search".equals(path)) {
                parameter = "q";
            } else if ((host.equals("bing.com") || host.equals("www.bing.com"))
                    && "/search".equals(path)) {
                parameter = "q";
            } else {
                return "";
            }
            if (uri.getRawQuery() == null) {
                return "";
            }
            StringBuilder query = new StringBuilder();
            for (String part : uri.getRawQuery().split("&")) {
                String[] pair = part.split("=", 2);
                if (pair.length == 2 && parameter.equals(decode(pair[0]))) {
                    query.append(' ').append(decode(pair[1]));
                }
            }
            return query.toString().trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String decode(String text) throws java.io.UnsupportedEncodingException {
        return URLDecoder.decode(text, StandardCharsets.UTF_8.name());
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
