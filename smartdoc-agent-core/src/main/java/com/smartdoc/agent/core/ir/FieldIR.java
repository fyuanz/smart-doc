package com.smartdoc.agent.core.ir;

import java.util.List;

/**
 * 字段级表示（可选，低频但为完整性保留）。
 *
 * @param name        字段名
 * @param type        字段类型 FQN
 * @param modifiers   修饰符列表，如 ["private", "static", "final"]
 * @param annotations 注解列表
 * @param javadoc     字段文档注释；仅源码通道可获得
 */
public record FieldIR(
        String name,
        String type,
        List<String> modifiers,
        List<String> annotations,
        String javadoc
) {
}