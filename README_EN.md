# Smart Addr — Intelligent Address Parsing Service

> Extract province/city/district, consignee, mobile phone, and zip code from unstructured Chinese logistics addresses.

[中文版本](README.md) | [API Docs](docs/API.md) | [Test Report](docs/TEST_REPORT.md)

---

## 🎯 Project Overview

`smart-addr` is an intelligent address parsing service specifically designed for **Chinese e-commerce and logistics scenarios**. It can handle various non-standard user inputs, such as:

```
"新疆维吾尔自治区可克达拉市王五收"
"收货人邮编李晓明 手机号邮编13812345678 所在地区邮编安徽淮南市寿县"
"上海邮编邮编闵行区邮编邮编旭辉国际3号楼"
```

These inputs would fail with traditional tokenizers, but `smart-addr` can correctly identify them.

---

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| 🧠 **Dual-Engine Architecture** | `address-parse` primary engine + `IK Analyzer` fallback engine for high coverage |
| 📦 **Logistics Optimized** | Specifically trained for e-commerce delivery addresses; supports mixed zip/phone/consignee formats |
| 🛡️ **Anti-Misrecognition** | Consignee is NOT extracted by default; only triggered by explicit keywords (收货人/收件人) or minimal pure-name input |
| 🗺️ **Autonomous Region Support** | Full support for Xinjiang "自治区直辖县 + 可克达拉市" and other special administrative divisions |
| 🔢 **Auto Zip Code** | Automatically fills postal codes based on region |
| 📊 **44 Regression Tests** | Covers edge cases like minimal input, zip interference, multiple phones, same-name provinces/cities |

---

## 🚀 Quick Start

### Requirements

- Java 8+
- Maven 3.6+
- MySQL 5.7+ (for region data)

### 1. Clone

```bash
git clone https://github.com/david4034647/smart-addr.git
cd smart-addr
```

### 2. Configure Database

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wxrrd
spring.datasource.username=your_username
spring.datasource.password=your_password
```

> The project depends on `wxrrd.region` table (5,323 administrative region records). Data is automatically loaded into memory cache on first startup.

### 3. Build & Run

```bash
mvn clean package -DskipTests
java -jar target/springboot-mybatis-0.0.1-SNAPSHOT.jar
```

Service runs at `http://localhost:8435` by default.

### 4. API Call

```bash
curl -sG "http://localhost:8435/rrd/address/extraction" \
  --data-urlencode "s=新疆维吾尔自治区可克达拉市王五收"
```

**Response:**

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

---

## 📐 Architecture

```
┌──────────────────────────────────────────────────────────┐
│              Smart Addr Dual-Engine Architecture          │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Layer 1: address-parse Primary Engine                    │
│  ├─ Rule-based + china-area.json region library          │
│  ├─ Responsible: province/city/district extraction       │
│  └─ Reliability check: consignee contains no region      │
│     keywords                                             │
│                                                          │
│  Layer 2: IK Tokenizer + Custom Region DB Fallback       │
│  ├─ Based on IKAnalyzer Chinese tokenizer                │
│  ├─ MySQL region table (5,323 records) loaded to memory  │
│  ├─ Responsible: fallback when address-parse fails       │
│  └─ Optimized: road suffix blacklist (大道/路/街/碑等)   │
│                                                          │
│  Layer 3: Minimal Input Fallback                         │
│  └─ Full-text matching for province-only/district-only   │
│     /pure-name inputs                                    │
│                                                          │
│  Result Merge Layer                                      │
│  ├─ address-parse: consignee/phone priority              │
│  ├─ Legacy: region info supplement (district/city/prov)  │
│  └─ supplementAddress: auto-fill province-city relations │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 🧩 Consignee Recognition Strategy

**Core Principle: Better to miss than misidentify.**

```java
Consignee is extracted when ANY of the following is met:

1. Input contains explicit keywords
   "收货人" / "收件人" / "联系人" / "姓名" / "称呼"
   → Example: "收货人张三 手机号138..." → consignee=张三 ✅

2. Minimal pure-name input fallback
   - Input length: 2-3 Chinese characters
   - All text (no digits)
   - No address keywords (province/city/district/road/number)
   - Surname exists in Baijiaxing (百家姓)
   → Example: "李晓明" → consignee=李晓明 ✅
   → Example: "王建国"   → consignee=王建国 ✅

NOT extracted:
   - "陕西省西安市雁塔区长安南路1号" → consignee="" ✅
   - "广东省深圳市南山区科技园南路88号" → consignee="" ✅
```

---

## 📊 Test Coverage

The project includes **44 regression test cases** covering:

| Category | Count | Example |
|----------|-------|---------|
| Standard Province/City/District | 5 | `四川省成都市锦江区春熙路1号` |
| Autonomous Regions / Direct-Controlled Counties | 6 | `新疆维吾尔自治区可克达拉市王五收` |
| Municipalities | 5 | `上海市浦东新区世纪大道1号` |
| Zip Code Interference | 4 | `上海邮编邮编闵行区邮编邮编旭辉国际3号楼` |
| Minimal Input | 4 | `王建国`、`广东省`、`南山区` |
| Phone/Zip Mixed | 6 | `张三13800138000广东省广州市...` |
| Same-Name Province/City | 1 | `吉林省吉林市昌邑区重庆街1号` |
| Error/Meaningless | 2 | `test`、`可克达市` |

Full test report: [View Details](docs/TEST_REPORT.md)

---

## 🗺️ Roadmap

| Version | Goal | Status |
|---------|------|--------|
| **v1.0** | Chinese address recognition (province/city/district + consignee + phone + zip) | ✅ Completed |
| **v1.1** | Fix zip-in-middle misrecognition (e.g., `北京市100000朝阳区`) | 🔴 High Priority |
| **v1.2** | International address support (Libpostal integration) | 🟡 Planned |
| **v1.3** | Traditional Chinese support | 🟡 Planned |
| **v2.0** | ML model replacing rule engine | 🔵 Long-term |

---

## 🤝 Contributing

Issues and PRs are welcome!

```bash
# 1. Fork this repository
# 2. Create feature branch
git checkout -b feature/your-feature

# 3. Commit changes
git commit -m "feat: describe your change"

# 4. Push to your fork
git push origin feature/your-feature

# 5. Submit Pull Request
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

## 🙏 Acknowledgments

- [address-parse](https://github.com/im-neo/address-parse) — Chinese address parsing library
- [IK Analyzer](https://github.com/wks/ik-analyzer) — Chinese tokenizer
- [Spring Boot](https://spring.io/projects/spring-boot) — Application framework

---

> **Note**: Current version only supports Chinese address recognition. International address support is under development. Stay tuned!
