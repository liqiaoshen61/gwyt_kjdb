package com.jwch.gwyt_project.Info;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;


@Table(name = "PasswordInfo")
public class PasswordInfo {

    @Column(name = "Id", isId = true, autoGen = true, property = "NOT NULL")
    private int id;
    @Column(name = "passWord")
    private String passWord;
    @Column(name = "is_show")
    private int is_show;

    public static int PASSWORD_HIDE = 0;
    public static int PASSWORD_SHOW = 1;

    public int getIs_show() {
        return is_show;
    }

    public boolean getIsShow() {
        return is_show == PASSWORD_SHOW;
    }

    public void setIs_show(int is_show) {
        this.is_show = is_show;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getPassWord() {
        return passWord;
    }


    public PasswordInfo(String passWord, int is_show) {
        this.passWord = passWord;
        this.is_show = is_show;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public PasswordInfo() {
    }
}
