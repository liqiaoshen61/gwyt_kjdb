package com.jwch.gwyt_project.util

import java.util.AbstractMap

object MapConverter {
    // 定义映射关系结构
    data class FieldMapping(val key: String, val value: String, val sort: Int)

    // 初始化预定义的映射关系（根据提供的JSON结构）
    val MAPPINGS: List<FieldMapping> = listOf(
        FieldMapping("平台编", "平台编号", 0),
        FieldMapping("事件类", "事件类型", 1),
        FieldMapping("年份", "年份", 1),
        FieldMapping("下发时", "下发时间", 1),
        FieldMapping("问题编", "问题编号", 2),
        FieldMapping("河湖名", "河湖名", 3),
        FieldMapping("所在市", "所在市", 4),
        FieldMapping("所在县", "所在县", 5),
        FieldMapping("所在镇", "所在镇", 6),
        FieldMapping("所在村", "所在村", 7),
        FieldMapping("问题类", "问题类型", 8),
        FieldMapping("属性", "属性", 9),
        FieldMapping("程度", "程度", 10),
        FieldMapping("经度", "经度", 11),
        FieldMapping("纬度", "纬度", 12),
        FieldMapping("基期时", "基期时", 13),
        FieldMapping("当期时", "当期时", 14),
        FieldMapping("原地类", "原地类", 15),
        FieldMapping("现地类", "现地类", 16),
        FieldMapping("问题描", "问题描述", 17),
        FieldMapping("严重程", "严重程度", 18),
        FieldMapping("是否问", "是否问题", 19),
        FieldMapping("不是问", "不是问题原因", 20),
        FieldMapping("事件状", "事件状态", 21),
        FieldMapping("图斑处", "图斑处理流程", 22),
        FieldMapping("不认领", "不认领具体原因说明", 23),
        FieldMapping("位置", "位置", 24),
        //河道
        FieldMapping("HHDM", "河湖代码", 25),
        FieldMapping("HHMC", "河湖名称", 26),
        FieldMapping("XZQ", "行政区", 27),
        FieldMapping("length", "长度", 27),

        //河流
        FieldMapping("RV_CODE", "河流编号", 38),
        FieldMapping("RV_NAME", "河流名称", 39),

        //水葫芦
        FieldMapping("县级", "县级", 28),
        FieldMapping("周长", "周长", 29),
        FieldMapping("地级", "地级", 30),
        FieldMapping("影像时", "影像时间", 31),
        FieldMapping("河流名", "河流名", 32),
        FieldMapping("面积", "面积", 33),

        //水利部图斑
        FieldMapping("编码", "编码", 33),
        FieldMapping("地物对", "地物对象", 31),
        FieldMapping("图斑大", "图斑大类", 30),
        FieldMapping("河段", "河段(湖片)名称", 33),
//        FieldMapping("省级", "省级", 33),
//        FieldMapping("市级", "市级", 33),
        FieldMapping("周长", "周长", 34),
        FieldMapping("影像采", "影像采集日期", 35),
        FieldMapping("推荐日", "推荐日期", 36),
        FieldMapping("复核状", "复核状态", 37),

        FieldMapping("水利类", "水利类类型", 38),
        FieldMapping("河湖库", "河湖库名称", 39),
        FieldMapping("状态", "状态", 40),

        FieldMapping("年度", "水利类类型", 41),
        FieldMapping("名称", "项目名称", 42),
        FieldMapping("业主", "项目业主", 43),
        FieldMapping("坐标系", "坐标系", 44),

        //水电站字段
        FieldMapping("水电站", "水电站名称", 40),
        FieldMapping("建设运", "建设运营单位", 41),
        FieldMapping("投产时", "投产时间", 43),
        FieldMapping("开发方", "开发方式", 44),
        FieldMapping("电站设", "电站设计水头（m）", 44),
        FieldMapping("总装机", "总装机容量（kW）", 44),
        FieldMapping("总库容（", "总库容（万m³）", 44),
        FieldMapping("坝型", "坝型", 44),
        FieldMapping("最大坝", "最大坝高（m）", 44),

        //水库
        FieldMapping("水库名", "水库名称", 40),
        FieldMapping("所在河", "所在河流", 41),
        FieldMapping("工程规", "工程规模", 43),
        FieldMapping("功能类", "功能类别", 41),
        FieldMapping("坝型", "坝型", 43),
        FieldMapping("集雨面", "集雨面积", 43),
        FieldMapping("是否挂", "是否挂闸", 43),
        FieldMapping("校核洪", "校核洪水位", 43),
        FieldMapping("设计洪", "设计洪水位", 43),
        FieldMapping("正常蓄", "正常蓄水位", 43),
        FieldMapping("总库容", "总库容", 43),
        FieldMapping("坝高", "坝高", 43),
        FieldMapping("坝顶高", "坝顶高程", 43),
        FieldMapping("正常溢", "正常溢洪道堰顶高程", 43),
        FieldMapping("主汛期", "主汛期限制水位", 43),
        FieldMapping("后汛期", "后汛期限制水位", 43),

        FieldMapping("ZRBHDBM", "自然保护地面编码", 0),
        FieldMapping("ZRBHDNC", "自然保护地面名称", 1),
        FieldMapping("MJ", "面积", 2),
        FieldMapping("LX", "立标类型", 3),
    )

    // HashMap 查表，O(1)
    private val LOOKUP: Map<String, FieldMapping> = MAPPINGS.associateBy { it.key }

    /**
     * 转换方法：遍历输入 Map 的 key，通过 HashMap 查找映射关系（includeUnmapped 默认 true）
     * 性能：O(N log N)，N 为输入 Map 的条目数
     * @param inputMap 输入参数Map
     * @return 按 sort 排序的键值对列表
     */
    fun convert(inputMap: Map<String, Any>): List<Map.Entry<String, Any>> = convert(inputMap, true)

    /**
     * 转换方法：遍历输入 Map 的 key，通过 HashMap 查找映射关系
     * 性能：O(N log N)，N 为输入 Map 的条目数
     * @param inputMap 输入参数Map
     * @param includeUnmapped 是否包含无映射的字段，true 原样保留在末尾，false 则丢弃
     * @return 按 sort 排序的键值对列表
     */
    fun convert(inputMap: Map<String, Any>, includeUnmapped: Boolean): List<Map.Entry<String, Any>> {
        val entries = inputMap.entries
            .mapNotNull { (key, value) ->
                val mapping = LOOKUP[key]
                if (!includeUnmapped && mapping == null) return@mapNotNull null
                val displayKey = mapping?.value ?: key
                val sort = mapping?.sort ?: Int.MAX_VALUE
                Triple(displayKey, value, sort)
            }
            .sortedBy { it.third }
            .map { AbstractMap.SimpleEntry(it.first, it.second) }
        return entries
    }
}