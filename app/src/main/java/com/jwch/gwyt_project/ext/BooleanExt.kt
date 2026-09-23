package com.jwch.gwyt_project.ext

inline fun Boolean.yes(block: () -> Unit): Boolean {
    if (this) {
        block()
    }
    return this
}

inline fun Boolean.no(block: () -> Unit): Boolean {
    if (!this) {
        block()
    }
    return this
}

inline fun <T> Boolean.getOne(trueParams: T, falseParams: T): T = if (this) trueParams else falseParams



