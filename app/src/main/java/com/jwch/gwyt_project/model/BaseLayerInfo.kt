package com.jwch.gwyt_project.model

import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.jwch.gwyt_project.Info.EMapsInfo
import com.jwch.gwyt_project.core.Config
import com.jwch.gwyt_project.db.DbUtil
import com.jwch.gwyt_project.ext.printMsg
import java.io.File

class BaseLayerInfo {

    //矢量底图
    var vector: EMapsInfo = DbUtil.db.appDb.selector(EMapsInfo::class.java).where("BaseMapType", "=", 1).findFirst()

    //矢量注记
    var vectorNote: EMapsInfo = DbUtil.db.appDb.selector(EMapsInfo::class.java).where("BaseMapType", "=", 2).findFirst()

    //影像底图
    var image: EMapsInfo = DbUtil.db.appDb.selector(EMapsInfo::class.java).where("BaseMapType", "=", 3).findFirst()

    //影像注记
    var imageNote: EMapsInfo = DbUtil.db.appDb.selector(EMapsInfo::class.java).where("BaseMapType", "=", 4).findFirst()


    var provinceLayer = EMapsInfo()
    var sandiaoLayer = EMapsInfo()

    fun initLayer() {

        "${Config.EMAPDATA_PATH}${getTpkPath(image.tileFile)}".printMsg()
        image.layer = ArcGISTiledLayer(Config.EMAPDATA_PATH + getTpkPath(image.tileFile))
        imageNote.layer = ArcGISTiledLayer(Config.EMAPDATA_PATH + getTpkPath(imageNote.tileFile))
        vector.layer = ArcGISTiledLayer(Config.EMAPDATA_PATH + getTpkPath(vector.tileFile))
        vectorNote.layer = ArcGISTiledLayer(Config.EMAPDATA_PATH + getTpkPath(vectorNote.tileFile))
        val url = Config.EMAPDATA_PATH + "全省底图/layers"
        url.printMsg()
        provinceLayer.layer = ArcGISTiledLayer(url)
        val url2 = Config.EMAPDATA_PATH + "三调影像/layers"
        url2.printMsg()
        sandiaoLayer.layer = ArcGISTiledLayer(url2)
    }

    companion object {
        fun getTpkPath(path: String): String {
            var path = path
            if (!path.contains(".tpk")) {
                if (File(Config.EMAPDATA_PATH + path + "/layers").exists()) {
                    path = "$path/layers"
                } else if (File(Config.EMAPDATA_PATH + path + "/图层").exists()) {
                    path = "$path/图层"
                } else if (File(Config.EMAPDATA_PATH + path + "/Layers").exists()) {
                    path = "$path/Layers"
                }
            }
            return path
        }
    }


}