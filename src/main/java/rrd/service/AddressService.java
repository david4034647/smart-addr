/*
 * Decompiled with CFR 0.152.
 */
package rrd.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.neo.address.parse.AddressParse;
import com.neo.address.parse.ParseResult;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import rrd.bean.RegionDetails;
import rrd.mode.RegionMode;
import rrd.util.StringUtil;

@Service
public class AddressService {
    private static Logger logger = Logger.getLogger(AddressService.class);
    private IKSegmenter iks = new IKSegmenter((Reader) new StringReader(""), true);

    @Autowired
    private RegionMode regionMode;
    @Autowired
    private AddressParseAdapter addressParseAdapter;

    // 路名/地标后缀黑名单，避免误识为人名
    private static final Set<String> ROAD_SUFFIXES = new HashSet<String>(Arrays.asList(
            "大道", "路", "街", "巷", "桥", "广场", "公园", "大厦", "公寓",
            "小区", "花园", "商城", "中心", "号楼", "栋", "单元", "层",
            "弄", "胡同", "里", "院", "碑"
    ));

    // 明确的人名关键字：只有输入中包含这些词时，才进行收货人识别
    private static final String[] CONSIGNEE_KEYWORDS = {
            "收货人", "收件人", "联系人", "姓名", "称呼"
    };

    /**
     * 主入口：三层解析策略
     * Layer 1: address-parse 主解析（中文地址智能识别库）
     * Layer 2: 优化后的旧逻辑（IK 分词 + 地区库匹配）
     * Layer 3: 极简输入全文匹配兜底
     */
    public synchronized JSONObject getWords(String input) {
        logger.debug("getWords input=" + input);

        // 判断是否应该提取收货人姓名（只有包含明确关键字时才提取）
        boolean allowConsignee = shouldExtractConsignee(input);
        logger.debug("allowConsignee=" + allowConsignee);

        // Layer 1: address-parse 主解析
        RegionDetails apResult = null;
        try {
            apResult = addressParseAdapter.parse(input);
            // 如果不允许提取收货人，清空 address-parse 的 consignee
            if (!allowConsignee && apResult != null) {
                apResult.setConsignee("");
            }
        } catch (Exception e) {
            logger.error("address-parse error", e);
        }

        // Layer 2: 优化后的旧逻辑（始终执行，用于补充 address-parse 缺失的地区信息）
        RegionDetails legacyResult = doLegacyParse(input, allowConsignee);

        // 如果 address-parse 结果可靠，合并两个引擎的结果（address-parse 人名手机优先，旧逻辑地区补充）
        if (apResult != null && isReliableResult(apResult, input)) {
            logger.debug(" Layer 1 address-parse succeeded, merging with legacy");
            RegionDetails merged = mergeResults(apResult, legacyResult, allowConsignee);
            return merged.convertToJSON();
        }

        // Layer 3: 极简输入全文匹配兜底
        if (!isValidResult(legacyResult)) {
            logger.debug(" Layer 3 minimal input fallback");
            RegionDetails minimalResult = parseMinimalInput(input, allowConsignee);
            if (isValidResult(minimalResult)) {
                return minimalResult.convertToJSON();
            }
        }

        return legacyResult.convertToJSON();
    }

    /**
     * 合并 address-parse 和旧逻辑的结果
     * 策略：address-parse 的人名、手机、邮编优先；旧逻辑的地区信息补充
     */
    private RegionDetails mergeResults(RegionDetails ap, RegionDetails legacy, boolean allowConsignee) {
        RegionDetails merged = new RegionDetails();

        // 收货人姓名：只有输入中包含明确关键字时才提取，避免地址被误吞
        if (allowConsignee) {
            if (!ap.getConsignee().isEmpty()) {
                merged.setConsignee(ap.getConsignee());
            } else if (!legacy.getConsignee().isEmpty() && !isNameEmbeddedInAddress(legacy.getConsignee(), ap)) {
                merged.setConsignee(legacy.getConsignee());
            }
        }
        merged.setMobile(!ap.getMobile().isEmpty() ? ap.getMobile() : legacy.getMobile());
        merged.setZipcode(!ap.getZipcode().isEmpty() ? ap.getZipcode() : legacy.getZipcode());

        // 地区信息：优先使用有 regionId 的结果，否则使用有名称的结果
        // district
        if (!ap.getDistrict().isEmpty()) {
            merged.setDistrict(ap.getDistrict());
            merged.setDistrict_name(ap.getDistrict_name());
        } else if (!legacy.getDistrict().isEmpty()) {
            merged.setDistrict(legacy.getDistrict());
            merged.setDistrict_name(legacy.getDistrict_name());
        } else if (!ap.getDistrict_name().isEmpty()) {
            merged.setDistrict_name(ap.getDistrict_name());
        } else if (!legacy.getDistrict_name().isEmpty()) {
            merged.setDistrict_name(legacy.getDistrict_name());
        }

        // city
        if (!ap.getCity().isEmpty()) {
            merged.setCity(ap.getCity());
            merged.setCity_name(ap.getCity_name());
        } else if (!legacy.getCity().isEmpty()) {
            merged.setCity(legacy.getCity());
            merged.setCity_name(legacy.getCity_name());
        } else if (!ap.getCity_name().isEmpty()) {
            merged.setCity_name(ap.getCity_name());
        } else if (!legacy.getCity_name().isEmpty()) {
            merged.setCity_name(legacy.getCity_name());
        }

        // province
        if (!ap.getProvince().isEmpty()) {
            merged.setProvince(ap.getProvince());
            merged.setProvince_name(ap.getProvince_name());
        } else if (!legacy.getProvince().isEmpty()) {
            merged.setProvince(legacy.getProvince());
            merged.setProvince_name(legacy.getProvince_name());
        } else if (!ap.getProvince_name().isEmpty()) {
            merged.setProvince_name(ap.getProvince_name());
        } else if (!legacy.getProvince_name().isEmpty()) {
            merged.setProvince_name(legacy.getProvince_name());
        }

        // address：取非空的那个
        if (!ap.getAddress().isEmpty()) {
            merged.setAddress(ap.getAddress());
        } else if (!legacy.getAddress().isEmpty()) {
            merged.setAddress(legacy.getAddress());
        }

        // 补充省市关系
        this.regionMode.supplementAddress(merged);

        return merged;
    }

    /**
     * 判断解析结果是否有效（至少解析出一个有效字段）
     */
    private boolean isValidResult(RegionDetails rd) {
        if (rd == null) return false;
        return !rd.getProvince().isEmpty()
                || !rd.getCity().isEmpty()
                || !rd.getDistrict_name().isEmpty()
                || !rd.getConsignee().isEmpty()
                || !rd.getMobile().isEmpty()
                || !rd.getZipcode().isEmpty();
    }

    /**
     * 判断旧逻辑提取的人名是否实际上嵌在地址中（address-parse 已识别的地址部分）
     */
    private boolean isNameEmbeddedInAddress(String name, RegionDetails ap) {
        if (name.isEmpty()) return false;
        // 如果 address-parse 的详细地址中包含该"人名"，说明是误识
        return !ap.getAddress().isEmpty() && ap.getAddress().contains(name);
    }

    /**
     * 判断是否应该提取收货人姓名
     * 策略：
     * 1. 输入中包含明确的人名关键字（收货人/收件人/联系人/姓名/称呼）时提取
     * 2. 兜底：极简纯人名输入（全文字、2-3字、姓氏匹配、不含地址关键词）时提取
     * 避免地址中的文字被误识为人名（如"安南"、"号1201"、"解放碑"等）
     */
    private boolean shouldExtractConsignee(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        // 规则1：包含明确关键字
        for (String keyword : CONSIGNEE_KEYWORDS) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        // 规则2：极简纯人名输入兜底（确保本地覆盖线上功能）
        String trimmed = input.trim();
        if (trimmed.length() >= 2 && trimmed.length() <= 3 && StringUtil.isNameStrict(trimmed)) {
            logger.debug(" pure name fallback matched: " + trimmed);
            return true;
        }
        return false;
    }

    /**
     * 判断 address-parse 的结果是否可靠，不可靠时应回退到旧逻辑
     */
    private boolean isReliableResult(RegionDetails rd, String originalInput) {
        if (rd == null || !isValidResult(rd)) {
            return false;
        }

        String consignee = rd.getConsignee();
        String province = rd.getProvince_name();
        String city = rd.getCity_name();
        String district = rd.getDistrict_name();

        // 检查1：consignee 中不应包含行政区划关键词
        if (!consignee.isEmpty()) {
            String[] regionKeywords = {"省", "自治区", "市辖区", "特别行政区", "维吾尔", "回族", "壮族"};
            for (String kw : regionKeywords) {
                if (consignee.contains(kw)) {
                    logger.debug(" unreliable: consignee contains region keyword '" + kw + "'");
                    return false;
                }
            }
        }

        // 检查2：如果 province 和输入中的省份明显不一致
        if (!province.isEmpty() && originalInput.contains("西藏") && !province.contains("西藏")) {
            logger.debug(" unreliable: province '" + province + "' mismatches 西藏 in input");
            return false;
        }
        if (!province.isEmpty() && originalInput.contains("新疆") && !province.contains("新疆")) {
            logger.debug(" unreliable: province '" + province + "' mismatches 新疆 in input");
            return false;
        }

        // 检查3：如果只解析出 province，且输入较长（非极简输入），可能解析不充分
        if (!province.isEmpty() && city.isEmpty() && district.isEmpty()
                && consignee.isEmpty() && originalInput.length() > 5) {
            // 但如果输入就是省名本身，不算不充分
            if (!originalInput.contains(province) || originalInput.length() > province.length() + 2) {
                logger.debug(" unreliable: only province parsed for long input");
                return false;
            }
        }

        return true;
    }

    /**
     * Layer 2: 优化后的旧逻辑（IK 分词 + 地区库匹配）
     */
    private RegionDetails doLegacyParse(String input, boolean allowConsignee) {
        this.iks.reset((Reader) new StringReader(input));
        RegionDetails regionDetails = new RegionDetails();
        ArrayList address = Lists.newArrayList();
        try {
            Lexeme l = null;
            StringBuffer sb = new StringBuffer();
            while ((l = this.iks.next()) != null) {
                String word = l.getLexemeText();
                int lexType = l.getLexemeType();
                logger.debug(" IK seg word=\"" + word + "\" type=" + lexType
                        + " begin=" + l.getBeginPosition() + " end=" + l.getEndPosition());

                if (l.getLexemeType() == 2 && StringUtil.isPhone(l.getLexemeText())) {
                    regionDetails.setMobile(l.getLexemeText());
                } else if (l.getLexemeType() == 2 && StringUtil.isZipCode(l.getLexemeText())) {
                    regionDetails.setZipcode(l.getLexemeText());
                } else if (!(l.getLexemeText().equals("邮编")
                        || !input.substring(l.getEndPosition(), Math.min(input.length(), l.getEndPosition() + 1)).equals("路")
                        && this.regionMode.setAddress(l.getLexemeText(), regionDetails))) {
                    sb.append(l.getLexemeText());
                    logger.debug("   -> appended to address buffer, sb now=\"" + sb.toString() + "\"");
                    continue;
                }
                this.putConsignee(regionDetails, address, sb, allowConsignee);
                sb.delete(0, sb.length());
            }
            this.putConsignee(regionDetails, address, sb, allowConsignee);
            this.getDetailAddress(regionDetails, address);
            this.regionMode.supplementAddress(regionDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug(" final regionDetails province=" + regionDetails.getProvince()
                + " city=" + regionDetails.getCity()
                + " district=" + regionDetails.getDistrict()
                + " address=" + regionDetails.getAddress());
        return regionDetails;
    }

    /**
     * Layer 3: 极简输入全文匹配兜底
     * 对无法通过分词解析的极简输入（如"广东省"、"南山区"、"代伟"），
     * 直接对整个输入串做地区匹配和人名提取
     */
    private RegionDetails parseMinimalInput(String input, boolean allowConsignee) {
        RegionDetails rd = new RegionDetails();
        if (input == null || input.isEmpty()) {
            return rd;
        }

        // 步骤1：尝试从整个输入中提取地区（从长到短滑动窗口）
        for (int len = input.length(); len >= 2; len--) {
            for (int i = 0; i <= input.length() - len; i++) {
                String sub = input.substring(i, i + len);
                if (this.regionMode.setAddress(sub, rd)) {
                    // 匹配到地区，处理剩余文本
                    String remaining = input.substring(0, i) + input.substring(i + len);
                    if (remaining.length() >= 2) {
                        String name = allowConsignee ? this.extractName(remaining) : null;
                        if (name != null) {
                            rd.setConsignee(name);
                        } else {
                            rd.setAddress(remaining);
                        }
                    }
                    this.regionMode.supplementAddress(rd);
                    return rd;
                }
            }
        }

        // 步骤2：没有匹配到地区，且允许提取人名时，尝试提取（仅对 2-4 字短文本）
        if (allowConsignee && input.length() >= 2 && input.length() <= 4) {
            String name = this.extractName(input);
            if (name != null) {
                rd.setConsignee(name);
            }
        }
        return rd;
    }

    private void putConsignee(RegionDetails regionDetails, List<String> address, StringBuffer sb, boolean allowConsignee) {
        if (sb.length() == 0) {
            return;
        }
        String text = sb.toString();

        // 步骤1：如果允许提取人名且还没有提取到，尝试从 text 中滑动提取
        if (allowConsignee && regionDetails.getConsignee().isEmpty()) {
            String foundName = this.extractName(text);
            if (foundName != null) {
                regionDetails.setConsignee(foundName);
                int idx = text.indexOf(foundName);
                // 人名前的内容作为 address 片段
                if (idx > 0) {
                    String prefix = text.substring(0, idx);
                    boolean regionMatched = this.tryExtractRegion(prefix, regionDetails, address);
                    if (!regionMatched && prefix.length() > 0) {
                        address.add(prefix);
                    }
                }
                // 人名后的内容作为 address 片段
                String suffix = text.substring(idx + foundName.length());
                if (suffix.length() > 0) {
                    boolean regionMatched = this.tryExtractRegion(suffix, regionDetails, address);
                    if (!regionMatched) {
                        address.add(suffix);
                    }
                }
                sb.delete(0, sb.length());
                return;
            }
        }

        // 步骤2：尝试提取地名
        boolean matched = this.tryExtractRegion(text, regionDetails, address);
        if (!matched) {
            address.add(text);
        }
        sb.delete(0, sb.length());
    }

    /**
     * 从文本中滑动提取人名（优先较长、优先从前方匹配）
     * 优化：增加路名/地标后缀黑名单过滤
     */
    private String extractName(String text) {
        if (text == null || text.length() < 2) {
            return null;
        }
        for (int i = 0; i <= text.length() - 2; i++) {
            int maxLen = Math.min(4, text.length() - i);
            for (int len = maxLen; len >= 2; len--) {
                String sub = text.substring(i, i + len);
                // 路名/地标黑名单过滤，避免"解放碑"、"纪大道"等被误识为人名
                if (isRoadOrLandmark(sub)) {
                    continue;
                }
                if (StringUtil.isNameStrict(sub)) {
                    return sub;
                }
            }
        }
        return null;
    }

    /**
     * 判断文本是否为路名或地标（避免误识为人名）
     */
    private boolean isRoadOrLandmark(String text) {
        for (String suffix : ROAD_SUFFIXES) {
            if (text.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 组合回退匹配：对连续未匹配的单字累积串，从中提取最长的匹配地名
     */
    private boolean tryExtractRegion(String text, RegionDetails regionDetails, List<String> address) {
        if (text == null || text.length() < 3) {
            return false;
        }
        logger.debug(" tryExtractRegion text=\"" + text + "\"");
        for (int len = text.length(); len >= 3; len--) {
            for (int i = 0; i <= text.length() - len; i++) {
                String sub = text.substring(i, i + len);
                boolean setAddrResult = this.regionMode.setAddress(sub, regionDetails);
                if (setAddrResult) {
                    logger.debug("   extracted region=\"" + sub + "\" from offset=" + i);
                    if (i > 0) {
                        address.add(text.substring(0, i));
                    }
                    if (i + len < text.length()) {
                        address.add(text.substring(i + len));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void getDetailAddress(RegionDetails regionDetails, List<String> address) {
        try {
            if (address.size() == 0) {
                return;
            }
            int maxLengthOfRegion = 0;
            int index = 0;
            for (int i = 0; i < address.size(); ++i) {
                if (address.get(i).length() <= maxLengthOfRegion) continue;
                maxLengthOfRegion = address.get(i).length();
                index = i;
            }
            regionDetails.setAddress(address.get(index));
        } catch (Exception e) {
            logger.error("get address err ", e);
        }
    }
}
