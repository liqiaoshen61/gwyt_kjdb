package com.jwch.gwyt_project.model

/**
 * Created by Jameni on 2018/1/15.
 * 图片
 */
class ImageModel {

    var imgUrl: String? = null//图片地址
    var imgId: String? = null  //图片id
    var imgType = 0 //图片类型，0 网络图片，1本地图片 2本地资源id
    var imgResId = 0 //图片资源id
    var des = ""
    var description : String? = null//

    var isSelect = false

    var url : String? = null//

    var name : String? = null
    var canDelete : Boolean = true

    constructor() {}

    constructor(imgUrl: String?) {
        this.imgUrl = imgUrl
    }

    constructor(imgUrl: String?, name: String?) {
        this.imgUrl = imgUrl
        this.name = name
    }

    constructor(imgUrl: String?, des: String, name: String?) {
        this.imgUrl = imgUrl
        this.des = des
        this.name = name
    }

    constructor(imgUrl: String?, des: String, name: String?,canDelete : Boolean) {
        this.imgUrl = imgUrl
        this.des = des
        this.name = name
        this.canDelete = canDelete
    }

}