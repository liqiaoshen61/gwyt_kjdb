package com.jwch.gwyt_project.fragment

import android.app.AlertDialog
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jameni.allutillib.common.CommonUtil
import com.jwch.gwyt_project.R
import com.jwch.gwyt_project.adapter.shp.ShpItem
import com.jwch.gwyt_project.adapter.shp.ShpTreeAdapter
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.databinding.FragLoadShpBinding
import com.jwch.gwyt_project.ext.gone
import com.jwch.gwyt_project.ext.show
import com.jwch.gwyt_project.ext.tip
import com.jwch.gwyt_project.ext.visiable
import com.jwch.gwyt_project.fragment.base.BaseFragment
import com.jwch.gwyt_project.model.DataEvent
import com.jwch.gwyt_project.model.ShpModel
import com.qmuiteam.qmui.kotlin.onClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 * 加载外部 .shp 文件（多级树形列表，支持文件夹）
 */
class LoadShpFragment : BaseFragment<FragLoadShpBinding>() {

    private lateinit var adapter: ShpTreeAdapter
    private var dataList: MutableList<BaseNode> = mutableListOf()
    private val selectedModels = mutableListOf<ShpModel>()

    private val openFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> uri?.let { importFolder(it) } }

    companion object {
        // SHP 文件的关联文件扩展名
        private val SHP_COMPANION_EXTENSIONS = setOf("shx", "dbf", "prj", "cpg", "qix", "sbn", "sbx", "xml")
    }

    override fun initView() {
        initDataList()
    }

    private fun initDataList() {
        adapter = ShpTreeAdapter()
        vb.lvMain.setLinearManager()
        vb.lvMain.adapter = adapter

        adapter.onDeleteItem = { item -> showDeleteDialog(item) }

        adapter.setOnItemClickListener { _, _, position ->
            val item = adapter.data[position] as? ShpItem ?: return@setOnItemClickListener
            when (item.itemType) {
                ShpItem.TYPE_FOLDER -> adapter.expandOrCollapse(position)
                ShpItem.TYPE_FILE -> onFileClick(item)
            }
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            val item = adapter.data[position] as? ShpItem ?: return@setOnItemChildClickListener
            if (view.id == R.id.ivLayerCheck && item.itemType == ShpItem.TYPE_FILE) {
                onFileClick(item)
            }
        }

        loadShpFiles()
    }

    private fun onFileClick(item: ShpItem) {
        val model = item.shpModel ?: return
        if (item.select) {
            item.select = false
            selectedModels.remove(model)
        } else {
            item.select = true
            selectedModels.add(model)
        }
        adapter.notifyDataSetChanged()

        if (selectedModels.isEmpty()) {
            EventBus.getDefault().post(DataEvent(DataEvent.DRAW_SELECT_SHP_LIST, ArrayList<ShpModel>()))
        } else {
            EventBus.getDefault().post(DataEvent(DataEvent.DRAW_SELECT_SHP_LIST, ArrayList(selectedModels)))
        }
    }

    private fun loadShpFiles() {
        GlobalScope.launch(Dispatchers.IO) {
            val root = File(Config.SHP_IMPORT_PATH)
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
                        if (canCollapseFolder(file, childList)) {
                            val promoted = childList.first() as ShpItem
                            promoted.levelIndex = level
                            parentList.add(promoted)
                        } else {
                            val folder = ShpItem(ShpItem.TYPE_FOLDER)
                            folder.name = file.name
                            folder.filePath = file.absolutePath
                            folder.levelIndex = level
                            folder.childNode = childList
                            folder.isExpanded = level < 1
                            parentList.add(folder)
                        }
                    }
                }

                file.isFile && file.name.endsWith(".shp", ignoreCase = true) -> {
                    val model = ShpModel(file)
                    val node = ShpItem(ShpItem.TYPE_FILE)
                    node.name = model.name
                    node.filePath = model.filePath
                    node.levelIndex = level
                    node.shpModel = model
                    parentList.add(node)
                }
            }
        }
    }

    private fun canCollapseFolder(dir: File, children: MutableList<BaseNode>): Boolean {
        if (children.size != 1) return false
        val item = children.first() as? ShpItem ?: return false
        if (item.itemType != ShpItem.TYPE_FILE) return false
        val shpName = item.name.removeSuffix(".shp").removeSuffix(".SHP")
        return shpName.equals(dir.name, ignoreCase = true)
    }

    // ==================== 导入逻辑 ====================

    /**
     * 导入整个文件夹
     */
    private fun importFolder(uri: Uri) {
        GlobalScope.launch(Dispatchers.IO) {
            val targetDir = File(Config.SHP_IMPORT_PATH)
            if (!targetDir.exists()) targetDir.mkdirs()

            val docFile = DocumentFile.fromTreeUri(requireActivity(), uri)
            var successCount = 0
            var skipCount = 0
            if (docFile != null) {
                val result = copyShpDocumentFolder(docFile, targetDir)
                successCount = result.first
                skipCount = result.second
            }
            withContext(Dispatchers.Main) {
                showImportResult(successCount, skipCount)
            }
        }
    }

    /**
     * 递归复制文件夹中的SHP相关文件，返回 (成功数, 跳过数)
     */
    private fun copyShpDocumentFolder(source: DocumentFile, targetDir: File): Pair<Int, Int> {
        var success = 0
        var skip = 0
        if (!targetDir.exists()) targetDir.mkdirs()

        source.listFiles().forEach { child ->
            if (child.isDirectory) {
                val subDir = File(targetDir, child.name ?: "unknown")
                val (s, k) = copyShpDocumentFolder(child, subDir)
                success += s; skip += k
            } else if (child.isFile) {
                val name = child.name ?: return@forEach
                val ext = name.substringAfterLast('.', "").lowercase()
                // 复制所有SHP相关文件（.shp, .shx, .dbf, .prj 等）
                if (ext == "shp" || ext in SHP_COMPANION_EXTENSIONS) {
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

    private fun showDeleteDialog(item: ShpItem) {
        val title = if (item.itemType == ShpItem.TYPE_FOLDER) "文件夹" else "文件"
        AlertDialog.Builder(requireContext())
            .setTitle("确认删除")
            .setMessage("确定删除${title}「${item.name}」吗？\n此操作不可恢复。")
            .setPositiveButton("删除") { _, _ -> deleteItem(item) }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteItem(item: ShpItem) {
        GlobalScope.launch(Dispatchers.IO) {
            var success = false
            try {
                if (item.itemType == ShpItem.TYPE_FILE) {
                    // 删除单个文件时，同时删除关联文件
                    val shpFile = File(item.filePath)
                    val baseName = shpFile.name.removeSuffix(".shp").removeSuffix(".SHP")
                    val parentDir = shpFile.parentFile
                    // 先删除 .shp 文件
                    success = shpFile.delete()
                    // 删除关联文件
                    if (parentDir != null && success) {
                        SHP_COMPANION_EXTENSIONS.forEach { ext ->
                            val companion = File(parentDir, "$baseName.$ext")
                            if (companion.exists()) companion.delete()
                        }
                    }
                } else {
                    success = File(item.filePath).deleteRecursively()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                if (success) {
                    item.shpModel?.let { selectedModels.remove(it) }
                    if (selectedModels.isEmpty()) {
                        EventBus.getDefault().post(DataEvent(DataEvent.DRAW_SELECT_SHP_LIST, ArrayList<ShpModel>()))
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
        loadShpFiles()
    }

    override fun initViewListener() {
        vb.ivClose.onClick {
            EventBus.getDefault().post(DataEvent(DataEvent.CLOSE_SHP_FRAG))
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