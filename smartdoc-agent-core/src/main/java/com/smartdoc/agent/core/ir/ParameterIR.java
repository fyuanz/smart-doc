package com.smartdoc.agent.core.ir;

import java.util.List;

/**
 * 方法形参表示。
 *
 * @param name        参数名；字节码通道可能因未携带调试信息而为 null 或占位名
 * @param type        参数类型 FQN
 * @param annotations 参数级注解列表
 * @param javadoc     参数文档注释；仅源码通道可获得
 */
public record ParameterIR(
        String name,
        String type,
        List<String> annotations,
        String javadoc
) {
}