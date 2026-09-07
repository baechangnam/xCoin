package com.kr.forbidden;

import java.util.List;
import java.util.Locale;

/** Package IDs verified against the apps' Google Play listings. */
final class CoinAppCatalog {
    static final class App {
        final String name;
        final String packageName;

        App(String name, String packageName) {
            this.name = name;
            this.packageName = packageName;
        }
    }

    private CoinAppCatalog() { }

    static App[] apps(int category) {
        switch (category) {
            case 0:
                return new App[]{
                        new App("업비트", "com.dunamu.exchange"),
                        new App("빗썸", "com.btckorea.bithumb"),
                        new App("코인원", "coinone.co.kr.official")
                };
            case 1:
                return new App[]{
                        new App("바이낸스 (Binance)", "com.binance.dev"),
                        new App("바이비트 (Bybit)", "com.bybit.app")
                };
            case 2:
                return new App[]{
                        new App("코인니스", "live.coinness"),
                        new App("코인판", "com.coinpan.coinpan")
                };
            default:
                throw new IllegalArgumentException("Unknown app category: " + category);
        }
    }

    static boolean contains(String text, String packageName) {
        for (String entry : text.split("[,\\n]")) {
            if (entry.trim().toLowerCase(Locale.ROOT).equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    static String append(String original, List<String> packages) {
        String result = original;
        for (String packageName : packages) {
            if (!contains(result, packageName)) {
                if (!result.isEmpty() && !result.endsWith("\n")) {
                    result += "\n";
                }
                result += packageName;
            }
        }
        return result;
    }
}
