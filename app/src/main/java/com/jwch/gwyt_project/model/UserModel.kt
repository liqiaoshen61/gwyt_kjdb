package com.jwch.gwyt_project.model

class UserModel {
    var acc: String? = null
    var pw: String? = null

    constructor(acc: String?, pw: String?) {
        this.acc = acc
        this.pw = pw
    }
}