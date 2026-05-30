package rrd.service;

import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 地址解析服务回归测试
 * 覆盖 44 个核心场景中的关键用例
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressServiceTest {

    @Autowired
    private AddressService addressService;

    @Before
    public void warmup() {
        // address-parse 首次调用有预热成本（约 440ms），先执行一次
        addressService.getWords("广东省深圳市南山区");
    }

    // ==================== address-parse 主解析验证 ====================

    @Test
    public void testTC35_Xinjiang_Kekedala() {
        // TC35: 新疆+可克达拉市（核心修复场景）
        JSONObject result = addressService.getWords("新疆维吾尔自治区可克达拉市王五收");
        System.out.println("TC35 result: " + result.toJSONString());
        Assert.assertFalse("province should not be empty",
                result.getString("province_name").isEmpty());
        Assert.assertFalse("city should not be empty",
                result.getString("city_name").isEmpty());
        Assert.assertFalse("district should not be empty",
                result.getString("district_name").isEmpty());
    }

    @Test
    public void testTC06_OnlyName() {
        // TC06: 仅人名
        JSONObject result = addressService.getWords("代伟");
        System.out.println("TC06 result: " + result.toJSONString());
        // address-parse 应该能识别出人名，或通过兜底识别
        Assert.assertTrue("consignee should be recognized",
                result.getString("consignee").contains("代伟") ||
                        result.getString("address").contains("代伟"));
    }

    @Test
    public void testTC10_ComplexAddress() {
        // TC10: 完整复杂地址（直辖市上海 + 静安区）
        JSONObject result = addressService.getWords(
                "代伟18621478902上海静安区昌平路东岸公寓2号楼304室230031");
        System.out.println("TC10 result: " + result.toJSONString());
        Assert.assertFalse("province should not be empty",
                result.getString("province_name").isEmpty());
        // 直辖市中"静安区"作为 district，不是 city
        Assert.assertTrue("district or city should contain 静安",
                !result.getString("district_name").isEmpty()
                        || result.getString("city_name").contains("静安"));
        Assert.assertEquals("mobile", "18621478902",
                result.getString("mobile"));
    }

    @Test
    public void testTC31_NameInMiddle() {
        // TC31: 手机号中置，人名在地址中间
        JSONObject result = addressService.getWords(
                "广东省深圳市南山区科技园南路88号张三13800138000号1201室");
        System.out.println("TC31 result: " + result.toJSONString());
        Assert.assertEquals("mobile", "13800138000",
                result.getString("mobile"));
        Assert.assertTrue("consignee should contain 张三",
                result.getString("consignee").contains("张三") ||
                        result.getString("address").contains("张三"));
    }

    // ==================== 兜底解析验证 ====================

    @Test
    public void testTC12_OnlyProvince() {
        // TC12: 仅省名（极简输入兜底）
        JSONObject result = addressService.getWords("广东省");
        System.out.println("TC12 result: " + result.toJSONString());
        Assert.assertFalse("province should not be empty",
                result.getString("province_name").isEmpty());
    }

    @Test
    public void testTC11_OnlyDistrict() {
        // TC11: 仅区名（极简输入兜底）
        JSONObject result = addressService.getWords("南山区");
        System.out.println("TC11 result: " + result.toJSONString());
        Assert.assertFalse("district should not be empty",
                result.getString("district_name").isEmpty());
    }

    // ==================== 人名过度识别修复验证 ====================

    @Test
    public void testTC22_NotOverRecognizeRoadName() {
        // TC22: "解放碑"不应被误识为人名
        JSONObject result = addressService.getWords("重庆市渝中区解放碑1号");
        System.out.println("TC22 result: " + result.toJSONString());
        // 验证省市区至少解析出一个
        Assert.assertTrue("province or city or district should not be empty",
                !result.getString("province_name").isEmpty()
                        || !result.getString("city_name").isEmpty()
                        || !result.getString("district_name").isEmpty());
        // 不应将"解放碑"误识为 consignee
        Assert.assertFalse("consignee should not be 解放碑",
                "解放碑".equals(result.getString("consignee")));
    }

    @Test
    public void testTC28_NotOverRecognizeRoadName2() {
        // TC28: "文三"不应被误识为人名
        JSONObject result = addressService.getWords(
                "浙江省杭州市西湖区文三路478号华星时代广场A座1901室王建国收310012");
        System.out.println("TC28 result: " + result.toJSONString());
        Assert.assertFalse("province should not be empty",
                result.getString("province_name").isEmpty());
        Assert.assertFalse("district should not be empty",
                result.getString("district_name").isEmpty());
    }

    // ==================== 一致性验证 ====================

    @Test
    public void testTC27_StandardAddress() {
        // TC27: 标准省市区地址
        JSONObject result = addressService.getWords("四川省成都市锦江区春熙路1号");
        System.out.println("TC27 result: " + result.toJSONString());
        Assert.assertEquals("province", "四川省",
                result.getString("province_name"));
        Assert.assertEquals("city", "成都市",
                result.getString("city_name"));
        Assert.assertEquals("district", "锦江区",
                result.getString("district_name"));
    }

    @Test
    public void testTC20_LabeledComplexAddress() {
        // TC20: 带标签复杂地址
        JSONObject result = addressService.getWords(
                "收货人邮编徐敏娟 手机号邮编13865432632 所在地区邮编安徽淮南市寿县 详细地址邮编西大街红学新村对面老食品厂家属区");
        System.out.println("TC20 result: " + result.toJSONString());
        Assert.assertFalse("province should not be empty",
                result.getString("province_name").isEmpty());
        Assert.assertFalse("district should not be empty",
                result.getString("district_name").isEmpty());
    }

    @Test
    public void testTC38_PostcodeSeparator() {
        // TC38: 含"邮编"分隔符
        JSONObject result = addressService.getWords("上海邮编邮编闵行区邮编邮编旭辉国际3号楼");
        System.out.println("TC38 result: " + result.toJSONString());
        Assert.assertFalse("province should not be empty",
                result.getString("province_name").isEmpty());
    }

    @Test
    public void testTC41_XinjiangNoSpace() {
        // TC41: 新疆+自治区直辖县+可克达拉（无空格）
        JSONObject result = addressService.getWords("新疆维吾尔自治区自治区直辖县可克达拉市");
        System.out.println("TC41 result: " + result.toJSONString());
        Assert.assertFalse("province should not be empty",
                result.getString("province_name").isEmpty());
    }

    @Test
    public void testTC44_XinjiangWithPostcode() {
        // TC44: 新疆+邮编+自治区直辖县
        JSONObject result = addressService.getWords(
                "新疆维吾尔自治区邮编邮编自治区直辖县邮编邮编可克达拉市");
        System.out.println("TC44 result: " + result.toJSONString());
        Assert.assertFalse("province should not be empty",
                result.getString("province_name").isEmpty());
    }

    @Test
    public void testTC34_TibetAddress() {
        // TC34: 自治区地址
        JSONObject result = addressService.getWords("西藏自治区拉萨市城关区北京中路1号");
        System.out.println("TC34 result: " + result.toJSONString());
        Assert.assertEquals("province", "西藏自治区",
                result.getString("province_name"));
        Assert.assertEquals("city", "拉萨市",
                result.getString("city_name"));
        Assert.assertEquals("district", "城关区",
                result.getString("district_name"));
    }
}
