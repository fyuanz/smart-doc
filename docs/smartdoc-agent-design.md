# SmartDoc-Agent 产品需求与架构设计文档

> 版本：v1.0.0（初稿统一落盘）
> 日期：2026-09-07
> 状态：已评审待完善
> 定位：本文件是 SmartDoc-Agent 项目的「产品需求 + 技术架构」唯一事实来源，后续实现以本文为准；约定细节可随项目推进以增量方式补充，但不推翻已确认的核心决策。

---

## 目录

1. [项目概述](#1-项目概述)
2. [产品需求](#2-产品需求)
3. [核心设计理念](#3-核心设计理念)
4. [总体架构](#4-总体架构)
5. [双通道输入与统一 IR（L2）](#5-双通道输入与统一-irl2)
6. [检索策略（运行期主路径）](#6-检索策略运行期主路径)
7. [工程模块结构](#7-工程模块结构)
8. [核心数据契约 smartdoc.json（L3）](#8-核心数据契约-smartdocjsonl3)
9. [关键技术决策记录（ADR）](#9-关键技术决策记录adr)
10. [技术选型清单](#10-技术选型清单)
11. [演进路线图](#11-演进路线图)
12. [后续规划与待办](#12-后续规划与待办)
13. [附录 A：统一 IR 完整示例](#13-附录-a统一-ir-完整示例)

---

## 1. 项目概述

### 1.1 一句话定位

> SmartDoc-Agent 是一套「零源码依赖 · 编译期知识萃取」的智能软件说明书引擎：在 CI/CD 构建阶段解析编译产物（或源码），用大模型（LLM）生成结构化功能说明书，再以极轻量 SDK 嵌入宿主软件，让最终用户通过自然语言提问获得「功能怎么用 / 方法怎么调」的精准指引。

### 1.2 背景与痛点

传统软件"可用不可知"问题突出：企业内部积累了大量的组件库、SDK、API，但使用者（尤其是新员工、下游团队、第三方集成方）往往无法快速得知某个能力是否存在、该如何调用、参数怎么填、有哪些坑。现有解决方案各自的缺陷：

- 手写文档：滞后、不一致、维护成本高、覆盖率低；
- 直接让 LLM 读源码生成文档：需要暴露源码，难以满足金融、政务等高安全合规要求；
- 运行期实时调用 LLM：存在网络依赖、Token 成本、响应延迟，以及"幻觉"风险，企业级场景难以信任。

### 1.3 命名与历史沿革

本项目在早期探索中出现过三个命名方案，现已**统一采用 `SmartDoc-Agent`**：

| 命名 | 来源文档 | 说明 |
|------|----------|------|
| iGuide SDK | `deepseek_html_20260907_ba3011.html` | 早期方案，模块前缀 `cw-` |
| SmartDoc-Agent | `smartdoc-agent-spec.html` | 采用此命名与模块前缀 `smartdoc-agent-` |
| SmartDoc-Agent（最终） | 本文档 | 融合双通道、A+B 混合架构的最终方案 |

最终工程与包名前缀：`smartdoc-agent-*`（模块）、`com.smartdoc.agent`（groupId/包名）。

---

## 2. 产品需求

### 2.1 核心价值

| 价值点 | 含义 | 达成方式 |
|--------|------|----------|
| 源码零泄漏 | 全程可只接触字节码，源码永不离开仓库 | 默认字节码通道，`sourceChannels` 标记用于审计 |
| 运行期零延迟 | 运行时无网络、无 LLM 调用 | 预生成 + 本地内存检索，响应 < 20ms |
| 确定性 AI | AI 推理前置到构建期，运行期不产生幻觉 | LLM 只在构建期出结果，经 Schema 校验归档 |
| 极简接入 | SDK 体积 < 300KB，宿主无第三方依赖污染 | `runtime` 模块独立、轻量 |

### 2.2 目标用户与使用场景

**目标用户（双端）：**

- 构建期使用者：SDK/组件库维护团队、CI/CD 平台、平台工程团队；
- 运行期使用者：企业软件最终用户、下游开发团队、第三方集成方。

**典型使用场景：**

1. 用户提问「如何修复自相交图形？」，系统返回 `GeometryFixer#simpleRepair(...)` 的方法说明与调用示例；
2. 下游团队接入某内部 SDK，问「导出 PNG 用哪个接口」，系统给出准确调用链；
3. 黑盒第三方 Jar（无源码无文档）排查，运行时探针现场解剖并给出答案；
4. 遗留系统（无源码）的 API 使用指导。

### 2.3 功能需求

#### 2.3.1 构建期能力（方案 A：预生成）

- 输入源扫描：支持 `.class` / `.jar`（字节码通道）与 `.java`（源码通道）；
- 字节码解析：提取类、方法、签名、注解、异常、调用关系等确定性元数据；
- 源码解析（可选）：额外提取泛型、局部变量名、注释、javadoc 等丰富语义；
- 统一 IR 生成：两条通道归一为同一份中间表示（IR）；
- LLM 推理：消费 IR，生成功能导图、方法语义摘要、使用示例、关键词；
- Schema 校验：生成结果必须通过 `smartdoc.json` Schema 校验后归档；
- CI/CD 集成：以 CLI / Maven 插件形式接入构建流水线。

#### 2.3.2 运行期能力（主路径，方案 A 检索）

- 加载本地 `smartdoc.json` 知识库；
- 两层混合检索：倒排索引（BM25/FTS）粗筛 + 向量精排；
- 双层溯源定位：先命中包级功能导图，再沿 FQN 下钻到类/方法；
- 精准答案渲染：方法说明 + 参数 + 异常 + 调用示例。

#### 2.3.3 运行时探针兜底（方案 B：回源）

- 未命中或置信度不足时，触发运行时探针；
- 探针通过反射/反编译现场读取真实类结构与签名；
- 探针拿到结构后再调用 LLM 生成答案；
- 探针产出作为「一次性答案」返回，**不回写** `smartdoc.json`。

### 2.4 非功能需求

| 维度 | 要求 |
|------|------|
| 性能 | 运行期检索响应 < 20ms；构建期离线可接受分钟级耗时 |
| 安全 | 字节码通道零源码泄漏；`sourceChannels` 提供审计留痕 |
| 体积 | `runtime` SDK < 300KB，无第三方重型依赖 |
| 确定性 | 运行期结果完全由数据驱动，无概率输出 |
| 可扩展 | LLM 适配层支持多模型切换；IR 可增量演进 |
| 可测试 | 提供 Excel/GIS 双靶场，端到端可断言验证 |

---

## 3. 核心设计理念

### 3.1 确定性计算对冲不确定性

> 用编译期的「确定性计算」对冲运行期大模型的「不确定性」，让 AI 在企业级场景中真正可信、可靠、可落地。

- 构建期：一次性消耗 AI Token 完成深度推演，结果经校验后**冻结**；
- 运行期：纯Java内存检索，零概率、零幻觉、零成本；
- 探针兜底：仅在缓存未命中时按需"回源"，是一次性、边界化的火力投放。

### 3.2 三层数据流

| 层 | 内容 | 产出方 | 性质 |
|----|------|--------|------|
| L1 输入源 | `.class` / `.java` | 编译器 / 开发者 | 原始 |
| L2 统一 IR | 结构化代码元数据（类/方法/签名/注解/调用关系） | 解析器（ASM/AST） | 确定性、无 LLM |
| L3 smartdoc.json | 语义知识库（功能导图 + 类/方法描述 + 示例） | LLM | 语义、纯契约 |

关键原则：**IR 只含解析器能 100% 确定提取的信息，把不确定性全部留给 L3 的 LLM。**

---

## 4. 总体架构

### 4.1 端到端架构图

```
┌──────────────────── 构建期 Build-time（耗 AI Token）────────────────────┐
│                                                                         │
│  输入源（双通道）         解析器              统一 IR    →   LLM Agent    │
│  ├─ .jar/.class ───► ASM 字节码解析 ──┐                                    │
│  └─ .java ────────► AST 源码解析 ────┴──► 中间表示(IR) ──► smartdoc.json   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
                                  │ 打包 / 发布
                                  ▼
┌──────────────────── 运行期 Run-time（零 / 低成本）──────────────────────┐
│                                                                         │
│  用户提问 → 两层混合检索 ──命中且置信度够──► 直接返回精准答案               │
│                    │                                                     │
│                    └──未命中/低置信──► 运行时探针回源 ──► 现场解析 + LLM ──► 答案
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4.2 A+B 混合模式（缓存 + 回源）

本项目采用「方案 A（预生成）+ 方案 B（运行时探针）」混合架构，本质是 **预生成缓存 + 未命中回源**：

- **方案 A（主路径）**：构建期把确定部分提前算好，落地为 `smartdoc.json` 缓存；运行期检索命中即返回，毫秒级、零幻觉；
- **方案 B（兜底路径）**：缓存未命中或置信度不足时，运行时探针现场解剖真实类，动态生成答案，覆盖黑盒 Jar、遗留系统等 A 盲区。

### 4.3 路由与回源机制

| 阶段 | 动作 | 成本 | 幻觉风险 |
|------|------|------|----------|
| 主路径（A） | 检索 `smartdoc.json` 命中 | 极低 | 极低（构建期已定稿） |
| 兜底路径（B） | 探针反射/反编译现场解析 + LLM | 较高 | 中（LLM 现场推理） |

路由判据为「命中率/置信度」：检索命中且打分高 → 走 A；检索未命中或打分低于阈值 → 走 B。

---

## 5. 双通道输入与统一 IR（L2）

### 5.1 双通道设计

| 通道 | 解析对象 | 解析器 | 优势 | 缺失语义 |
|------|----------|--------|------|----------|
| 字节码（默认） | `.class` / `.jar` | ASM 9.x | 零源码泄漏，满足高安全合规 | 泛型擦除、局部变量名、javadoc、注释 |
| 源码（可选） | `.java` | AST（JavaParser 等） | 语义最完整，LLM 理解最准 | 需源码可见，不适配严格合规场景 |

二者归一为同一份 IR。LLM 只看 IR，不关心来源；`sourceChannels` 只用于审计，不进下游语义。

### 5.2 统一 IR 实体定义

| 实体 | 字段 | 类型 | 说明 |
|------|------|------|------|
| **ProjectIR** | `irVersion` | String | IR 自身版本，当前 `"0.1"` |
| | `projectName` | String | 被测工程名称 |
| | `sourceChannels` | List&lt;String&gt; | `bytecode` / `source`，可混合；仅审计 |
| | `packages` | List&lt;PackageIR&gt; | 包级聚合上下文 |
| | `classes` | List&lt;ClassIR&gt; | 类级全量索引，O(1) 定位 |
| **PackageIR** | `name` | String | 包全限定名 |
| | `classFqns` | List&lt;String&gt; | 包下所有类 FQN，溯源指针 |
| | `classSummary` | List&lt;String&gt; | 类签名/javadoc 精简摘要 |
| **ClassIR** | `fqn` | String | 全限定类名，唯一指针 |
| | `name` | String | 简单类名 |
| | `packageName` | String | 所在包名 |
| | `kind` | String | class / interface / enum / annotation / record |
| | `modifiers` | List&lt;String&gt; | 如 ["public","final"] |
| | `superClass` | String | 直接父类 FQN；无显式父类为 null |
| | `interfaces` | List&lt;String&gt; | 接口 FQN 列表 |
| | `typeParameters` | List&lt;String&gt; | 泛型参数；字节码通道通常为空 |
| | `annotations` | List&lt;String&gt; | 类级注解 |
| | `javadoc` | String | 类文档；仅源码通道，否则 null |
| | `fields` | List&lt;FieldIR&gt; | 字段列表 |
| | `methods` | List&lt;MethodIR&gt; | 方法列表 |
| | `dependencies` | List&lt;String&gt; | 类级依赖 FQN |
| **MethodIR** | `fqn` | String | 方法唯一指针（见 5.3） |
| | `name` | String | 方法名 |
| | `declaringClassFqn` | String | 声明类 FQN |
| | `modifiers` | List&lt;String&gt; | 如 ["public","static"] |
| | `returnType` | String | 返回值 FQN；构造方法为 null |
| | `typeParameters` | List&lt;String&gt; | 泛型参数 |
| | `parameters` | List&lt;ParameterIR&gt; | 形参列表 |
| | `throwsTypes` | List&lt;String&gt; | 受检异常 FQN |
| | `annotations` | List&lt;String&gt; | 方法级注解 |
| | `javadoc` | String | 方法文档；仅源码通道 |
| | `invokedMethods` | List&lt;String&gt; | 确定性调用关系图谱 |
| | `bytecodeSize` | Integer | 字节码大小；未采集为 null |
| **FieldIR** | `name` / `type` | String | 字段名 / 类型 FQN |
| | `modifiers` / `annotations` | List&lt;String&gt; | 修饰符 / 注解 |
| | `javadoc` | String | 字段文档；仅源码通道 |
| **ParameterIR** | `name` | String | 参数名；无调试信息可为 null |
| | `type` | String | 参数类型 FQN |
| | `annotations` | List&lt;String&gt; | 参数级注解 |
| | `javadoc` | String | 参数文档；仅源码通道 |

### 5.3 FQN 唯一指针编码规则

- 类指针 = 全限定类名，如 `com.example.gis.GeometryFixer`；
- 方法指针 = Javadoc 风格，如 `com.example.gis.GeometryFixer#simpleRepair(org.locationtech.jts.geom.Geometry)`；
- 参数类型使用全限定名，天然消解重载歧义，从导图下钻时能精准定位到具体重载。

### 5.4 IR 关键设计决策

1. **FQN 是唯一指针**，类/方法均通过 FQN 建立溯源与检索的锚点；
2. **`sourceChannels` 只用于审计**，不进 LLM prompt，保证双通道语义一致；
3. **调用关系是确定性图谱**，`dependencies` / `invokedMethods` 由解析器精确提取，是"准确调用链"与"功能导图"的硬基础；
4. **保留 null 字段**，不改用 `@JsonInclude(NON_NULL)`，让"字段固定存在、null 表达缺失"在 JSON 中显式呈现；
5. **Schema 放宽演进**，不设 `additionalProperties:false`，便于后期加字段不破坏旧契约。

### 5.5 IR JSON Schema

IR 契约已落盘为 JSON Schema 文件：`smartdoc-agent-core/src/main/resources/schema/ir-schema.json`（draft-07）。核心约束：

- 根对象必含 `irVersion`、`projectName`、`sourceChannels`、`packages`、`classes`；
- `irVersion` 固定为 `"0.1"`；
- `sourceChannels` 元素限定 `bytecode` / `source`；
- 可空字段（`superClass`、`javadoc`、`returnType`、`bytecodeSize`、参数 `name`）使用 `["类型", "null"]` 联合类型。

---

## 6. 检索策略（运行期主路径）

### 6.1 两层混合检索

| 层 | 引擎 | 职责 |
|----|------|------|
| 粗筛 | 倒排索引（BM25 / FTS） | 精准锁定类名、方法名、注解、FQN |
| 精排 | 向量 Embedding | 理解模糊业务需求意图 |
| 融合 | 重排序 | 两路结果融合，兼顾精确度与意图理解 |

### 6.2 包级功能导图 → 类/方法 FQN 双层溯源

1. 用户模糊提问 → 向量命中「包级功能导图」（L3 顶层）；
2. 导图节点携带 FQN 指针 → 沿 FQN 下钻到目标类；
3. 类内继续定位到方法 FQN → 返回精确调用示例。

> 导图本身在 L3（LLM 生成），但导图骨架与 FQN 指针均来自 IR 的 `packages[]` + FQN。

---

## 7. 工程模块结构

### 7.1 Maven 多模块划分

```
smart-doc-agent/                      (根工程 POM)
├── smartdoc-agent-core/              # 核心解析与 AI 推演引擎（当前已落地 IR）
│   ├── ir/                           # 统一 IR 实体与 Schema
│   ├── bytecode/                     # ASM 9.x 字节码解析器（待实现）
│   ├── source/                       # AST 源码解析器（待实现，可选通道）
│   ├── ai/                           # LLM 适配器（OpenAPI 兼容）与 Prompt 模板
│   └── generator/                    # smartdoc.json 生成与 Schema 校验
├── smartdoc-agent-cli/               # 命令行工具（Picocli），用于 CI/CD
├── smartdoc-agent-runtime/           # 嵌入宿主的轻量 SDK（< 300KB）
│   ├── loader/                       # Classpath 本地 json 加载
│   ├── index/                        # 内存倒排索引
│   └── search/                       # 混合检索 + 重排序
├── smartdoc-agent-probe/             # 运行时探针（方案 B 兜底）
├── smartdoc-agent-maven-plugin/      # Maven 构建自动打包插件（可选）
├── smartdoc-agent-testbeds/          # 演练靶场
│   ├── testbed-excel/                # Excel 导入校验（异常提取验证）
│   └── testbed-gis/                  # GIS 图形修复（算法意图提取验证）
└── smartdoc-agent-demo/              # 集成展示（Web + Swing 双模）
```

### 7.2 各模块职责

- **core**：双通道解析、统一 IR、LLM 适配、`smartdoc.json` 生成与校验——构建期全部核心逻辑；
- **cli**：将 core 能力封装为命令行，供 CI/CD 调用；
- **runtime**：运行期主路径的检索 SDK，零第三方重型依赖；
- **probe**：运行期兜底的探针能力，反射/反编译现场解析（可与 runtime 解耦）；
- **maven-plugin**：构建自动触发的薄封装；
- **testbeds**：两个确定性靶场，验证异常提取与算法意图提取；
- **demo**：Web（Spring Boot + localhost:8080）与 Swing 双模态交互验证。

### 7.3 当前进展

已落地：根 POM、`smartdoc-agent-core` 模块 POM、6 个 IR 实体（record）、IR JSON Schema，并通过编译验证（`mvn compile` 成功）。

---

## 8. 核心数据契约 smartdoc.json（L3）

`smartdoc.json` 是连接构建期 CLI 与运行期 Runtime 的标准化语义知识库，采用「顶层功能导图 + 底层类/方法描述」双层结构：

```json
{
  "version": "1.0.0",
  "generatedTime": "2026-09-07T00:00:00Z",
  "projectName": "shape-tool",
  "packages": [
    {
      "name": "com.example.gis",
      "summary": "GIS 几何图形容错与修复相关能力",
      "entryClassFqns": ["com.example.gis.GeometryFixer"]
    }
  ],
  "classes": [
    {
      "fqn": "com.example.gis.GeometryFixer",
      "aiSummary": "GIS 几何图形容错修复工具类",
      "methods": [
        {
          "methodFqn": "com.example.gis.GeometryFixer#simpleRepair(org.locationtech.jts.geom.Geometry)",
          "description": "通过零距离缓冲区消除自相交与无效顶点",
          "parameters": [
            { "name": "g", "type": "Geometry", "desc": "待修复几何对象" }
          ],
          "exceptions": ["TopologyException"],
          "usageExample": "Geometry fixed = fixer.simpleRepair(rawGeo);",
          "keywords": ["修复", "自相交", "缓冲区", "GIS", "多边形"]
        }
      ]
    }
  ]
}
```

> 说明：L3 Schema 将在实现 `generator` 模块时进一步细化，本文先给出结构示意（融合了「包级功能导图 → 类/方法 FQN」双层溯源）。

---

## 9. 关键技术决策记录（ADR）

| # | 决策 | 结论 | 状态 |
|---|------|------|------|
| ADR-1 | 项目命名 | 采用 `SmartDoc-Agent`（模块前缀 `smartdoc-agent-`） | 已确认 |
| ADR-2 | 输入源 | 双通道（字节码 + 源码），默认字节码 | 已确认 |
| ADR-3 | 统一 IR | 中间表示只含确定性信息，由解析器产出 | 已确认 |
| ADR-4 | 架构模式 | A+B 混合（预生成缓存 + 探针回源） | 已确认 |
| ADR-5 | 检索策略 | 两层混合（BM25/FTS 粗筛 + 向量精排） | 已确认 |
| ADR-6 | 溯源方式 | 包级功能导图 → 类/方法 FQN 双层溯源 | 已确认 |
| ADR-7 | 探针兜底 | 探针拿到结构后也调 LLM | 已确认 |
| ADR-8 | 缓存回写 | 不回写，保持 `smartdoc.json` 构建期纯洁确定性 | 已确认 |
| ADR-9 | LLM 接入 | 初期兼容 OpenAPI 规范即可（可切任意兼容模型） | 已确认 |
| ADR-10 | IR 定义 | 由主程定义 v0.1，随项目推进完善 | 已确认 |

---

## 10. 技术选型清单

| 类别 | 选型 | 说明 |
|------|------|------|
| 语言 / 运行时 | Java 17（LTS） | record 等现代特性 |
| 字节码解析 | ASM 9.x | 高性能、无依赖 |
| 源码解析（可选） | JavaParser（或等价 AST） | 源码通道 |
| 反编译辅助 | FernFlower（QuiltFlower） | 提升 AI 理解度 |
| LLM 集成 | LangChain4j / OkHttp | 兼容 OpenAPI，支持多模型 |
| 命令行解析 | Picocli | 注解驱动、彩色输出 |
| JSON 处理 | Jackson 2.16.x | record 序列化、树模型 + 流式 |
| 检索引擎（粗筛） | 倒排索引 / BM25（Lucene 或自研轻量版） | 精确锁定 |
| 检索引擎（精排） | Embedding 向量库 | 意图理解 |
| 测试界面 | Spring Boot + Thymeleaf | Web 演示 |
| 桌面交互 | Java Swing | 纯桌面验证 |
| 构建工具 | Maven 多模块 | 严格版本管控 |
| 单元测试 | JUnit 5 + AssertJ | 断言式验证 |

---

## 11. 演进路线图

**阶段一（当前）：基础骨架与端到端跑通**

1. 掌握 ASM 字节码解析与 Java 类加载机制；
2. 跑通「CI CLI → AI 提取 → 生成 JSON → 内存检索」全流程；
3. 落地统一 IR、双通道解析器、`smartdoc.json` 生成器；
4. 建立熟练的 Java 模块化开发与工程化思想。

**阶段二：Agent 化与 MCP 协议集成**

1. 将 SmartDoc-Agent 封装为符合 MCP（Model Context Protocol）规范的 Server；
2. 作为 Cursor、Claude Desktop 等 AI 编程环境的本地插件，主动回答类库使用难题；
3. 演化为真正意义上的「主动式 AI 软件陪练」。

---

## 12. 后续规划与待办

- [ ] 实现 `core/bytecode`：ASM 字节码解析器，抽取 ClassIR / MethodIR；
- [ ] 实现 `core/source`：AST 源码解析器（可选通道）；
- [ ] 实现 `core/ai`：OpenAPI 兼容 LLM 适配器 + Prompt 模板；
- [ ] 实现 `core/generator`：`smartdoc.json` 生成与 Schema 校验；
- [ ] 细化 `smartdoc.json` 完整 JSON Schema（L3）；
- [ ] 实现 `runtime`：倒排索引 + 向量检索 + 重排序；
- [ ] 实现 `probe`：运行时探针兜底；
- [ ] 实现 `cli`：Picocli 命令行入口；
- [ ] 建立 `testbeds`（Excel / GIS）与端到端自动化断言；
- [ ] 搭建 `demo`：Web + Swing 双模交互验证。

---

## 13. 附录 A：统一 IR 完整示例

```json
{
  "irVersion": "0.1",
  "projectName": "shape-tool",
  "sourceChannels": ["bytecode"],
  "packages": [
    {
      "name": "com.example.gis",
      "classFqns": ["com.example.gis.GeometryFixer"],
      "classSummary": [
        "GeometryFixer : public final class - GIS 几何容错修复工具"
      ]
    }
  ],
  "classes": [
    {
      "fqn": "com.example.gis.GeometryFixer",
      "name": "GeometryFixer",
      "packageName": "com.example.gis",
      "kind": "class",
      "modifiers": ["public", "final"],
      "superClass": "java.lang.Object",
      "interfaces": [],
      "typeParameters": [],
      "annotations": [],
      "javadoc": null,
      "fields": [],
      "methods": [
        {
          "fqn": "com.example.gis.GeometryFixer#simpleRepair(org.locationtech.jts.geom.Geometry)",
          "name": "simpleRepair",
          "declaringClassFqn": "com.example.gis.GeometryFixer",
          "modifiers": ["public"],
          "returnType": "org.locationtech.jts.geom.Geometry",
          "typeParameters": [],
          "parameters": [
            {
              "name": "g",
              "type": "org.locationtech.jts.geom.Geometry",
              "annotations": [],
              "javadoc": null
            }
          ],
          "throwsTypes": [],
          "annotations": [],
          "javadoc": null,
          "invokedMethods": [
            "org.locationtech.jts.operation.buffer.BufferOp#bufferOp(Geometry, double)"
          ],
          "bytecodeSize": 482
        }
      ],
      "dependencies": ["org.locationtech.jts.geom.Geometry"]
    }
  ]
}
```

> 注：本例 `sourceChannels = ["bytecode"]`，故所有 `javadoc` 均为 `null`，正好演示"源码通道独有字段在字节码通道下为空"的语义。