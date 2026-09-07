package com.smartdoc.agent.core.ir;

import java.util.List;

/**
 * 包级表示，用于支撑「顶层（包/功能级）功能导图」的生成。
 * <p>
 * 不包含类/方法详情，只聚合该包下各类的定位信息与摘要，供 LLM 总结功能导图。
 *
 * @param name         包全限定名，如 "com.example.gis"
 * @param classFqns    该包下所有类的 FQN，作为下钻溯源指针
 * @param classSummary 该包下各类签名/javadoc 的精简摘要（LLM 总结导图的素材）
 */
public record PackageIR(
        String name,
        List<String> classFqns,
        List<String> classSummary
) {
}