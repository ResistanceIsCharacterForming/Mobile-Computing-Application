package com.example.shelfship.utils

import android.app.Activity
import android.content.Context

fun Context.getCurrentActivityName(): String {
    return (this as? Activity)?.javaClass?.simpleName ?: ""
}
