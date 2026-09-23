package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 水印密码信息
 * 与软件锁屏密码（PasswordInfo）独立，互不影响
 */
@Table(name = "WatermarkPasswordInfo")
public class WatermarkPasswordInfo {

    @Column(name = "Id", isId = true, autoGen = true, property = "NOT NULL")
    private int id;
    @Column(name = "passWord")
    private String passWord;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public WatermarkPasswordInfo(String passWord) {
        this.passWord = passWord;
    }

    public WatermarkPasswordInfo() {
    }
}
