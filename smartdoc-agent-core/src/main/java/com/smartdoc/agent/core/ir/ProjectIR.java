package com.smartdoc.agent.core.ir;

import java.util.List;

/**
 * 统一 IR 顶层容器，对应一个被测程序 / 工程。
 * <p>
 * IR 是「解析器输出、LLM 输入」的确定性代码元数据中间表示（L2），
 * 只包含解析器能 100% 确定提取出的信息，不掺入任何 AI 推理结果。
 *
 * @param irVersion      IR 自身版本号（当前为 "0.1"）
 * @param projectName    被测工程名称
 * @param sourceChannels 输入来源通道：{@code "bytecode"} / {@code "source"}，可混合。
 *                       仅用于审计与合规追溯，不参与下游 LLM 语义。
 * @param packages       包级聚合上下文（供 LLM 生成顶层功能导图）
 * @param classes        扁平的类级全量索引，以 FQN 作为唯一指针，便于 O(1) 定位
 */
public record ProjectIR(
        String irVersion,
        String projectName,
        List<String> sourceChannels,
        List<PackageIR> packages,
        List<ClassIR> classes
) {
}