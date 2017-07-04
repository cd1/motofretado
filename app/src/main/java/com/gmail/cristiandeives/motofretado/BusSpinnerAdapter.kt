package com.gmail.cristiandeives.motofretado

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.MainThread
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.gmail.cristiandeives.motofretado.http.Bus

@MainThread
internal class BusSpinnerAdapter(context: Context) : ArrayAdapter<Bus>(context, ITEM_RESOURCE) {
    companion object {
        private val TAG = BusSpinnerAdapter::class.java.simpleName
        @LayoutRes
        private val ITEM_RESOURCE = android.R.layout.simple_spinner_item
        @LayoutRes
        private val DROPDOWN_ITEM_RESOURCE = android.R.layout.simple_spinner_dropdown_item
    }

    private val mInflater: LayoutInflater
    private var mErrorMessage: String? = null

    init {
        setDropDownViewResource(DROPDOWN_ITEM_RESOURCE)
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        Log.v(TAG, "> getCount()")

        val count = maxOf(super.getCount(), 1)

        Log.v(TAG, "< getCount(): $count")
        return count
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        Log.v(TAG, "> getView(position=$position, convertView=$convertView, parent=$parent)")

        val view = if (!hasActualBusData()) {
            val myConvertView = convertView ?: mInflater.inflate(ITEM_RESOURCE, parent, false)

            val textView = myConvertView as TextView
            textView.text = if (!mErrorMessage.isNullOrEmpty()) {
                textView.error = ""
                mErrorMessage
            } else {
                context.getString(R.string.loading_bus_numbers)
            }

            textView
        } else {
            super.getView(position, convertView, parent)
        }

        Log.v(TAG, "> getView(position=$position, convertView=$convertView, parent=$parent): $view")
        return view
    }

    internal fun setErrorMessage(message: String) {
        mErrorMessage = message
        clear()
    }

    internal fun hasActualBusData() = super.getCount() > 0
}