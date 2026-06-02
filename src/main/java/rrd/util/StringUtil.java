/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package rrd.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import rrd.util.CharMapper;

public class StringUtil {
    private static Logger logger = Logger.getLogger(StringUtil.class);
    private static String dateFormat = "yyyyMMdd";
    private static SimpleDateFormat format = new SimpleDateFormat(dateFormat);
    private static final Set<String> suffName = new HashSet<String>(Arrays.asList("\u8d75", "\u94b1", "\u5b59", "\u674e", "\u5468", "\u5434", "\u90d1", "\u738b", "\u51af", "\u9648", "\u696e", "\u536b", "\u848b", "\u6c88", "\u97e9", "\u6768", "\u6731", "\u79e6", "\u5c24", "\u8bb8", "\u4f55", "\u5415", "\u65bd", "\u5f20", "\u5b54", "\u66f9", "\u4e25", "\u534e", "\u91d1", "\u9b4f", "\u9676", "\u59dc", "\u621a", "\u8c22", "\u90b9", "\u55bb", "\u67cf", "\u6c34", "\u7aa6", "\u7ae0", "\u4e91", "\u82cf", "\u6f58", "\u845b", "\u595a", "\u8303", "\u5f6d", "\u90ce", "\u9c81", "\u97e6", "\u660c", "\u9a6c", "\u82d7", "\u51e4", "\u82b1", "\u65b9", "\u4fde", "\u4efb", "\u8881", "\u67f3", "\u9146", "\u9c8d", "\u53f2", "\u5510", "\u8d39", "\u5ec9", "\u5c91", "\u859b", "\u96f7", "\u8d3a", "\u502a", "\u6c64", "\u6ed5", "\u6bb7", "\u7f57", "\u6bd5", "\u90dd", "\u90ac", "\u5b89", "\u5e38", "\u4e50", "\u4e8e", "\u65f6", "\u5085", "\u76ae", "\u535e", "\u9f50", "\u5eb7", "\u4f0d", "\u4f59", "\u5143", "\u535c", "\u987e", "\u5b5f", "\u5e73", "\u9ec4", "\u548c", "\u7a46", "\u8427", "\u5c39", "\u59da", "\u90b5", "\u6e5b", "\u6c6a", "\u7941", "\u6bdb", "\u79b9", "\u72c4", "\u7c73", "\u8d1d", "\u660e", "\u81e7", "\u8ba1", "\u4f0f", "\u6210", "\u6234", "\u8c08", "\u5b8b", "\u8305", "\u5e9e", "\u718a", "\u7eaa", "\u8212", "\u5c48", "\u9879", "\u795d", "\u8463", "\u6881", "\u675c", "\u962e", "\u84dd", "\u95fd", "\u5e2d", "\u5b63", "\u9ebb", "\u5f3a", "\u8d3e", "\u8def", "\u5a04", "\u5371", "\u6c5f", "\u7ae5", "\u989c", "\u90ed", "\u6885", "\u76db", "\u6797", "\u5201", "\u953a", "\u5f90", "\u4e18", "\u9a86", "\u9ad8", "\u590f", "\u8521", "\u7530", "\u6a0a", "\u80e1", "\u51cc", "\u970d", "\u865e", "\u4e07", "\u652f", "\u67ef", "\u661d", "\u7ba1", "\u5362", "\u83ab", "\u7ecf", "\u623f", "\u88d8", "\u7f2a", "\u5e72", "\u89e3", "\u5e94", "\u5b97", "\u4e01", "\u5ba3", "\u8d32", "\u9093", "\u90c1", "\u5355", "\u676d", "\u6d2a", "\u5305", "\u8bf8", "\u5de6", "\u77f3", "\u5d14", "\u5409", "\u94ae", "\u9f9a", "\u7a0b", "\u5d47", "\u90a2", "\u6ed1", "\u88f4", "\u9646", "\u8363", "\u7fc1", "\u8340", "\u7f8a", "\u65bc", "\u60e0", "\u7504", "\u9eb9", "\u5bb6", "\u5c01", "\u82ae", "\u7fbf", "\u50a8", "\u9773", "\u6c72", "\u90b4", "\u7cdc", "\u677e", "\u4e95", "\u6bb5", "\u5bcc", "\u5deb", "\u4e4c", "\u7126", "\u5df4", "\u5f13", "\u7267", "\u9697", "\u5c71", "\u8c37", "\u8f66", "\u4faf", "\u5b93", "\u84ec", "\u5168", "\u90d7", "\u73ed", "\u4ef0", "\u79cb", "\u4ef2", "\u4f0a", "\u5bab", "\u5b81", "\u4ec7", "\u683e", "\u66b4", "\u7518", "\u659c", "\u5389", "\u620e", "\u7956", "\u6b66", "\u7b26", "\u5218", "\u666f", "\u8a79", "\u675f", "\u9f99", "\u53f6", "\u5e78", "\u53f8", "\u97f6", "\u90dc", "\u9ece", "\u84df", "\u8584", "\u5370", "\u5bbf", "\u767d", "\u6000", "\u84b2", "\u90b0", "\u4ece", "\u9102", "\u7d22", "\u54b8", "\u7c4d", "\u8d56", "\u5353", "\u853a", "\u5c60", "\u8499", "\u6c60", "\u4e54", "\u9634", "\u90c1", "\u80e5", "\u80fd", "\u82cd", "\u53cc", "\u95fb", "\u8398", "\u515a", "\u7fdf", "\u8c2d", "\u8d21", "\u52b3", "\u9004", "\u59ec", "\u7533", "\u6276", "\u5835", "\u5189", "\u5bb0", "\u90e6", "\u96cd", "\u90e4", "\u74a9", "\u6851", "\u6842", "\u6fee", "\u725b", "\u5bff", "\u901a", "\u8fb9", "\u6248", "\u71d5", "\u5180", "\u90cf", "\u6d66", "\u5c1a", "\u519c", "\u6e29", "\u522b", "\u5e84", "\u664f", "\u67f4", "\u77bf", "\u960e", "\u5145", "\u6155", "\u8fde", "\u8339", "\u4e60", "\u5ba6", "\u827e", "\u9c7c", "\u5bb9", "\u5411", "\u53e4", "\u6613", "\u614e", "\u6208", "\u5ed6", "\u5ebe", "\u7ec8", "\u66a8", "\u5c45", "\u8861", "\u6b65", "\u90fd", "\u803f", "\u6ee1", "\u5f18", "\u5321", "\u56fd", "\u6587", "\u5bc7", "\u5e7f", "\u7984", "\u9619", "\u4e1c", "\u6b27", "\u6bb3", "\u6c83", "\u5229", "\u851a", "\u8d8a", "\u5914", "\u9686", "\u5e08", "\u5de9", "\u538d", "\u8042", "\u6641", "\u52fe", "\u6556", "\u878d", "\u51b7", "\u8a3e", "\u8f9b", "\u961a", "\u90a3", "\u7b80", "\u9976", "\u7a7a", "\u66fe", "\u6bcb", "\u6c99", "\u4e5c", "\u517b", "\u97a0", "\u987b", "\u4e30", "\u5de2", "\u5173", "\u84af", "\u76f8", "\u67e5", "\u540e", "\u8346", "\u7ea2", "\u6e38", "\u7afa", "\u6743", "\u9011", "\u76d6", "\u76ca", "\u6853", "\u516c", "\u4e07\u4fdf", "\u53f8\u9a6c", "\u4e0a\u5b98", "\u6b27\u9633", "\u590f\u4faf", "\u8bf8\u845b", "\u95fb\u4eba", "\u4e1c\u65b9", "\u8d6b\u8fde", "\u7687\u752b", "\u5c09\u8fdf", "\u516c\u7f8a", "\u6fb9\u53f0", "\u516c\u51b6", "\u5b97\u653f", "\u6fee\u9633", "\u6df3\u4e8e", "\u5355\u4e8e", "\u592a\u53d4", "\u7533\u5c60", "\u516c\u5b59", "\u4ef2\u5b59", "\u8f69\u8f95", "\u4ee4\u72d0", "\u953a\u79bb", "\u5b87\u6587", "\u957f\u5b59", "\u6155\u5bb9", "\u9c9c\u4e8e", "\u95fe\u4e18", "\u53f8\u5f92", "\u53f8\u7a7a", "\u4e0c\u5b98", "\u53f8\u5bc7", "\u4ec9", "\u7763", "\u5b50\u8f66", "\u989b\u5b59", "\u7aef\u6728", "\u5deb\u9a6c", "\u516c\u897f", "\u6f06\u96d5", "\u4e50\u6b63", "\u58e4\u9a77", "\u516c\u826f", "\u62d3\u62d4", "\u5939\u8c37", "\u5bb0\u7236", "\u8c37\u6881", "\u664b", "\u695a", "\u960e", "\u6cd5", "\u6c5d", "\u9122", "\u6d82", "\u94a6", "\u6bb5\u5e72", "\u767e\u91cc", "\u4e1c\u90ed", "\u5357\u95e8", "\u547c\u5ef6", "\u5f52", "\u6d77", "\u7f8a\u820c", "\u5fae\u751f", "\u5cb3", "\u5e05", "\u7f11", "\u4ea2", "\u51b5", "\u540e", "\u6709", "\u7434", "\u6881\u4e18", "\u5de6\u4e18", "\u4e1c\u95e8", "\u897f\u95e8", "\u5546", "\u725f", "\u4f58", "\u4f74", "\u4f2f", "\u8d4f", "\u5357\u5bab", "\u58a8", "\u54c8", "\u8c2f", "\u7b2a", "\u5e74", "\u7231", "\u9633", "\u4f5f", "\u7b2c\u4e94", "\u8a00", "\u4ee3", "\u798f"));

    public static boolean isNumberic(String str) {
        Pattern pattern = Pattern.compile("[0-9]{1,}");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean isDotString(String input) {
        if (input == null) {
            return false;
        }
        String trimInput = input.trim();
        for (int i = 0; i < trimInput.length(); ++i) {
            char c = trimInput.charAt(i);
            if (CharMapper.isDot(c)) continue;
            return false;
        }
        return true;
    }

    public static boolean containJapanese(String input) {
        if (input == null) {
            return false;
        }
        String trimInput = input.trim();
        for (int i = 0; i < trimInput.length(); ++i) {
            char c = trimInput.charAt(i);
            if (CharMapper.getType(c) != CharMapper.CharType.Japanese) continue;
            return true;
        }
        return false;
    }

    public static String removeFrontNum(String input) {
        char c;
        if (input == null) {
            return null;
        }
        int length = input.length();
        int index = 1;
        for (int i = 0; i < input.length() && CharMapper.getType(c = input.charAt(i)) == CharMapper.CharType.Digit; ++i) {
            ++index;
        }
        return input.substring(index, length);
    }

    public static String removeBackNum(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(input);
        String result = StringUtil.removeFrontNum(sb.reverse().toString());
        return new StringBuilder(result).reverse().toString();
    }

    public static int[] validateSubString(String input) {
        char c;
        char c2;
        if (input == null) {
            return null;
        }
        int[] pos = new int[2];
        int length = input.length();
        for (int i = 0; i < length && CharMapper.getType(c2 = input.charAt(i)) == CharMapper.CharType.Digit; ++i) {
            pos[0] = pos[0] + 1;
        }
        StringBuilder sb = new StringBuilder(input);
        input = sb.reverse().toString();
        for (int i = 0; i < length && CharMapper.getType(c = input.charAt(i)) == CharMapper.CharType.Digit; ++i) {
            pos[1] = pos[1] + 1;
        }
        pos[1] = length - pos[1];
        return pos;
    }

    public static String toNormal(String input) {
        if (input == null) {
            return null;
        }
        String trimInput = input.trim();
        StringBuilder sb = new StringBuilder(trimInput.length());
        for (int i = 0; i < trimInput.length(); ++i) {
            char pre;
            char c = trimInput.charAt(i);
            if (CharMapper.getType(c) == CharMapper.CharType.Dot) {
                if (i <= 0 || CharMapper.getType(pre = trimInput.charAt(i - 1)) == CharMapper.CharType.Dot) continue;
                sb.append(":");
                continue;
            }
            if (CharMapper.getType(c) == CharMapper.CharType.English) {
                char _c = CharMapper.toLowerCase(c);
                if (i > 0) {
                    pre = trimInput.charAt(i - 1);
                    if (CharMapper.getType(pre) == CharMapper.CharType.Chinese) {
                        sb.append(":");
                        sb.append(_c);
                        continue;
                    }
                    sb.append(_c);
                    continue;
                }
                sb.append(_c);
                continue;
            }
            if (CharMapper.getType(c) == CharMapper.CharType.Chinese) {
                if (i > 0) {
                    pre = trimInput.charAt(i - 1);
                    if (CharMapper.getType(pre) == CharMapper.CharType.English) {
                        sb.append(":");
                        sb.append(CharMapper.simp2trad(c));
                        continue;
                    }
                    sb.append(CharMapper.simp2trad(c));
                    continue;
                }
                sb.append(CharMapper.simp2trad(c));
                continue;
            }
            if (CharMapper.getType(c) == CharMapper.CharType.DigitExt) {
                sb.append(CharMapper.toArab(c));
                continue;
            }
            sb.append(CharMapper.toDBCCase(c));
        }
        return sb.toString();
    }

    public static String toShow(String input) {
        if (input == null) {
            return null;
        }
        String trimInput = input.trim();
        StringBuilder sb = new StringBuilder(trimInput.length());
        for (int i = 0; i < trimInput.length(); ++i) {
            char c = trimInput.charAt(i);
            if (CharMapper.getType(c) == CharMapper.CharType.Dot) {
                if (i > 0) {
                    char pre = trimInput.charAt(i - 1);
                    if (CharMapper.getType(pre) == CharMapper.CharType.Dot) continue;
                    sb.append(CharMapper.toEnPun(c));
                    continue;
                }
                sb.append(CharMapper.toEnPun(c));
                continue;
            }
            if (CharMapper.getType(c) == CharMapper.CharType.Chinese) {
                sb.append(CharMapper.simp2trad(c));
                continue;
            }
            if (CharMapper.getType(c) == CharMapper.CharType.DigitExt) {
                sb.append(CharMapper.toArab(c));
                continue;
            }
            sb.append(CharMapper.toDBCCase(c));
        }
        return sb.toString();
    }

    public static String toChinese(String input) {
        if (input == null) {
            return null;
        }
        String trimInput = input.trim();
        StringBuilder sb = new StringBuilder(trimInput.length());
        for (int i = 0; i < trimInput.length(); ++i) {
            char c = trimInput.charAt(i);
            if (!CharMapper.isChinese(c)) continue;
            sb.append(c);
        }
        return sb.toString();
    }

    public static String toSegWordsStand(String input) {
        if (input == null) {
            return null;
        }
        String trimInput = input.trim();
        StringBuilder sb = new StringBuilder(trimInput.length());
        for (int i = 0; i < trimInput.length(); ++i) {
            char c = trimInput.charAt(i);
            if (c == ' ') {
                sb.append(' ');
                continue;
            }
            if (CharMapper.getType(c) == CharMapper.CharType.Dot) {
                if (c == '#') {
                    sb.append(c);
                    continue;
                }
                sb.append('\u90ae');
                sb.append('\u7f16');
                continue;
            }
            if (CharMapper.getType(c) == CharMapper.CharType.Chinese) {
                sb.append(CharMapper.simp2trad(c));
                continue;
            }
            if (CharMapper.getType(c) == CharMapper.CharType.DigitExt) {
                sb.append(CharMapper.toArab(c));
                continue;
            }
            sb.append(CharMapper.toDBCCase(c));
        }
        return sb.toString();
    }

    public static boolean isName(String s) {
        if (s == null || s.length() <= 1) {
            return false;
        }
        return suffName.contains(s.substring(0, 1)) && s.length() <= 4;
    }

    /**
     * 严格校验人名：在 isName 基础上排除含数字或地址关键词的误匹配（如"昌平路东"）
     */
    public static boolean isNameStrict(String s) {
        if (!isName(s)) {
            return false;
        }
        // 不含数字
        for (int i = 0; i < s.length(); i++) {
            if (CharMapper.isDigit(s.charAt(i))) {
                return false;
            }
        }
        // 不含常见地址/动词关键词
        String[] invalid = {"路", "街", "号", "楼", "室", "区", "县", "市", "省", "镇", "乡", "村", "巷", "弄", "栋", "单元", "层", "收", "寄", "转", "到", "达", "送", "手", "机"};
        for (String kw : invalid) {
            if (s.contains(kw)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isZipCode(String s) {
        if (s == null || s.length() <= 1) {
            return false;
        }
        return s.length() == 6;
    }

    public static boolean isPhone(String s) {
        if (s == null || s.length() <= 1) {
            return false;
        }
        return s.startsWith("1") && s.length() == 11;
    }

    public static String toStandard(String input) {
        if (input == null) {
            return null;
        }
        String trimInput = input.trim();
        StringBuilder sb = new StringBuilder(trimInput.length());
        for (int i = 0; i < trimInput.length(); ++i) {
            char c = trimInput.charAt(i);
            if (CharMapper.getType(c) == CharMapper.CharType.Dot) {
                sb.append(CharMapper.toEnPun(c));
                continue;
            }
            if (CharMapper.getType(c) == CharMapper.CharType.Chinese) {
                sb.append(CharMapper.simp2trad(c));
                continue;
            }
            if (CharMapper.getType(c) == CharMapper.CharType.DigitExt) {
                sb.append(CharMapper.toArab(c));
                continue;
            }
            sb.append(CharMapper.toDBCCase(c));
        }
        return sb.toString();
    }

    public static String removeDots(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (CharMapper.getType(c) == CharMapper.CharType.Dot) continue;
            sb.append(c);
        }
        return sb.toString();
    }

    public static String simp2Trad(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            sb.append(CharMapper.simp2trad(c));
        }
        return sb.toString();
    }

    public static boolean isENString(String str) {
        Pattern pattern = Pattern.compile("[a-zA-Z]{1,}");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static String filterStr(String str) {
        Pattern pattern = Pattern.compile("\u9365\u70b2\ue632@([^:]+?):|\\[\u675e\ue100\u5f42\\]|@([^:]+?):");
        Matcher matcher = pattern.matcher(str);
        String s = matcher.replaceAll("");
        return s;
    }

    public static boolean isNumbericAndEn(String str) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]{1,}");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean isNotChDiEnStr(String input) {
        if (input == null) {
            return false;
        }
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (CharMapper.isChinese(c) || CharMapper.isDigit(c) || CharMapper.isEnglish(c)) continue;
            return true;
        }
        return false;
    }

    public static String getTrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        StringBuffer buffer = stringWriter.getBuffer();
        stringWriter = null;
        writer = null;
        return buffer.toString();
    }

    public static List<String> getDateList(String date1, String date2) {
        String tmp;
        ArrayList<String> dateList = new ArrayList<String>(1);
        if (date1.equals(date2)) {
            logger.info((Object)"\u6d93\u3084\u91dc\u93c3\u30e6\u6e61\u9429\u54e5\u74d1!");
            dateList.add(date1);
            return dateList;
        }
        if (date1.compareTo(date2) > 0) {
            tmp = date1;
            date1 = date2;
            date2 = tmp;
        }
        tmp = format.format(StringUtil.str2Date(date1).getTime() + 86400000L);
        int num = 0;
        while (tmp.compareTo(date2) < 0) {
            dateList.add(tmp);
            ++num;
            tmp = format.format(StringUtil.str2Date(tmp).getTime() + 86400000L);
        }
        if (num == 0) {
            dateList.add(date1);
            dateList.add(date2);
            return dateList;
        }
        return dateList;
    }

    private static Date str2Date(String str) {
        if (str == null) {
            return null;
        }
        try {
            return format.parse(str);
        }
        catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String subDateByNum(String date, int num) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        Date d = null;
        Calendar cal = null;
        try {
            d = df.parse(date);
            cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(5, -num);
        }
        catch (Exception e) {
            logger.info((Object)StringUtil.getTrace(e));
        }
        return df.format(cal.getTime());
    }

    public static float similarity(String A, String B) {
        return (StringUtil.sc(A, B) + StringUtil.sc(B, A)) / 2.0f;
    }

    private static float sc(String A, String B) {
        float sc = 0.0f;
        for (int i = 0; i < A.length(); ++i) {
            sc += StringUtil.cc(A, i, B);
        }
        return sc /= (float)A.length();
    }

    private static float cc(String A, int i, String B) {
        return (float)(B.length() - StringUtil.posOffset(A, i, B)) / (float)B.length();
    }

    private static int posOffset(String A, int i, String B) {
        int posOffset = B.length();
        for (int j = 0; j < B.length(); ++j) {
            if (i - j >= 0 && i - j < posOffset && A.charAt(i) == B.charAt(i - j)) {
                return j;
            }
            if (i + j >= posOffset || A.charAt(i) != B.charAt(i + j)) continue;
            return j;
        }
        return posOffset;
    }

    public static String getIPAddress() {
        String ip = "";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress().toString();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public static int getInteger(String str) {
        int i = 0;
        if (str == null || str.isEmpty()) {
            i = -1;
        }
        i = StringUtil.isNumberic(str) ? Integer.valueOf(str) : -1;
        return i;
    }

    public static String StringRegexOfTitle(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }
        title = title.trim();
        Pattern pattern = Pattern.compile("(^\\s*\u3010[^\u3011]*\u3011\\s*)|(^\\s*\\[[^\\]]*\\]\\s*)|(\\s*\u6b63\u7248\\s*)|(\\s*\u5305\u90ae\\s*)");
        Matcher matcher = pattern.matcher(title);
        title = matcher.replaceAll("");
        return title;
    }

    public static void main(String[] args) {
        System.out.println(StringUtil.toNormal("15870769037\u629a\u5dde\u5e02\u8fce\u5bbe\u5927\u9053555\u53f7\u57ce\u5e02\u539f\u5885,\u5434\u743c344000"));
        String str = " a s c  ";
        System.out.println(str);
        StringUtil.StringRegexOfTitle("");
    }
}
