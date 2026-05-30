# Smart Addr — 智能地址解析服务

> 中文物流地址智能识别，从非结构化文本中自动提取省/市/区、收货人、手机号、邮编等信息。

[English Version](README_EN.md) | [API 文档](docs/API.md) | [测试报告](docs/TEST_REPORT.md)

---

## 🎯 项目定位

`smart-addr` 是一个专门面向**中文电商/物流场景**的地址智能解析服务。它能处理各种非标准的用户输入，例如：

```
"新疆维吾尔自治区可克达拉市王五收"
"收货人邮编李晓明 手机号邮编13812345678 所在地区邮编安徽淮南市寿县"
"上海邮编邮编闵行区邮编邮编旭辉国际3号楼"
```

这些输入在传统分词器下会失败，而 `smart-addr` 能正确识别。

---

## ✨ 核心特性

| 特性 | 说明 |
|------|------|
| 🧠 **双引擎架构** | `address-parse` 主引擎 + `IK 分词` 兜底引擎，确保高覆盖率 |
| 📦 **物流场景优化** | 专门针对电商收货地址训练，支持邮编/手机/收货人混合格式 |
| 🛡️ **Consignee 防误识** | 默认不识别收货人，仅在含明确关键字（收货人/收件人等）或极简纯人名时提取 |
| 🗺️ **自治区直辖县支持** | 完整支持新疆"自治区直辖县+可克达拉市"等特殊行政区划 |
| 🔢 **邮编自动填充** | 根据地区自动填充邮政编码 |
| 📊 **44 个回归测试** | 覆盖极简输入、邮编干扰、多手机号、省市同名等边缘场景 |

---

## 🚀 快速开始

### 环境要求

- Java 8+
- Maven 3.6+
- MySQL 5.7+（地区数据存储）

### 1. 克隆项目

```bash
git clone https://github.com/david4034647/smart-addr.git
cd smart-addr
```

### 2. 配置数据库

修改 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wxrrd
spring.datasource.username=your_username
spring.datasource.password=your_password
```

> 项目依赖 `wxrrd.region` 地区表（5323 条行政区划数据）。首次启动会自动加载到内存缓存。

### 3. 编译启动

```bash
mvn clean package -DskipTests
java -jar target/springboot-mybatis-0.0.1-SNAPSHOT.jar
```

服务默认运行在 `http://localhost:8435`

### 4. 调用接口

```bash
curl -sG "http://localhost:8435/rrd/address/extraction" \
  --data-urlencode "s=新疆维吾尔自治区可克达拉市王五收"
```

**返回结果：**

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

## 📐 架构设计

```
┌──────────────────────────────────────────────────────────┐
│                    Smart Addr 双引擎架构                   │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Layer 1: address-parse 主引擎                            │
│  ├─ 基于规则 + china-area.json 地区库                     │
│  ├─ 负责：省市区提取、邮编/手机号识别                     │
│  └─ 可靠性校验：consignee 不含行政区划关键词              │
│                                                          │
│  Layer 2: IK 分词 + 自定义地区库 兜底引擎                  │
│  ├─ 基于 IKAnalyzer 中文分词                             │
│  ├─ MySQL 地区表（5323 条）加载到内存                     │
│  ├─ 负责：address-parse 失败时的回退解析                  │
│  └─ 优化：路名后缀黑名单（大道/路/街/碑等）               │
│                                                          │
│  Layer 3: 极简输入兜底                                    │
│  └─ 仅省名/区名/纯人名等极简输入的全文匹配               │
│                                                          │
│  结果合并层                                               │
│  ├─ address-parse 人名/手机优先                           │
│  ├─ 旧逻辑地区信息补充（district/city/province）         │
│  └─ supplementAddress 自动补全省市关系                   │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 🧩 Consignee 识别策略

**核心原则：宁可漏识，不可误识。**

```java
提取收货人姓名的条件（满足任一即可）：

1. 输入中包含明确关键字
   "收货人" / "收件人" / "联系人" / "姓名" / "称呼"
   → 例："收货人张三 手机号138..." → consignee=张三 ✅

2. 极简纯人名输入兜底（确保覆盖线上功能）
   - 输入长度 2-3 个字
   - 全文字（不含数字）
   - 不含地址关键词（省/市/区/路/号等）
   - 姓氏在百家姓中
   → 例："李晓明" → consignee=李晓明 ✅
   → 例："王建国"   → consignee=王建国 ✅

不提取的场景：
   - "陕西省西安市雁塔区长安南路1号" → consignee="" ✅
   - "广东省深圳市南山区科技园南路88号" → consignee="" ✅
```

---

## 📊 测试覆盖

项目包含 **44 个回归测试用例**，覆盖以下场景：

| 场景分类 | 用例数 | 示例 |
|----------|--------|------|
| 标准省市区 | 5 | `四川省成都市锦江区春熙路1号` |
| 自治区/直辖县 | 6 | `新疆维吾尔自治区可克达拉市王五收` |
| 直辖市 | 5 | `上海市浦东新区世纪大道1号` |
| 邮编干扰 | 4 | `上海邮编邮编闵行区邮编邮编旭辉国际3号楼` |
| 极简输入 | 4 | `王建国`、`广东省`、`南山区` |
| 手机/邮编混排 | 6 | `张三13800138000广东省广州市...` |
| 省市同名 | 1 | `吉林省吉林市昌邑区重庆街1号` |
| 错误/无意义 | 2 | `test`、`可克达市` |

完整测试报告：[查看详情](docs/TEST_REPORT.md)

---

## 🗺️ 未来规划

| 阶段 | 目标 | 状态 |
|------|------|------|
| **v1.0** | 中文地址智能识别（省市区+收货人+手机+邮编） | ✅ 已完成 |
| **v1.1** | 修复邮编中置地址误识别（如`北京市100000朝阳区`） | 🔴 高优先级 |
| **v1.2** | 国际地址支持（Libpostal 集成） | 🟡 规划中 |
| **v1.3** | 繁体中文支持 | 🟡 规划中 |
| **v2.0** | 机器学习模型替换规则引擎 | 🔵 远期规划 |

---

## 🤝 贡献指南

欢迎提交 Issue 和 PR！

```bash
# 1. Fork 本仓库
# 2. 创建特性分支
git checkout -b feature/your-feature

# 3. 提交更改
git commit -m "feat: 描述你的更改"

# 4. 推送到你的 Fork
git push origin feature/your-feature

# 5. 提交 Pull Request
```

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 🙏 致谢

- [address-parse](https://github.com/im-neo/address-parse) — 中文地址解析库
- [IK Analyzer](https://github.com/wks/ik-analyzer) — 中文分词器
- [Spring Boot](https://spring.io/projects/spring-boot) — 应用框架

---

> **注意**：当前版本仅支持中文地址识别。国际地址支持正在开发中，敬请期待！
