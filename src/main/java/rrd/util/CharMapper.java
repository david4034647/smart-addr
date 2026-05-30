/*
 * Decompiled with CFR 0.152.
 */
package rrd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CharMapper {
    private static final char[] digitExtChars = new char[]{'\u2160', '\u2161', '\u2162', '\u2163', '\u2164', '\u2165', '\u2166', '\u2167', '\u2168', '\u2169', '\u216a', '\u216b', '\u2170', '\u2171', '\u2172', '\u2173', '\u2174', '\u2175', '\u2176', '\u2177', '\u2178', '\u2179', '\u2460', '\u2461', '\u2462', '\u2463', '\u2464', '\u2465', '\u2466', '\u2467', '\u2468', '\u2469', '\u2474', '\u2475', '\u2476', '\u2477', '\u2478', '\u2479', '\u247a', '\u247b', '\u247c', '\u247d', '\u247e', '\u247f', '\u2480', '\u2481', '\u2482', '\u2483', '\u2484', '\u2485', '\u2486', '\u2487', '\u2488', '\u2489', '\u248a', '\u248b', '\u248c', '\u248d', '\u248e', '\u248f', '\u2490', '\u2491', '\u2492', '\u2493', '\u2494', '\u2495', '\u2496', '\u2497', '\u2498', '\u2499', '\u249a', '\u249b', '\u3220', '\u3221', '\u3222', '\u3223', '\u3224', '\u3225', '\u3226', '\u3227', '\u3228', '\u3229'};
    private static final char[] charMapper = new char[65535];
    private static final CharType[] charType = new CharType[65535];

    private static void fillCharType() {
        for (char c = '\u0000'; c < charType.length; c = (char)(c + '\u0001')) {
            CharMapper.charType[c] = CharMapper.isDot(c) ? CharType.Dot : (CharMapper.isDigit(c) ? CharType.Digit : (CharMapper.isEnglish(c) ? CharType.English : (CharMapper.isChinese(c) ? CharType.Chinese : (CharMapper.isJapanese(c) ? CharType.Japanese : CharType.Other))));
        }
        for (char c : digitExtChars) {
            CharMapper.charType[c] = CharType.DigitExt;
        }
    }

    private static void fillCharMapper() {
        for (int c = 0; c < charType.length; c = (int)((char)(c + 1))) {
            CharMapper.charMapper[c] = (char) c;
        }
        CharMapper.loadMapper(CharMapper.class.getResourceAsStream("/charmap.txt"), charMapper);
    }

    private static void loadMapper(InputStream is, char[] mapper) {
        if (is == null) {
            return;
        }
        try {
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String readline = null;
            while ((readline = br.readLine()) != null) {
                if (readline.length() != 2) continue;
                mapper[readline.charAt((int)0)] = readline.charAt(1);
            }
            br.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public static char toDBCCase(char c) {
        if (c == '\u3000') {
            return ' ';
        }
        if (c >= '\uff01' && c <= '\uff5e') {
            return (char)(c - 65248);
        }
        return c;
    }

    public static CharType getType(char c) {
        return charType[c];
    }

    public static char simp2trad(char c) {
        return charMapper[c];
    }

    public static char toEnPun(char c) {
        return charMapper[c];
    }

    public static char toLowerCase(char c) {
        if (c >= 'A' && c <= 'Z') {
            return (char)(c + 32);
        }
        return c;
    }

    public static char toArab(char c) {
        return charMapper[c];
    }

    public static boolean isDot(char c) {
        if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION) {
            return true;
        }
        if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.SPACING_MODIFIER_LETTERS) {
            return true;
        }
        return !Character.isLetterOrDigit(c);
    }

    public static boolean isDigit(char c) {
        return c <= '9' && c >= '0';
    }

    public static boolean isEnglish(char c) {
        return c <= 'Z' && c >= 'A' || c <= 'z' && c >= 'a';
    }

    public static boolean isJapanese(char c) {
        return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HIRAGANA || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.BOPOMOFO;
    }

    public static boolean isChinese(char c) {
        return Character.isLetterOrDigit(c) && Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS;
    }

    static {
        try {
            CharMapper.fillCharType();
            CharMapper.fillCharMapper();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static enum CharType {
        Other,
        Dot,
        Digit,
        DigitExt,
        English,
        Chinese,
        Japanese;

    }
}
