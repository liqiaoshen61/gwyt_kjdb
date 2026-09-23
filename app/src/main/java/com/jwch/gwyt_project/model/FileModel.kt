package com.jwch.gwyt_project.model

import com.jwch.gwyt_project.ext.printMsg
import java.io.File

class FileModel {

    var name = ""//文件名
    var ext = ""//文件后缀
    var fileSize = ""//文件大小
    var fileSizeLong = 0L//文件大小
    var filePath = ""//文件路径
    var select = false


    constructor() {
    }

    constructor(filePath: String, name: String) {
        this.filePath = filePath
        this.name = name
    }

    constructor(file: File) {

        this.name = file.name
        this.ext = getFileExt()
        this.fileSizeLong = file.length()
        this.fileSize = file.length().toString()
        this.filePath = file.absolutePath
    }

    fun getFileExt():String{


        val lastIndexOf = this.name.lastIndexOf(".")
        val suffix = this.name.substring(lastIndexOf)
        "后缀名:$suffix".printMsg()

        return suffix



    }
}