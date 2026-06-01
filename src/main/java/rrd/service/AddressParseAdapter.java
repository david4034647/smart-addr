package rrd.service;

import com.neo.address.parse.AddressParse;
import com.neo.address.parse.ParseResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rrd.bean.RegionDetails;
import rrd.mode.RegionMode;

import java.util.List;

/**
 * address-parse 库结果适配器
 * 将 address-parse 的 ParseResult 转换为项目内部的 RegionDetails
 */
@Component
public class AddressParseAdapter {
    private static Logger logger = Logger.getLogger(AddressParseAdapter.class);

    @Autowired
    private RegionMode regionMode;

    /**
     * 使用 address-parse 解析地址，并转换为 RegionDetails
     *
     * @param input 原始地址字符串
     * @return 解析结果，如果 address-parse 未能解析出有效地区信息则返回 null
     */
    public RegionDetails parse(String input) {
        try {
            List<ParseResult> results = AddressParse.parse(input);
            if (results == null || results.isEmpty()) {
                return null;
            }

            ParseResult pr = results.get(0);
            if (pr == null) {
                return null;
            }

            // 如果 province 和 city 都为空，认为解析失败
            if (isEmpty(pr.getProvince()) && isEmpty(pr.getCity()) && isEmpty(pr.getArea())) {
                // 但如果有姓名或手机号，仍可返回部分结果
                if (isEmpty(pr.getName()) && isEmpty(pr.getMobile()) && isEmpty(pr.getZipCode())) {
                    return null;
                }
            }

            RegionDetails rd = new RegionDetails();
            rd.setMobile(nullToEmpty(pr.getMobile()));
            rd.setZipcode(nullToEmpty(pr.getZipCode()));
            rd.setConsignee(nullToEmpty(pr.getName()));
            rd.setAddress(nullToEmpty(pr.getDetail()));

            // 通过 RegionMode 将地区名称映射为 regionId
            setRegionByName(rd, pr.getProvince(), pr.getCity(), pr.getArea());

            logger.info("[AddressParseAdapter] parsed: province=" + rd.getProvince_name()
                    + ", city=" + rd.getCity_name()
                    + ", district=" + rd.getDistrict_name()
                    + ", consignee=" + rd.getConsignee()
                    + ", mobile=" + rd.getMobile());

            return rd;
        } catch (Exception e) {
            logger.error("[AddressParseAdapter] parse error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置省市区信息，通过名称查找对应的 regionId
     * 增强匹配：自动尝试添加常见后缀
     */
    private void setRegionByName(RegionDetails rd, String provinceName, String cityName, String areaName) {
        // 处理省
        if (!isEmpty(provinceName)) {
            Integer provinceId = findRegionIdWithFallback(provinceName, new String[]{"省", "自治区", "特别行政区"});
            if (provinceId != null) {
                rd.setProvince(String.valueOf(provinceId));
                rd.setProvince_name(regionMode.findRegionNameById(provinceId));
            } else {
                // 未找到 ID，但保留名称
                rd.setProvince_name(provinceName);
            }
        }

        // 处理市
        if (!isEmpty(cityName)) {
            Integer cityId = findRegionIdWithFallback(cityName, new String[]{"市", "州", "盟"});
            if (cityId != null) {
                rd.setCity(String.valueOf(cityId));
                rd.setCity_name(regionMode.findRegionNameById(cityId));
            } else {
                // 找不到 regionId 时，用原始名称作为 city 标记，确保 mergeResults 能识别
                rd.setCity(cityName);
                rd.setCity_name(cityName);
            }
        }

        // 处理区/县
        if (!isEmpty(areaName)) {
            Integer areaId = findRegionIdWithFallback(areaName, new String[]{"区", "县", "市", "旗"});
            if (areaId != null) {
                java.util.List<Integer> districts = new java.util.ArrayList<>();
                districts.add(areaId);
                rd.setDistrict(districts);
                rd.setDistrict_name(regionMode.findRegionNameById(areaId));
            } else {
                rd.setDistrict_name(areaName);
            }
        }
    }

    /**
     * 查找 regionId，如果原始名称找不到，尝试添加后缀再查找
     */
    private Integer findRegionIdWithFallback(String name, String[] suffixes) {
        Integer id = regionMode.findRegionIdByName(name);
        if (id != null) {
            return id;
        }
        for (String suffix : suffixes) {
            id = regionMode.findRegionIdByName(name + suffix);
            if (id != null) {
                return id;
            }
        }
        // 兜底：尝试"市"后缀（适用于直辖市如"上海"→"上海市"）
        id = regionMode.findRegionIdByName(name + "市");
        if (id != null) {
            return id;
        }
        return null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
