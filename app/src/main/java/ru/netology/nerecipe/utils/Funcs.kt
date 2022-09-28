package ru.netology.nerecipe.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.io.ByteArrayOutputStream

fun toText(value: Int): String {
    val i: Int
    val d: Int
    val units: String
    if (value >= 1_000_000) {
        i = value / 1_000_000
        d = (value % 1_000_000) / 100_000
        units = "M"
    } else if (value >= 1_000) {
        i = value / 1_000
        d = if (value >= 10_000) 0 else (value % 1_000) / 100
        units = "K"
    } else {
        i = value
        d = 0
        units = ""
    }
    return if (d != 0) "$i.$d$units" else "$i$units"
}

fun hideKeyboard(view: View) {
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken,0)
}

fun showKeyboard(view: View) {
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view,0)
    //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
}

fun bitmap2String (bitmap: Bitmap?): String? {
    if (bitmap != null) {
        val os = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        val ba = os.toByteArray()
        return Base64.encodeToString(ba, 0)
    } else {
        return null
    }
}
