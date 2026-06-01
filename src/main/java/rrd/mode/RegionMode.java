/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ArrayListMultimap
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.Sets
 *  org.apache.log4j.Logger
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 */
package rrd.mode;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rrd.bean.Region;
import rrd.bean.RegionDetails;
import rrd.dao.RegionDao;

@Component
public class RegionMode {
    private static Multimap<String, Integer> regionNameIds = ArrayListMultimap.create();
    private static Map<Integer, String> regionIdNames = Maps.newHashMap();
    private static Map<Integer, Integer> regionIdToParentRegionId = Maps.newHashMap();
    private static Set<String> zhixiashi = Sets.newHashSet("\u4e0a\u6d77", "\u5317\u4eac", "\u5929\u6d25", "\u91cd\u5e86");
    private static Logger logger = Logger.getLogger(RegionMode.class);
    @Autowired
    private RegionDao regionDao;

    @PostConstruct
    public void init() {
        if (regionNameIds.size() > 0) {
            return;
        }
        List<Region> regions = this.regionDao.findAllRegion();
        for (Region region : regions) {
            if (region.getRegionName().endsWith("\u7701") || region.getRegionName().endsWith("\u5e02")) {
                if (zhixiashi.contains(region.getRegionName().substring(0, region.getRegionName().length() - 1))) continue;
                regionNameIds.put(region.getRegionName().substring(0, region.getRegionName().length() - 1), region.getRegionId());
            }
            regionIdToParentRegionId.put(region.getRegionId(), region.getParRegionId());
            regionNameIds.put(region.getRegionName(), region.getRegionId());
            regionIdNames.put(region.getRegionId(), region.getRegionName());
        }
        logger.info((Object)("total size = " + regionNameIds.size()));
    }

    public boolean setAddress(String s, RegionDetails regionDetails) {
        logger.debug("setAddress word=\"" + s + "\" containsKey=" + regionNameIds.containsKey((Object)s));
        try {
            if (!regionNameIds.containsKey((Object)s)) {
                return false;
            }
            List<Integer> regionNameToIds = (List<Integer>) regionNameIds.get(s);
            logger.debug("   matched id count=" + regionNameToIds.size());
            if (regionNameToIds.size() > 1) {
                if (regionDetails.getDistrict().size() == 0) {
                    regionDetails.setDistrict(regionNameToIds);
                    regionDetails.setDistrict_name(s);
                    return true;
                }
                return false;
            }
            ArrayList regions = Lists.newArrayList();
            int parRegionId = 0;
            int regionId = (Integer)regionNameToIds.get(0);
            regions.add(regionId);
            while ((parRegionId = regionIdToParentRegionId.getOrDefault(regionId, 1).intValue()) != 1) {
                regions.add(parRegionId);
                regionId = parRegionId;
            }
            logger.debug("   region chain=" + regions + " size=" + regions.size());
            if (regions.size() == 1 && regionDetails.getProvince().length() == 0) {
                regionDetails.setProvince(((Integer)regions.get(0)).toString());
                regionDetails.setProvince_name(regionIdNames.get(regions.get(0)));
                logger.debug("   -> set PROVINCE id=" + regions.get(0) + " name=" + regionIdNames.get(regions.get(0)));
                return true;
            }
            if (regions.size() - 2 == 0 && regionDetails.getCity().length() == 0) {
                String regionName = regionIdNames.get(regions.get(0));
                // 直辖市特殊处理：如果 province 为空，直接当作 province
                if (regionName != null && zhixiashi.contains(regionName) && regionDetails.getProvince().isEmpty()) {
                    regionDetails.setProvince(((Integer)regions.get(0)).toString());
                    regionDetails.setProvince_name(regionName);
                    logger.debug("   -> set PROVINCE(直辖市) id=" + regions.get(0) + " name=" + regionName);
                    return true;
                }
                regionDetails.setCity(((Integer)regions.get(0)).toString());
                regionDetails.setCity_name(regionName);
                logger.debug("   -> set CITY id=" + regions.get(0) + " name=" + regionName);
                return true;
            }
            if (regions.size() - 3 == 0 && regionDetails.getDistrict().size() == 0) {
                regionDetails.setDistrict(regionNameToIds);
                regionDetails.setDistrict_name(s);
                logger.debug("   -> set DISTRICT id=" + regionNameToIds.get(0));
                return true;
            }
            logger.debug("   chain length " + regions.size() + " does NOT match 1/2/3, skip");
        }
        catch (Exception e) {
            logger.error((Object)"select address err ,", (Throwable)e);
        }
        return false;
    }

    /**
     * 通过地区名称查找 regionId
     * @param name 地区名称
     * @return 唯一匹配的 regionId，若无匹配或有多匹配则返回 null
     */
    public Integer findRegionIdByName(String name) {
        if (name == null || name.isEmpty() || !regionNameIds.containsKey((Object) name)) {
            return null;
        }
        List<Integer> ids = (List<Integer>) regionNameIds.get(name);
        if (ids != null && ids.size() == 1) {
            return ids.get(0);
        }
        // 多个匹配时返回 null（需要上下文消歧）
        return null;
    }

    /**
     * 通过 regionId 查找地区名称
     * @param regionId 地区 ID
     * @return 地区名称，若不存在返回 null
     */
    public String findRegionNameById(Integer regionId) {
        if (regionId == null) {
            return null;
        }
        return regionIdNames.get(regionId);
    }

    public void supplementAddress(RegionDetails regionDetails) {
        try {
            // 步骤1：处理多个 district 的情况
            if (regionDetails.getDistrict().size() > 1) {
                boolean isMatchCityId = false;
                for (Integer districtId : regionDetails.getDistrict()) {
                    if (!regionIdToParentRegionId.get(districtId).toString().equals(regionDetails.getCity())) continue;
                    isMatchCityId = true;
                }
                if (!isMatchCityId) {
                    regionDetails.setCity("");
                    return;
                }
            }

            // 步骤2：确定 cityId
            // 如果 district 存在，从 district 推导 city（优先级最高，覆盖 address-parse 的误判）
            String cityId = "";
            if (regionDetails.getDistrict().size() == 1) {
                Integer districtId = regionDetails.getDistrict().get(0);
                Integer derivedCityId = regionIdToParentRegionId.get(districtId);
                if (derivedCityId != null && derivedCityId != 1) {
                    cityId = derivedCityId.toString();
                    regionDetails.setCity(cityId);
                    regionDetails.setCity_name(regionIdNames.get(derivedCityId));
                }
            }
            // 如果没有 district，使用已有的 city
            if (cityId.isEmpty()) {
                cityId = regionDetails.getCity();
            }

            if (cityId.isEmpty()) {
                return;
            }

            // 步骤3：从 city 推导 province
            Integer parentId = regionIdToParentRegionId.get(Integer.valueOf(cityId));
            if (parentId == null) {
                return;
            }
            String provinceId = parentId.toString();
            if (provinceId.isEmpty()) {
                return;
            }
            // 当 provinceId=1 时说明 city 本身是顶级行政区（如直辖市的区），保留已有 province
            if ("1".equals(provinceId)) {
                return;
            }
            if (!provinceId.equals(regionDetails.getProvince()) && !regionDetails.getProvince().isEmpty()) {
                // 直辖市数据可能存在"上海"和"上海市"两条记录导致 parent_id 不一致，
                // 保留已有 province 避免被清空
                return;
            } else {
                regionDetails.setProvince(provinceId);
                regionDetails.setProvince_name(regionIdNames.get(Integer.valueOf(provinceId)));
            }
        }
        catch (Exception e) {
            logger.error((Object)"supplement address err,", (Throwable)e);
        }
    }
}
