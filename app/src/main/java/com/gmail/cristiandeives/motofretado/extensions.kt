package com.gmail.cristiandeives.motofretado

import android.content.Context
import android.content.SharedPreferences
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast

private const val TAG = "ExtensionFunctions"

internal inline fun Context.editSharedPreferences(doEdit: SharedPreferences.Editor.() -> Unit) {
    val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
    editor.doEdit()
    editor.apply()
}

internal fun Context.toast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

internal fun Messenger.sendMessage(what: Int) {
    val msg = Message.obtain(null, what)
    try {
        send(msg)
    } catch (e: RemoteException) {
        Log.e(TAG, "error sending message", e)
    }
}