package com.jwch.gwyt_project.fragment

import android.app.AlertDialog
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.geojson.GeoJsonItem
import com.jwch.gwyt_project.adapter.geojson.GeoJsonTreeAdapter
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragLoadGeojsonBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.tip
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.GeoJsonModel
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 * 加载外部的 geojson 文件（多级树形列表，支持文件夹）
 */
class LoadGeojsonFragment : BaseFragment<FragLoadGeojsonBinding>() {

    private lateinit var adapter: GeoJsonTreeAdapter
    private var dataList: MutableList<BaseNode> = mutableListOf()
    private val selectedModels = mutableListOf<GeoJsonModel>()

    private val openFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> uri?.let { importFolder(it) } }

    override fun initView() {
        initDataList()
    }

    private fun initDataList() {
        adapter = GeoJsonTreeAdapter()
        vb.lvMain.setLinearManager()
        vb.lvMain.adapter = adapter

        adapter.onDeleteItem = { item -> showDeleteDialog(item) }

        adapter.setOnItemClickListener { _, _, position ->
            val item = adapter.data[position] as? GeoJsonItem ?: return@setOnItemClickListener
            when (item.itemType) {
                GeoJsonItem.TYPE_FOLDER -> adapter.expandOrCollapse(position)
                GeoJsonItem.TYPE_FILE -> onFileClick(item)
            }
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            val item = adapter.data[position] as? GeoJsonItem ?: return@setOnItemChildClickListener
            if (view.id == R.id.ivLayerCheck && item.itemType == GeoJsonItem.TYPE_FILE) {
                onFileClick(item)
            }
        }

        loadGeoJsonFiles()
    }

    private fun onFileClick(item: GeoJsonItem) {
        val model = item.geoJsonModel ?: return
        if (item.select) {
            item.select = false
            selectedModels.remove(model)
        } else {
            item.select = true
            selectedModels.add(model)
        }
        adapter.notifyDataSetChanged()

        if (selectedModels.isEmpty()) {
            EventBus.getDefault().post(DataEvent(DataEvent.DRAW_SELECT_GEOJSON_LIST, ArrayList<GeoJsonModel>()))
        } else {
            EventBus.getDefault().post(DataEvent(DataEvent.DRAW_SELECT_GEOJSON_LIST, ArrayList(selectedModels)))
        }
    }

    private fun loadGeoJsonFiles() {
        GlobalScope.launch(Dispatchers.IO) {
            val root = File(Config.GEO_JSON)
            val nodes = mutableListOf<BaseNode>()
            scanAndBuildTree(root, nodes, 0)

            withContext(Dispatchers.Main) {
                dataList = nodes
                adapter.setNewData(dataList)
                vb.tvNodata.visiable(!CommonUtil.matchList(dataList))
            }
        }
    }

    private fun scanAndBuildTree(dir: File, parentList: MutableList<BaseNode>, level: Int) {
        if (!dir.exists() || !dir.isDirectory) return

        val fs = dir.listFiles() ?: return
        val sorted = fs.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

        sorted.forEach { file ->
            when {
                file.isDirectory -> {
                    val childList = mutableListOf<BaseNode>()
                    scanAndBuildTree(file, childList, level + 1)
                    if (childList.isNotEmpty()) {
                        val folder = GeoJsonItem(GeoJsonItem.TYPE_FOLDER)
                        folder.name = file.name
                        folder.filePath = file.absolutePath
                        folder.levelIndex = level
                        folder.childNode = childList
                        folder.isExpanded = level < 1
                        parentList.add(folder)
                    }
                }

                (file.isFile && file.name.endsWith(".json", ignoreCase = true))
                        || (file.isFile && file.name.endsWith(".geojson", ignoreCase = true)) -> {
                    val model = GeoJsonModel(file)
                    val node = GeoJsonItem(GeoJsonItem.TYPE_FILE)
                    node.name = model.name
                    node.filePath = model.filePath
                    node.levelIndex = level
                    node.geoJsonModel = model
                    parentList.add(node)
                }
            }
        }
    }

    // ==================== 导入逻辑 ====================

    private fun importFolder(uri: Uri) {
        GlobalScope.launch(Dispatchers.IO) {
            val targetDir = File(Config.GEO_JSON)
            if (!targetDir.exists()) targetDir.mkdirs()

            val docFile = DocumentFile.fromTreeUri(requireActivity(), uri)
            var successCount = 0
            var skipCount = 0
            if (docFile != null) {
                val result = copyDocumentFolder(docFile, targetDir)
                successCount = result.first
                skipCount = result.second
            }
            withContext(Dispatchers.Main) {
                showImportResult(successCount, skipCount)
            }
        }
    }

    /**
     * 递归复制 DocumentFile 目录到目标 File 目录，返回 (成功数, 跳过数)
     */
    private fun copyDocumentFolder(source: DocumentFile, targetDir: File): Pair<Int, Int> {
        var success = 0
        var skip = 0
        if (!targetDir.exists()) targetDir.mkdirs()

        source.listFiles().forEach { child ->
            if (child.isDirectory) {
                val subDir = File(targetDir, child.name ?: "unknown")
                val (s, k) = copyDocumentFolder(child, subDir)
                success += s; skip += k
            } else if (child.isFile) {
                val name = child.name ?: return@forEach
                if (name.endsWith(".json", ignoreCase = true) || name.endsWith(".geojson", ignoreCase = true)) {
                    try {
                        if (safeCopyTo(child.uri, targetDir, name)) success++ else skip++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return Pair(success, skip)
    }

    /**
     * 安全复制：先写临时文件再 rename，避免截断同名文件
     * @return true=成功, false=跳过(目标已存在)
     */
    private fun safeCopyTo(uri: Uri, targetDir: File, fileName: String): Boolean {
        val targetFile = File(targetDir, fileName)
        if (targetFile.exists()) return false

        val tmpFile = File.createTempFile("import_", ".tmp", targetDir)
        try {
            requireActivity().contentResolver.openInputStream(uri)?.use { input ->
                tmpFile.outputStream().use { out -> input.copyTo(out) }
            } ?: return false

            return if (targetFile.exists()) {
                // rename 期间被其他线程先创建了
                false
            } else if (tmpFile.renameTo(targetFile)) {
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            tmpFile.delete()
        }
    }

    private fun showImportResult(success: Int, skip: Int) {
        val msg = if (skip > 0) {
            "成功导入 $success 个文件，跳过 $skip 个同名文件"
        } else {
            "成功导入 $success 个文件"
        }
        tip(msg)
        refreshList()
    }

    // ==================== 删除逻辑 ====================

    private fun showDeleteDialog(item: GeoJsonItem) {
        val title = if (item.itemType == GeoJsonItem.TYPE_FOLDER) "文件夹" else "文件"
        AlertDialog.Builder(requireContext())
            .setTitle("确认删除")
            .setMessage("确定删除${title}「${item.name}」吗？\n此操作不可恢复。")
            .setPositiveButton("删除") { _, _ -> deleteItem(item) }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteItem(item: GeoJsonItem) {
        GlobalScope.launch(Dispatchers.IO) {
            val file = File(item.filePath)
            var success = false
            try {
                success = if (file.isDirectory) file.deleteRecursively() else file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                if (success) {
                    // 如果删除的文件在选中列表中，同步移除
                    item.geoJsonModel?.let { selectedModels.remove(it) }
                    if (selectedModels.isEmpty()) {
                        EventBus.getDefault().post(DataEvent(DataEvent.DRAW_SELECT_GEOJSON_LIST, ArrayList<GeoJsonModel>()))
                    }
                    tip("已删除")
                    refreshList()
                } else {
                    tip("删除失败")
                }
            }
        }
    }

    private fun refreshList() {
        selectedModels.clear()
        adapter.setNewData(null)
        loadGeoJsonFiles()
    }

    override fun initViewListener() {
        vb.ivClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_GEOJSON_FRAG))
        }

        vb.tvImportFolder.onClick {
            openFolderLauncher.launch(null)
        }
    }

    fun onEmbedded() {
        vb.llTitleBar.gone()
        vb.vDivider.gone()
        vb.root.setBackgroundResource(0)
        vb.root.setPadding(0, 0, 0, 0)
    }
}