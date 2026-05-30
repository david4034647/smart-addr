# Smart Addr API 文档

## 接口地址

```
GET /rrd/address/extraction
```

## 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `s` | string | 是 | 待解析的地址字符串 |

## 请求示例

```bash
curl -sG "http://localhost:8435/rrd/address/extraction" \
  --data-urlencode "s=新疆维吾尔自治区可克达拉市王五收"
```

## 响应字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `country` | int | 国家代码，固定为 1（中国） |
| `country_name` | string | 国家名称 |
| `province` | string | 省份 ID（如 `650000`） |
| `province_name` | string | 省份名称 |
| `city` | string | 城市 ID |
| `city_name` | string | 城市名称 |
| `district` | string | 区县 ID |
| `district_name` | string | 区县名称 |
| `consignee` | string | 收货人姓名 |
| `mobile` | string | 手机号码 |
| `zipcode` | string | 邮政编码 |
| `address` | string | 详细地址 |

## 响应示例

### 成功解析

```json
{
  "country": 1,
  "country_name": "中国",
  "province": "650000",
  "province_name": "新疆维吾尔自治区",
  "city": "659000",
  "city_name": "自治区直辖县",
  "district": "659008",
  "district_name": "可克达拉市",
  "consignee": "",
  "mobile": "",
  "zipcode": "830000",
  "address": "王五收"
}
```

### 无法解析

```json
{
  "country": 1,
  "country_name": "中国",
  "province": "",
  "province_name": "",
  "city": "",
  "city_name": "",
  "district": "",
  "district_name": "",
  "consignee": "",
  "mobile": "",
  "zipcode": "",
  "address": ""
}
```

## 测试用例示例

### 标准省市区

```bash
curl -sG "http://localhost:8435/rrd/address/extraction" \
  --data-urlencode "s=四川省成都市锦江区春熙路1号"
```

**期望：** province=四川省, city=成都市, district=锦江区

### 含手机号

```bash
curl -sG "http://localhost:8435/rrd/address/extraction" \
  --data-urlencode "s=王建国13912345678上海静安区昌平路88号"
```

**期望：** consignee=王建国, mobile=13912345678, province=上海, district=静安区

### 含"收货人"关键字

```bash
curl -sG "http://localhost:8435/rrd/address/extraction" \
  --data-urlencode "s=收货人张三 手机号13800138000 广东省深圳市南山区"
```

**期望：** consignee=张三, mobile=13800138000, province=广东省, city=深圳市, district=南山区
