package com.gmail.cristiandeives.motofretado

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.annotation.UiThread
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import kotlinx.android.synthetic.main.dialog_add_bus.*

@MainThread
internal class AddBusDialogFragment : DialogFragment(), TextWatcher {
    companion object {
        private val TAG = AddBusDialogFragment::class.java.simpleName
    }

    private lateinit var mOnClickListener: OnClickListener
    private lateinit var mButtonOK: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onCreate(savedInstanceState=$savedInstanceState)")
        super.onCreate(savedInstanceState)

        val fragment = targetFragment

        if (fragment is OnClickListener) {
            mOnClickListener = fragment
        } else {
            Log.wtf(TAG, "parent fragment cannot handle clicks; dialog buttons won't work")
        }

        Log.v(TAG, "< onCreate(savedInstanceState=$savedInstanceState)")
    }

    override fun onStart() {
        Log.v(TAG, "> onStart()")
        super.onStart()

        mButtonOK = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
        mButtonOK.isEnabled = dialog.editBusId.text.isNotBlank()

        dialog.editBusId.addTextChangedListener(this)

        Log.v(TAG, "< onStart()")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.v(TAG, "> onCreateDialog(savedInstanceState=$savedInstanceState)")
        super.onCreateDialog(savedInstanceState)

        val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.add_bus_dialog_title)
                .setView(R.layout.dialog_add_bus)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val busId = dialog.editBusId.text.trim().toString()
                    mOnClickListener.onPositiveButtonClick(busId)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()

        Log.v(TAG, "< onCreateDialog(savedInstanceState=$savedInstanceState): $dialog")
        return dialog
    }

    override fun afterTextChanged(s: Editable) {
        Log.v(TAG, "> afterTextChanged(s=$s)")

        mButtonOK.isEnabled = s.isNotBlank()

        Log.v(TAG, "< afterTextChanged(s=$s)")
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        Log.v(TAG, "beforeTextChanged(s=$s, start=$start, count=$count, after=$after)")
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        Log.v(TAG, "onTextChanged(s=$s, start=$start, before=$before, count=$count)")
    }

    @UiThread
    internal interface OnClickListener {
        fun onPositiveButtonClick(text: String)
    }
}