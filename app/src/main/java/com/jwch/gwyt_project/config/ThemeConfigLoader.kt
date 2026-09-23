package com.jwch.gwyt_project.config

import com.jameni.allutillib.common.PrintUtil
import com.jwch.gwyt_project.Info.CatTypeInfo
import com.jwch.gwyt_project.Info.ThemesInfo
import com.jwch.gwyt_project.util.RegionConfigManager
import org.json.JSONObject

/**
 * 专题配置加载器（纯内存模式）
 * JSON优先，DB回退的策略：
 * 1. 优先从 assets 中读取 JSON 配置（由 AppContext 传入）
 * 2. 如果 JSON 加载失败，回退到 DB 查询
 */
object ThemeConfigLoader {

    private var catTypeList: MutableList<CatTypeInfo>? = null
    private var themeList: MutableList<ThemesInfo>? = null
    private var loaded = false
    private var jsonAvailable = false

    /**
     * 延迟注入的 region 直连 themes，key = parentCatTypeId
     * 这些 themes 不混入 themeList，而是在 arrangeZttListData 中
     * 追加到子分类之后，从而保持 JSON 中 categories → themes 的顺序
     */
    private var injectedDirectThemes: MutableMap<String, MutableList<ThemesInfo>> = mutableMapOf()

    /**
     * 初始化：从内存中的 JSON 字符串加载配置（不再读写文件）
     * @param jsonStr assets 中的 JSON 字符串，为 null 时回退到 DB
     * @return true 表示 JSON 配置可用，false 表示需要回退到 DB
     */
    @Synchronized
    fun init(jsonStr: String?): Boolean {
        if (loaded) return jsonAvailable

        try {
            if (jsonStr.isNullOrEmpty()) {
                PrintUtil.printMsg("ThemeConfigLoader: JSON 配置为空，将使用 DB 查询")
                loaded = true
                jsonAvailable = false
                return false
            }

            val jsonObj = JSONObject(jsonStr)
            val version = jsonObj.optInt("version", 0)
            PrintUtil.printMsg("ThemeConfigLoader: 加载 JSON 配置，version=$version")

            catTypeList = mutableListOf()
            themeList = mutableListOf()
            injectedDirectThemes = mutableMapOf()

            // 递归解析嵌套树形结构
            val categories = jsonObj.optJSONArray("categories")
            if (categories != null) {
                for (i in 0 until categories.length()) {
                    parseCategory(categories.getJSONObject(i), "0")
                }
            }

            // 注入地区定制图层到「基础数据」分类下
            injectRegionThemes(jsonObj)

            loaded = true
            jsonAvailable = catTypeList!!.isNotEmpty() && themeList!!.isNotEmpty()
            PrintUtil.printMsg("ThemeConfigLoader: JSON 配置加载完成, catTypes=${catTypeList?.size}, themes=${themeList?.size}")
            return jsonAvailable
        } catch (e: Exception) {
            PrintUtil.printMsg("ThemeConfigLoader: JSON 配置加载失败: ${e.message}")
            e.printStackTrace()
            loaded = true
            jsonAvailable = false
            return false
        }
    }

    /**
     * 递归解析分类节点，展开为扁平列表
     */
    private fun parseCategory(obj: JSONObject, parentId: String) {
        val info = CatTypeInfo()
        // 如果没有 id，根据 parentId + typeName 生成确定性 UUID
        info.id = obj.optString("id", "").let {
            if (it.isNotEmpty()) it
            else genId("$parentId:${obj.optString("typeName", "")}")
        }
        info.typeName = obj.optString("typeName", "")
        info.parentId = parentId
        info.sortCode = obj.optInt("sortCode", 0)
        catTypeList!!.add(info)

        // 解析当前分类下的主题
        val themesArr = obj.optJSONArray("themes")
        if (themesArr != null) {
            for (i in 0 until themesArr.length()) {
                val t = themesArr.getJSONObject(i)
                val theme = ThemesInfo()
                // 如果没有 id，根据 catTypeId + themeName 生成确定性 UUID
                theme.id = t.optString("id", "").let {
                    if (it.isNotEmpty()) it
                    else genId("${info.id}:${t.optString("themeName", "")}")
                }
                theme.catTypeId = info.id
                theme.themeName = t.optString("themeName", "")
                theme.setData()
                themeList!!.add(theme)
            }
        }

        // 递归解析子分类
        val children = obj.optJSONArray("children")
        if (children != null) {
            for (i in 0 until children.length()) {
                parseCategory(children.getJSONObject(i), info.id)
            }
        }
    }

    /** 根据输入生成确定性 UUID */
    private fun genId(input: String): String {
        return java.util.UUID.nameUUIDFromBytes(input.toByteArray(Charsets.UTF_8)).toString()
    }

    /**
     * 根据当前地区，将地区定制图层注入到「基础数据」分类
     *
     * 支持两种模式：
     * 1. categories: 注入带中间分类的图层（如「控制区」→ 核心控制区/一般控制区）
     * 2. themes: 直接将图层注入「基础数据」下，无中间分类（如永春县）
     */
    private fun injectRegionThemes(jsonObj: JSONObject) {
        val regionThemes = jsonObj.optJSONObject("regionThemes") ?: return

        val regionName = try {
            RegionConfigManager.getCurrentRegionName()
        } catch (e: Exception) { "" }

        if (regionName.isEmpty()) {
            PrintUtil.printMsg("ThemeConfigLoader: 未设置地区，跳过地区图层注入")
            return
        }

        val regionConfig = regionThemes.optJSONObject(regionName) ?: return

        // 找到「基础数据」分类
        val baseDataCat = catTypeList?.find { it.typeName == "基础数据" } ?: run {
            PrintUtil.printMsg("ThemeConfigLoader: 未找到「基础数据」分类，跳过地区图层注入")
            return
        }

        PrintUtil.printMsg("ThemeConfigLoader: 注入地区定制图层 [$regionName]")

        // 计算基础数据现有子分类的最大 sortCode，地区内容排到最后
        var maxSort = 0
        catTypeList?.filter { it.parentId == baseDataCat.id }?.forEach {
            if (it.sortCode > maxSort) maxSort = it.sortCode
        }

        // 模式1：有 categories 中间分类
        val regionCategories = regionConfig.optJSONArray("categories")
        if (regionCategories != null && regionCategories.length() > 0) {
            for (i in 0 until regionCategories.length()) {
                val child = regionCategories.getJSONObject(i)
                // 始终用动态计算的 sortCode 覆盖，确保地区分类排在「基础数据」子分类的最后
                child.put("sortCode", maxSort + i + 1)
                parseCategory(child, baseDataCat.id)
            }
        }

        // 模式2：themes 直接挂在「基础数据」下，无中间分类
        // 使用延迟注入，后续由 arrangeZttListData 追加到子分类之后
        val regionDirectThemes = regionConfig.optJSONArray("themes")
        if (regionDirectThemes != null && regionDirectThemes.length() > 0) {
            val list = injectedDirectThemes.getOrPut(baseDataCat.id) { mutableListOf() }
            for (i in 0 until regionDirectThemes.length()) {
                val t = regionDirectThemes.getJSONObject(i)
                val theme = ThemesInfo()
                theme.id = t.optString("id", "").let {
                    if (it.isNotEmpty()) it
                    else genId("${baseDataCat.id}:${t.optString("themeName", "")}")
                }
                theme.catTypeId = baseDataCat.id
                theme.themeName = t.optString("themeName", "")
                theme.setData()
                list.add(theme)
            }
            PrintUtil.printMsg("ThemeConfigLoader: 注入地区直接图层 ${regionDirectThemes.length()} 个（延迟注入）")
        }
    }

    /**
     * 是否从 JSON 加载成功
     */
    fun isJsonAvailable(): Boolean = jsonAvailable

    /**
     * 获取"专题数据"这个类型的 Id
     * 对应 DB: queryThemeMapTypeId()
     */
    fun queryThemeMapTypeId(): String? {
        if (!jsonAvailable) return null
        return catTypeList?.find {
            it.parentId == "0" && it.typeName.contains("专题数据")
        }?.id
    }

    /**
     * 通过 parentId 获取子类别列表
     * 对应 DB: queryCatTypeListLevel(parentId)
     */
    fun queryCatTypeListLevel(parentId: String = "0"): MutableList<CatTypeInfo>? {
        if (!jsonAvailable) return null
        val list = catTypeList?.filter { it.parentId == parentId }?.toMutableList() ?: mutableListOf()
        list.sortBy { it.sortCode }
        return list
    }

    /**
     * 通过 Id 获取主题
     * 对应 DB: queryThemesById(id)
     */
    fun queryThemesById(id: String): ThemesInfo? {
        if (!jsonAvailable) return null
        return themeList?.find { it.id == id }
    }

    /**
     * 通过分类 Id 获取专题图层列表
     * 对应 DB: queryThemesByTypeId(typeId)
     */
    fun queryThemesByTypeId(typeId: String): MutableList<ThemesInfo>? {
        if (!jsonAvailable) return null
        val list = themeList?.filter { it.catTypeId == typeId }?.toMutableList() ?: mutableListOf()
        return list
    }

    /**
     * 获取延迟注入的 region 直连 themes（排在子分类之后）
     */
    fun queryInjectedDirectThemes(parentId: String): MutableList<ThemesInfo>? {
        if (!jsonAvailable) return null
        return injectedDirectThemes[parentId]
    }

    /**
     * 强制重新加载
     */
    @Synchronized
    fun reload(jsonStr: String?) {
        catTypeList = null
        themeList = null
        injectedDirectThemes = mutableMapOf()
        loaded = false
        jsonAvailable = false
        init(jsonStr)
    }
}