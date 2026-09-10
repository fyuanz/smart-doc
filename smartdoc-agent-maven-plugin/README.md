# SmartDoc Agent Maven Plugin

The `generate-skill` goal reads one complete set of local OpenAPI 3.1.0 JSON documents and publishes one
service-owned Skill through the core updater. Its default phase is `compile`; configure the execution explicitly
in the one module that owns generation for the service.

```xml
<plugin>
  <groupId>com.smartdoc.agent</groupId>
  <artifactId>smartdoc-agent-maven-plugin</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <inherited>false</inherited>
  <executions>
    <execution>
      <id>update-orders-skill</id>
      <phase>compile</phase>
      <goals><goal>generate-skill</goal></goals>
    </execution>
  </executions>
  <configuration>
    <serviceId>orders</serviceId>
    <skillName>orders-api</skillName>
    <outputDirectory>${project.build.directory}/smartdoc</outputDirectory>
    <timeoutSeconds>30</timeoutSeconds>
    <documents>
      <document>
        <id>account</id>
        <path>${project.basedir}/src/main/openapi/account.json</path>
      </document>
      <document>
        <id>business</id>
        <path>${project.basedir}/src/main/openapi/business.json</path>
      </document>
    </documents>
  </configuration>
</plugin>
```

All configured documents are required. Configuration, read, conversion, timeout, locking, validation, and
publication failures are logged as service-specific warnings; they do not throw a Maven build failure. Once the
service, Skill, and output configuration is valid, each update attempt also writes its outcome to the status file.
Ordinary Java compilation and other Maven failures keep their normal exit status. The output parent contains
`<skillName>/` and updater state under `.smartdoc/`.

Local files under `src/main/openapi/` can be authoritative inputs and are read on every invocation. A generated
document must be produced earlier in the same build. Bind the goal after that producer and set
`<requireCurrentBuildDocuments>true</requireCurrentBuildDocuments>`; every configured document must then have been
rewritten no earlier than the current Maven session start and must remain unchanged while its bytes are read. An old
file left by a failed producer causes a warning and a failed update. The plugin does not start an application or
implement springdoc scanning. Bind the execution only in the service's generation-owner module so plugin inheritance
cannot create duplicate writers.

Runtime springdoc export requires compiled application classes, so the verified sample binds springdoc export to
`integration-test` and this goal to `verify`. It is a separate entry point from the static-document `compile` mode.
The testbed also shows that a springdoc HTTP capture failure can remain non-blocking; Spring Boot startup failure is
still a normal failure of the Spring Boot Maven plugin.

The default output is below `target`, so `clean` removes the previous Skill before compilation. Configure a
controlled durable output parent outside build output when failed clean builds must retain the last successful
Skill. Never point `outputDirectory` at a source root or a directory containing manual files.

Run the standalone lifecycle verification from the repository root:

```powershell
powershell -NoProfile -File testbeds/maven-plugin-integration/verify.ps1
powershell -NoProfile -File testbeds/springdoc-multi-package/verify-generated-integration.ps1
```
