package com.smartdoc.agent.core.ir;

import java.util.List;

/**
 * 类级表示，FQN 是唯一指针。
 *
 * @param fqn            全限定类名，如 "com.example.gis.GeometryFixer"
 * @param name           简单类名，如 "GeometryFixer"
 * @param packageName    所在包名，如 "com.example.gis"
 * @param kind           类类型：class / interface / enum / annotation / record
 * @param modifiers      修饰符列表，如 ["public", "final"]
 * @param superClass     直接父类 FQN；接口等无显式父类时为 null
 * @param interfaces     实现的接口 FQN 列表
 * @param typeParameters 泛型类型参数列表；字节码通道因类型擦除通常为空
 * @param annotations    类级注解列表，如 ["@Service"]
 * @param javadoc        类文档注释；仅源码通道可获得，字节码通道为 null
 * @param fields         字段列表
 * @param methods        方法列表
 * @param dependencies   类级依赖（引用的外部类型 FQN 列表）
 */
public record ClassIR(
        String fqn,
        String name,
        String packageName,
        String kind,
        List<String> modifiers,
        String superClass,
        List<String> interfaces,
        List<String> typeParameters,
        List<String> annotations,
        String javadoc,
        List<FieldIR> fields,
        List<MethodIR> methods,
        List<String> dependencies
) {
}