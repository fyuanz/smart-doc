# Maven compile integration testbed

This standalone Java 17 reactor verifies the SmartDoc Maven goal against real Maven lifecycle invocations.
`orders-service` owns a two-document Skill and `billing-service` owns a one-document Skill. Both publish below
the reactor's `target/smartdoc/` parent. The plugin execution is declared only in each service owner module and
uses `<inherited>false>`.

The inputs under each module's `src/main/openapi/` are authoritative static OpenAPI sources for this testbed.
Reading them during every compile is current for that input model. This does not prove that runtime springdoc
documents are fresh during `compile`; a code-generated producer must separately provide and prove its preparation
step before the plugin runs.

From the repository root, run:

```powershell
powershell -NoProfile -File testbeds/maven-plugin-integration/verify.ps1
```

The verifier installs the plugin snapshot locally, then checks clean/ordinary/repeated/targeted/parallel compilation,
package traversal, per-service generation count and isolation, invalid/absent inputs, preserved prior output, first-run
failure, invalid configuration, blocked output, and an intentional Java compilation error. The broken source is
activated only by the `broken-business-build` profile.
