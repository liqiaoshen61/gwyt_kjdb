package com.jwch.gwyt_project.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.core.Config
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.security.MessageDigest

/** 独立的勘界作业目录及 SHP 导入入口。 */
class SurveyJobsController(
    private val activity: Activity,
    private val panel: FrameLayout,
    private val onOpen: (Job, Boolean) -> Unit,
    private val onClose: () -> Unit
) {
    data class Job(val id: String, var name: String, val file: String, var count: Long,
                   var savedAt: Long = 0, var geometry: String = "矢量", val source: String = "")

    companion object { private const val IMPORT_FOLDER = 28704; private const val PAGE_SIZE = 10 }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val prefs = activity.getSharedPreferences("survey_jobs", 0)
    private val root = File(activity.getExternalFilesDir(null) ?: activity.filesDir, "shp_workspace")
    private val jobs = try {
        Gson().fromJson(prefs.getString("jobs", "[]"), Array<Job>::class.java)?.toMutableList() ?: mutableListOf()
    } catch (_: Exception) { mutableListOf<Job>() }
    private var busy = false
    private var disposed = false
    private var searchQuery = ""
    private var currentPage = 0
    private var jobList: LinearLayout? = null
    private var pageLabel: TextView? = null
    private var previousPageButton: TextView? = null
    private var nextPageButton: TextView? = null
    val isVisible: Boolean get() = panel.visibility == View.VISIBLE

    init { migrateExistingJobs() }

    private fun migrateExistingJobs() {
        if (prefs.getBoolean("legacy_migrated", false)) return
        busy = true
        scope.launch {
            try {
                val migrated = withContext(Dispatchers.IO) {
                    val legacy = activity.getSharedPreferences("shp_editor", 0)
                    val names = mutableMapOf<String, String>()
                    File(Config.SHP_IMPORT_PATH).walkTopDown().filter { it.isFile && it.extension.equals("shp", true) }.forEach { shp ->
                        val key = MessageDigest.getInstance("SHA-256").digest(shp.canonicalPath.toByteArray())
                            .joinToString("") { "%02x".format(it) }
                        names["source_$key"] = shp.nameWithoutExtension
                    }
                    legacy.all.filterKeys { it.startsWith("source_") }.mapNotNull { (key, value) ->
                        val file = (value as? String)?.let { File(it) } ?: return@mapNotNull null
                        if (!file.isFile || !file.canonicalPath.startsWith(root.canonicalPath + File.separator)) return@mapNotNull null
                        val meta = metadata(file)
                        val name = names[key] ?: if (legacy.getString("file", null) == file.absolutePath)
                            legacy.getString("source_name", "已有作业") ?: "已有作业" else "已有作业"
                        Job(UUID.randomUUID().toString(), name, file.absolutePath, meta.first,
                            savedAt = file.lastModified(), geometry = meta.second)
                    }
                }
                migrated.forEach { job -> if (jobs.none { it.file == job.file }) jobs.add(job) }
                persist(); prefs.edit().putBoolean("legacy_migrated", true).apply()
            } catch (_: Exception) {
                // 保留旧记录，后续启动可重试迁移。
            } catch (_: LinkageError) {
                // 尚未准备好原生数据引擎时仍可使用导入入口。
            } finally { busy = false; if (!disposed && isVisible) render(); if (disposed) scope.cancel() }
        }
    }

    fun show() { panel.visibility = View.VISIBLE; render() }
    fun hide() { panel.visibility = View.GONE }
    private fun persist() { prefs.edit().putString("jobs", Gson().toJson(jobs)).apply() }
    private fun dp(value: Int) = (value * activity.resources.displayMetrics.density).toInt()
    private fun label(value: String, size: Float = 14f) = TextView(activity).apply {
        text = value; textSize = size; setTextColor(Color.WHITE); setPadding(dp(8), dp(8), dp(8), dp(8))
    }
    private fun button(title: String, click: () -> Unit) = Button(activity).apply {
        text = title; textSize = 14f; setTextColor(Color.WHITE)
        setBackgroundResource(R.drawable.bg_import_btn)
        layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { setMargins(dp(4), dp(3), dp(4), dp(3)) }
        isEnabled = !busy
        setOnClickListener { if (!busy) click() }
    }

    private fun render() {
        if (disposed) return
        panel.removeAllViews()
        jobList = null; pageLabel = null; previousPageButton = null; nextPageButton = null
        val content = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(10), dp(10), dp(10)) }
        panel.addView(content, FrameLayout.LayoutParams(-1, -1))
        val header = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL }
        header.addView(label("勘界作业", 20f), LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(button("关闭") { hide(); onClose() }, LinearLayout.LayoutParams(dp(70), dp(44)))
        content.addView(header)
        val search = EditText(activity).apply {
            hint = "搜索作业名称"
            textSize = 14f
            singleLine = true
            setTextColor(Color.WHITE)
            setHintTextColor(0xFFB9C4D0.toInt())
            setPadding(dp(12), 0, dp(12), 0)
            setBackgroundResource(R.drawable.bg_survey_action)
            setText(searchQuery)
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchQuery = s?.toString().orEmpty()
                    currentPage = 0
                    renderJobPage()
                }
                override fun afterTextChanged(s: android.text.Editable?) = Unit
            })
        }
        content.addView(search, LinearLayout.LayoutParams(-1, dp(42)).apply { setMargins(dp(4), dp(6), dp(4), dp(4)) })
        content.addView(label(if (busy) "正在处理作业数据，请稍候。" else "共 ${jobs.size} 个作业 · 进入后自动加载地图"))
        val list = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        jobList = list
        content.addView(ScrollView(activity).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        val pagination = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, dp(4), 0, dp(4))
        }
        val prev = pageControl("上一页") { if (currentPage > 0) { currentPage--; renderJobPage() } }
        previousPageButton = prev
        pagination.addView(prev, LinearLayout.LayoutParams(0, dp(38), 1f).apply { setMargins(dp(3), 0, dp(3), 0) })
        val page = label("", 13f).apply { gravity = android.view.Gravity.CENTER }
        pageLabel = page
        pagination.addView(page, LinearLayout.LayoutParams(dp(96), dp(38)))
        val next = pageControl("下一页") { currentPage++; renderJobPage() }
        nextPageButton = next
        pagination.addView(next, LinearLayout.LayoutParams(0, dp(38), 1f).apply { setMargins(dp(3), 0, dp(3), 0) })
        content.addView(pagination)
        val importButton = button(if (busy) "正在处理…" else "＋ 导入 SHP") {
            AlertDialog.Builder(activity).setTitle("导入 SHP")
                .setMessage("选择包含 SHP 的文件夹。每个数据集须有同名的 shp、shx、dbf、prj 文件；导入后自动创建勘界作业。")
                .setPositiveButton("选择文件夹") { _, _ ->
                    activity.startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), IMPORT_FOLDER)
                }.setNegativeButton("取消", null).show()
        }
        content.addView(importButton)
        renderJobPage()
    }

    private fun pageControl(title: String, click: () -> Unit) = TextView(activity).apply {
        text = title; textSize = 14f; gravity = android.view.Gravity.CENTER
        setTextColor(Color.WHITE); setBackgroundResource(R.drawable.bg_survey_action)
        isEnabled = !busy
        setOnClickListener { if (!busy) click() }
    }

    private fun renderJobPage() {
        val list = jobList ?: return
        list.removeAllViews()
        val filtered = jobs.sortedByDescending { it.savedAt }.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true)
        }
        val pageCount = maxOf(1, (filtered.size + PAGE_SIZE - 1) / PAGE_SIZE)
        currentPage = currentPage.coerceIn(0, pageCount - 1)
        val from = currentPage * PAGE_SIZE
        val pageJobs = filtered.drop(from).take(PAGE_SIZE)
        if (filtered.isEmpty()) {
            list.addView(label(if (jobs.isEmpty()) "还没有勘界作业\n\n点击底部“导入 SHP”开始。" else "没有找到匹配名称的作业。", 16f))
        }
        pageJobs.forEach { job ->
            val card = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL; setPadding(dp(6), dp(5), dp(6), dp(5))
                setBackgroundResource(R.drawable.bg_import_btn)
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(4), 0, dp(4)) }
            }
            card.addView(label(job.name, 16f).apply { setPadding(dp(6), dp(3), dp(6), dp(2)); maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END })
            val saved = if (job.savedAt == 0L) "尚未编辑" else "最近保存：" + SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(job.savedAt))
            card.addView(label("${job.geometry}图层 · ${job.count} 个要素　·　$saved", 12f).apply { setPadding(dp(6), 0, dp(6), dp(4)); maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END })
            val row = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL }
            val actionParams = { LinearLayout.LayoutParams(0, dp(38), 1f).apply { setMargins(dp(3), dp(2), dp(3), dp(2)) } }
            row.addView(actionItem("加载图层", R.drawable.ic_job_layers) { onOpen(job, false) }, actionParams())
            row.addView(actionItem("重命名", R.drawable.ic_job_rename) { jobAction(job, 0) }, actionParams())
            row.addView(actionItem("导出", R.drawable.ic_job_export) { jobAction(job, 1) }, actionParams())
            row.addView(actionItem("删除", R.drawable.ic_job_delete) { jobAction(job, 2) }, actionParams())
            card.addView(row); list.addView(card)
        }
        pageLabel?.text = "${currentPage + 1} / $pageCount"
        previousPageButton?.isEnabled = !busy && currentPage > 0
        nextPageButton?.isEnabled = !busy && currentPage < pageCount - 1
        previousPageButton?.alpha = if (previousPageButton?.isEnabled == true) 1f else 0.45f
        nextPageButton?.alpha = if (nextPageButton?.isEnabled == true) 1f else 0.45f
    }


    private fun actionItem(title: String, icon: Int, click: () -> Unit) = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = android.view.Gravity.CENTER
        setBackgroundResource(R.drawable.bg_survey_action)
        isEnabled = !busy
        setOnClickListener { if (!busy) click() }
        addView(ImageView(activity).apply {
            setImageResource(icon)
            imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
        }, LinearLayout.LayoutParams(dp(18), dp(18)))
        addView(TextView(activity).apply {
            text = title; textSize = 12f; setTextColor(Color.WHITE)
            maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(3) })
    }

    private fun jobAction(job: Job, index: Int) {
        when (index) {
                0 -> {
                    val input = EditText(activity).apply { setText(job.name); isSingleLine = true }
                    val dialog = AlertDialog.Builder(activity).setTitle("作业名称").setView(input)
                        .setPositiveButton("保存", null).setNegativeButton("取消", null).create()
                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val name = input.text.toString().trim()
                            if (name.isBlank()) input.error = "请填写作业名称"
                            else { job.name = name; persist(); render(); dialog.dismiss() }
                        }
                    }; dialog.show()
                }
                1 -> onOpen(job, true)
                2 -> AlertDialog.Builder(activity).setTitle("删除作业？")
                    .setMessage("将删除“${job.name}”及本机保存的编辑成果。请先导出需要保留的数据。")
                    .setPositiveButton("删除") { _, _ -> deleteJob(job) }
                    .setNegativeButton("取消", null).show()
        }
    }

    private fun deleteJob(job: Job) {
        jobs.remove(job); persist(); busy = true; render()
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 每个作业使用独立目录，只清理应用工作区内直接属于该作业的目录。
                    fun removeDirectory(directory: File, prefix: String) {
                        val resolved = directory.canonicalFile
                        check(resolved.parentFile == root.canonicalFile && resolved.name.startsWith(prefix)) { "作业目录不合法" }
                        check(!resolved.exists() || resolved.deleteRecursively()) { "无法清理本地作业文件" }
                    }
                    if (jobs.none { it.file == job.file }) File(job.file).parentFile?.let { removeDirectory(it, "import_") }
                    if (job.source.isNotBlank()) removeDirectory(File(job.source), "source_")
                }
            } catch (e: Exception) {
                if (!disposed) Toast.makeText(activity, "作业已移除，文件清理失败：${e.message}", Toast.LENGTH_LONG).show()
            } finally { busy = false; if (!disposed) render() else scope.cancel() }
        }
    }

    fun markSaved(file: File) {
        scope.launch {
            val job = jobs.firstOrNull { it.file == file.absolutePath } ?: return@launch
            job.savedAt = System.currentTimeMillis()
            try { job.count = withContext(Dispatchers.IO) { metadata(file).first } } catch (_: Exception) { }
            persist(); if (isVisible) render()
        }
    }

    private fun metadata(file: File): Pair<Long, String> {
        check(ShpLoader.initGDAL()) { "GDAL 初始化失败" }
        val ds = org.gdal.ogr.ogr.Open(file.absolutePath, 0) ?: error("无法读取数据")
        try {
            val layer = ds.GetLayer(0) ?: error("数据没有图层")
            val type = org.gdal.ogr.ogr.GeometryTypeToName(layer.GetGeomType()).lowercase()
            val name = when { type.contains("polygon") -> "面"; type.contains("line") -> "线"; type.contains("point") -> "点"; else -> "矢量" }
            return layer.GetFeatureCount().toLong() to name
        } finally { ds.delete() }
    }

    fun onActivityResult(request: Int, result: Int, data: Intent?): Boolean {
        if (request != IMPORT_FOLDER) return false
        if (result != Activity.RESULT_OK) return true
        val uri = data?.data ?: return true
        if (busy) return true
        busy = true; render()
        scope.launch {
            try {
                val imported = mutableListOf<Job>()
                val failures = mutableListOf<String>()
                withContext(Dispatchers.IO) {
                    val tree = DocumentFile.fromTreeUri(activity, uri) ?: error("无法读取文件夹")
                    fun visit(folder: DocumentFile) {
                        val children = folder.listFiles()
                        children.filter { it.isDirectory }.forEach { visit(it) }
                        children.filter { it.isFile && it.name?.endsWith(".shp", true) == true }.forEach { shp ->
                            val base = shp.name!!.substringBeforeLast('.')
                            try {
                                val companions = children.filter { it.isFile && it.name?.substringBeforeLast('.')?.equals(base, true) == true }
                                val extensions = companions.mapNotNull { it.name?.substringAfterLast('.')?.lowercase() }.toSet()
                                check(extensions.containsAll(listOf("shp", "shx", "dbf", "prj"))) { "缺少 shp/shx/dbf/prj 配套文件" }
                                val sourceDir = GpkgTestData.newDirectory(root, "source")
                                companions.forEach { doc ->
                                    val ext = doc.name!!.substringAfterLast('.').lowercase()
                                    if (ext in listOf("shp", "shx", "dbf", "prj", "cpg")) {
                                        activity.contentResolver.openInputStream(doc.uri)?.use { input ->
                                            File(sourceDir, "source.$ext").outputStream().use { input.copyTo(it) }
                                        } ?: error("无法读取 ${doc.name}")
                                    }
                                }
                                val work = GpkgTestData.importShp(File(sourceDir, "source.shp"), root)
                                val meta = metadata(work)
                                imported.add(Job(UUID.randomUUID().toString(), base, work.absolutePath, meta.first,
                                    geometry = meta.second, source = sourceDir.absolutePath))
                            } catch (e: Exception) { failures.add("$base：${e.message}") }
                        }
                    }
                    visit(tree)
                }
                jobs.addAll(imported); persist()
                if (!disposed) {
                    val detail = if (imported.isEmpty() && failures.isEmpty()) "所选文件夹没有 SHP 数据，请选择数据所在的文件夹。"
                    else "已创建 ${imported.size} 个作业。" + if (failures.isNotEmpty()) "\n\n未导入：\n${failures.joinToString("\n")}" else ""
                    AlertDialog.Builder(activity).setTitle("导入结果").setMessage(detail)
                        .setPositiveButton("确定", null).show()
                }
            } catch (e: Exception) {
                if (!disposed) AlertDialog.Builder(activity).setTitle("导入失败").setMessage(e.message).setPositiveButton("确定", null).show()
            } catch (e: LinkageError) {
                if (!disposed) AlertDialog.Builder(activity).setTitle("数据引擎加载失败").setMessage(e.message).setPositiveButton("确定", null).show()
            } finally { busy = false; if (!disposed) render() else scope.cancel() }
        }
        return true
    }

    fun dispose() { disposed = true; if (!busy) scope.cancel() }
}
