package com.xgimi.filemanager.ext

fun Any.tryCatch(func: () -> Unit) {
    try {
        func()
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}