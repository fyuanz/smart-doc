package com.smartdoc.agent.core.ir;

import java.util.List;

/**
 * 方法级表示，方法 FQN 是唯一指针。
 * <p>
 * 方法指针采用 Javadoc 风格："类FQN#方法名(参数类型FQN列表)"，可天然消解重载歧义。
 *
 * @param fqn               方法唯一指针，如 "com.example.gis.GeometryFixer#simpleRepair(org.locationtech.jts.geom.Geometry)"
 * @param name              方法名
 * @param declaringClassFqn 声明该方法的类 FQN
 * @param modifiers         修饰符列表，如 ["public", "static"]
 * @param returnType        返回值类型 FQN；构造方法为 null
 * @param typeParameters    泛型类型参数列表；字节码通道通常为空
 * @param parameters        形参列表
 * @param throwsTypes       声明的受检异常 FQN 列表
 * @param annotations       方法级注解列表，如 ["@Deprecated"]
 * @param javadoc           方法文档注释；仅源码通道可获得
 * @param invokedMethods    方法级调用关系（确定性图谱），被调用方法 FQN 列表
 * @param bytecodeSize      字节码大小（字节数，可选），粗略衡量复杂度；未采集时为 null
 */
public record MethodIR(
        String fqn,
        String name,
        String declaringClassFqn,
        List<String> modifiers,
        String returnType,
        List<String> typeParameters,
        List<ParameterIR> parameters,
        List<String> throwsTypes,
        List<String> annotations,
        String javadoc,
        List<String> invokedMethods,
        Integer bytecodeSize
) {
}