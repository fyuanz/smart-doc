# 最小 Spring Boot 文档测试服务

单服务、四个 Java 包、两个显式 OpenAPI 分组。Java 17，Spring Boot 3.5.9，
springdoc WebMVC UI 2.8.15；版本组合参照 [springdoc 2.8.15 发布记录](https://github.com/springdoc/springdoc-openapi/releases/tag/v2.8.15)。
提供 Swagger UI 浏览接口；无数据库、注册中心、网关或 Knife4j，数据均为虚构，不持久化订单和上传文件。
Bearer 安全定义用于测试文档元数据，服务没有实现认证。

从仓库根目录运行（Maven 3.6.3+、JDK 17）：

```powershell
mvn -f testbeds/springdoc-multi-package/pom.xml test
mvn -f testbeds/springdoc-multi-package/pom.xml spring-boot:run
powershell -NoProfile -File testbeds/springdoc-multi-package/verify-generated-integration.ps1
```

手动启动默认仅监听 `127.0.0.1:18080`，用 Ctrl+C 停止。独立 POM 不依赖根工程或 core。
生成接入验证脚本会先把当前 SmartDoc 插件安装到本地 Maven 仓库，再为 HTTP/JMX 选择空闲端口；它不会
占用或停止手动运行的 `18080` 服务。

启动后打开 [Swagger UI](http://127.0.0.1:18080/swagger-ui.html)。通过页面顶部的
`Select a definition` 切换 `account`（用户接口）和 `business`（订单、文件接口），
展开接口即可查看参数、请求体、响应和 Schema，也可使用 `Try it out` 调用本地样例接口。
UI 使用现有两份分组 JSON；接入方式见 [springdoc 官方文档](https://springdoc.org/v2/)。

| 分组 | 文档地址 | 接口 |
| --- | --- | --- |
| account | http://127.0.0.1:18080/v3/api-docs/account | GET /users、GET /users/{id} |
| business | http://127.0.0.1:18080/v3/api-docs/business | POST /orders、POST /files |

`GET /users/1` 返回示例用户；其他编号返回 404。`GET /users?keyword=示例` 过滤名称。
`POST /orders` 接收 `{"userId":1,"quantity":2,"shippingAddress":{"city":"示例市","street":"示例路"}}`，
校验后以 201 回显；字段校验失败返回 400。`POST /files` 接收 multipart 的 `file` 字段，最多 1 MB。

测试包括真实随机端口 HTTP 文档获取，以及 MockMvc 接口/校验测试。断言精确 `3.1.0`、分组路径、
中文描述、参数位置、请求/响应、必填和最小值、共享与递归引用、multipart、鉴权定义。
两组全部断言通过后才写入 `target/openapi/`；测试关闭应用上下文，Surefire 限制测试进程 120 秒。
固定 `servers` 为默认演示地址，避免随机测试端口污染快照；自定义运行端口时它仍是默认演示地址。

刷新可提交的冻结输入（运行全部测试成功后复制，不自动提交 Git）：

```powershell
powershell -NoProfile -File testbeds/springdoc-multi-package/refresh-fixtures.ps1
```

`fixtures/account.json` 和 `fixtures/business.json` 供后续 core 离线测试使用；
`fixtures/metadata.json` 记录版本、来源端点、SHA-256、操作/Schema 数量和生产源码摘要。
普通 `test` 只更新 `target`，不会修改冻结输入。刷新后应一并审查两份 JSON 和 metadata 的差异。

`verify-generated-integration.ps1` 验证另一条独立链路：`pre-integration-test` 启动已编译应用，
springdoc Maven Plugin 1.5 在 `integration-test` 分别抓取两组 JSON，`post-integration-test` 停止应用，
SmartDoc 在 `verify` 扫描 `target/generated-openapi/`，检查发现的 JSON 属于本次 Maven 会话后发布 Skill。
脚本随后让抓取连接失败，确认旧 JSON
不会被当成本次结果，原 Skill 保持不变且 Maven 构建成功。
Skill 输出位于 `target/generated-resources/smartdoc/`，执行 `clean` 后不会保留。

这条运行时路径要求 `mvn verify`；普通 `compile` 和 `package` 不会到达集成测试阶段。Spring Boot
启动目标本身的失败仍会终止 Maven 构建，因此该样例是可复现的接入证据，不是默认生产配置。
