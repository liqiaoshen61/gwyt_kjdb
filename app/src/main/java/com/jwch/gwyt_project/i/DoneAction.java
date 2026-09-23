package com.jwch.gwyt_project.i;

public class DoneAction implements Runnable {

    private DoneListener doneListener;
    private int tag;
    private Object data;

    public DoneAction(int tag) {
        this.tag = tag;
    }

    public DoneAction(DoneListener doneListener) {
        this.doneListener = doneListener;
    }

    public DoneAction(DoneListener doneListener, int tag) {
        this.doneListener = doneListener;
        this.tag = tag;
    }


    public DoneAction(DoneListener doneListener, Object data) {
        this.doneListener = doneListener;
        this.data = data;
    }

    public DoneAction(DoneListener doneListener, Object data, int tag) {
        this.doneListener = doneListener;
        this.tag = tag;
        this.data = data;
    }

    @Override
    public void run() {
        if (doneListener != null) doneListener.onDone(data, tag);
    }
}
