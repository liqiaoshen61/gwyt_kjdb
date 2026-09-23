package com.jwch.gwyt_project.model



data class ProcessRecord(
    val processor: String,      // 处理人
    val processTime: String,    // 处理时间
    val content: String? = null, // 处理内容（类型1特有）
    val status: String? = null   // 处理状态（类型2特有）
) {
    override fun toString(): String {
        return "处理人:$processor 处理时间:$processTime" +
                (content?.let { " 处理内容:$it" } ?: "") +
                (status?.let { " 处理状态:$it" } ?: "")
    }
}