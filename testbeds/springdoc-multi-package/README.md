# 最小 Spring Boot 文档测试服务

单服务、四个 Java 包、两个显式 OpenAPI 分组。Java 17，Spring Boot 3.5.9，
springdoc WebMVC UI 2.8.15；版本组合参照 [springdoc 2.8.15 发布记录](https://github.com/springdoc/springdoc-openapi/releases/tag/v2.8.15)。
提供 Swagger UI 浏览接口；无数据库、注册中心、网关或 Knife4j，数据均为虚构，不持久化订单和上传文件。
Bearer 安全定义用于测试文档元数据，服务没有实现认证。

从仓库根目录运行（Maven 3.6.3+、JDK 17）：

```powershell
mvn -f testbeds/springdoc-multi-package/pom.xml test
mvn -f testbeds/springdoc-multi-package/pom.xml spring-boot:run
```

手动启动默认仅监听 `127.0.0.1:18080`，用 Ctrl+C 停止。独立 POM 不依赖根工程或 core。

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

本服务完成 P1.3 的真实文档样例。`compile` 不启动服务或导出文档，也尚未生成 Skill；
生产编译接入、失败隔离、多服务和多模块验证仍属于后续任务。
