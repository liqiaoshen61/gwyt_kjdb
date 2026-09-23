package com.jwch.gwyt_project.view.popwindow

//
class PopWindowModel {

    var id = ""
    var txt = ""
    var icon : Int? = null

    var select = false

    constructor(txt: String) {
        this.txt = txt
    }

    constructor(txt: String, select : Boolean) {
        this.txt = txt
        this.select = select
    }

    constructor(id: String, txt: String) {
        this.id = id
        this.txt = txt
    }

    constructor(txt: String, icon: Int) {
        this.txt = txt
        this.icon = icon
    }

    constructor(id: String, txt: String, icon: Int) {
        this.id = id
        this.txt = txt
        this.icon = icon
    }


}