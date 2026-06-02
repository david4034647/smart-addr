package rrd.service;

import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Bug 修复回归测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressServiceBugTest {

    @Autowired
    private AddressService addressService;

    /**
     * Bug 1: 输入"上海浦东新区"缺少 city 和 city_name
     * 期望: district=浦东新区 时，city 应从 district 的 parent 推导为"上海市"
     */
    @Test
    public void testBug1_ShanghaiPudongMissingCity() {
        JSONObject result = addressService.getWords("上海浦东新区");
        System.out.println("Bug1 result: " + result.toJSONString());

        Assert.assertFalse("province_name should not be empty",
                result.getString("province_name").isEmpty());
        Assert.assertFalse("district_name should not be empty",
                result.getString("district_name").isEmpty());

        // 核心断言：district=浦东新区时，city_name 必须不为空
        Assert.assertFalse("city_name must not be empty when district is present. " +
                        "District's parent city should be derived automatically.",
                result.getString("city_name").isEmpty());

        Assert.assertEquals("city_name should be 上海市", "上海市",
                result.getString("city_name"));
    }

    /**
     * Bug 2: 输入包含特殊字符 # 不应被截断
     * "东岸公寓2#304" 中的 # 是详细地址的一部分，表示栋号/房号分隔
     */
    @Test
    public void testBug2_HashCharacterShouldBePreserved() {
        JSONObject result = addressService.getWords("东岸公寓2#304");
        System.out.println("Bug2 result: " + result.toJSONString());

        String address = result.getString("address");
        Assert.assertTrue("address should contain '#' character. " +
                        "Input '东岸公寓2#304' should not be truncated to '东岸公寓2'.",
                address.contains("#"));
        Assert.assertTrue("address should contain '304'",
                address.contains("304"));
    }

    /**
     * Bug 3: 超长输入应能妥善处理
     * GET URL 中参数过长时，服务不应崩溃或截断
     */
    @Test
    public void testBug3_VeryLongInputShouldHandle() {
        // 构造一个 500 字的超长地址输入
        StringBuilder sb = new StringBuilder();
        sb.append("收货人李晓明 手机号13812345678 ");
        for (int i = 0; i < 20; i++) {
            sb.append("广东省深圳市南山区科技园南路88号科兴科学园B栋12楼1201室");
        }
        String longInput = sb.toString();

        System.out.println("Bug3 input length: " + longInput.length());

        JSONObject result = addressService.getWords(longInput);
        System.out.println("Bug3 result: " + result.toJSONString());

        // 至少应解析出省市区，不应抛出异常
        Assert.assertFalse("province should be parsed even for long input",
                result.getString("province_name").isEmpty());
        Assert.assertFalse("city should be parsed",
                result.getString("city_name").isEmpty());
        Assert.assertFalse("district should be parsed",
                result.getString("district_name").isEmpty());
    }

    /**
     * Bug 3 补充: 3000 字极限长度测试
     */
    @Test
    public void testBug3_ExtremeLongInput() {
        StringBuilder sb = new StringBuilder();
        sb.append("张三13812345678广东省深圳市南山区");
        // 填充大量无意义字符
        for (int i = 0; i < 500; i++) {
            sb.append("科技园路").append(i).append("号");
        }
        String extremeInput = sb.toString();

        System.out.println("Bug3-extreme input length: " + extremeInput.length());

        // 不应抛出异常
        JSONObject result = addressService.getWords(extremeInput);
        System.out.println("Bug3-extreme result: " + result.toJSONString());

        // 至少解析出省市区
        Assert.assertFalse("province should not be empty",
                result.getString("province_name").isEmpty());
    }
}
