package com.jwch.gwyt_project.model

class SelectFileModel {
    var name: String = ""
    var select = false

    constructor() {}

    constructor(name: String, select: Boolean) {
        this.name = name
        this.select = select
    }

    constructor(name: String) {
        this.name = name
    }


}