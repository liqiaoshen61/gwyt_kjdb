package com.jwch.gwyt_project.Info;

public class TianDiMapInfo {
    private EMapsInfo image;//影像底图
    private EMapsInfo imageNote;//影像注记
    private EMapsInfo vector;//矢量底图
    private EMapsInfo vectorNote;//矢量注记

    public EMapsInfo getImageNote() {
        return imageNote;
    }

    public void setImageNote(EMapsInfo imageNote) {
        this.imageNote = imageNote;
    }

    public EMapsInfo getVectorNote() {
        return vectorNote;
    }

    public void setVectorNote(EMapsInfo vectorNote) {
        this.vectorNote = vectorNote;
    }

    public EMapsInfo getImage() {
        return image;
    }

    public void setImage(EMapsInfo image) {

//        PrintUtil.printMsg("EMapsInfo===" + image.toString());
        this.image = image;
    }

    public EMapsInfo getVector() {
        return vector;
    }

    public void setVector(EMapsInfo vector) {
        this.vector = vector;
    }


    @Override
    public String toString() {
        return "TianDiMapInfo{" +
                "image=" + image +
                ", imageNote=" + imageNote +
                ", vector=" + vector +
                ", vectorNote=" + vectorNote +
                '}';
    }
}
