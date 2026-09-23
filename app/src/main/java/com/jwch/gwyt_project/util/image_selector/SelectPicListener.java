package com.jwch.gwyt_project.util.image_selector;

import com.luck.picture.lib.entity.LocalMedia;

import java.util.List;

public interface SelectPicListener {
    void onSelectPicSuccess(List<LocalMedia> resultList, int flag);
}
