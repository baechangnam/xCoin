package com.kr.forbidden;

import java.util.List;
import java.util.Locale;

/** Bundled website presets; adding a website does not add an Android app package. */
final class CoinSiteCatalog {
    static final class Site {
        final String name;
        final String domain;

        Site(String name, String domain) {
            this.name = name;
            this.domain = domain;
        }
    }

    private CoinSiteCatalog() { }

    static Site[] sites(int category) {
        switch (category) {
            case 0:
                return new Site[]{
                        new Site("업비트", "upbit.com"),
                        new Site("빗썸", "bithumb.com"),
                        new Site("코인원", "coinone.co.kr"),
                        new Site("코빗", "korbit.co.kr"),
                        new Site("고팍스", "gopax.co.kr")
                };
            case 1:
                return new Site[]{
                        new Site("바이낸스 (Binance)", "binance.com"),
                        new Site("바이비트 (Bybit)", "bybit.com"),
                        new Site("OKX", "okx.com"),
                        new Site("비트겟 (Bitget)", "bitget.com"),
                        new Site("코인베이스 (Coinbase)", "coinbase.com"),
                        new Site("크라켄 (Kraken)", "kraken.com")
                };
            case 2:
                return new Site[]{
                        new Site("코인마켓캡 (CoinMarketCap)", "coinmarketcap.com"),
                        new Site("코인게코 (CoinGecko)", "coingecko.com")
                };
            case 3:
                return new Site[]{
                        new Site("코인판", "coinpan.com")
                };
            default:
                throw new IllegalArgumentException("Unknown site category: " + category);
        }
    }

    static boolean contains(String text, String domain) {
        for (String rule : text.split("[,\\n]")) {
            // Keep equivalent-rule detection consistent with the accessibility service.
            String normalized = rule.trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith("https://")) {
                normalized = normalized.substring(8);
            } else if (normalized.startsWith("http://")) {
                normalized = normalized.substring(7);
            }
            int slash = normalized.indexOf('/');
            if (slash >= 0) {
                normalized = normalized.substring(0, slash);
            }
            if (normalized.startsWith("www.")) {
                normalized = normalized.substring(4);
            }
            if (normalized.equals(domain)) {
                return true;
            }
        }
        return false;
    }

    static String append(String original, List<String> domains) {
        String result = original;
        for (String domain : domains) {
            if (!contains(result, domain)) {
                if (!result.isEmpty() && !result.endsWith("\n")) {
                    result += "\n";
                }
                result += domain;
            }
        }
        return result;
    }
}
