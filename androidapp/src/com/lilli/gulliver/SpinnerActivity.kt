package com.lilli.gulliver

import android.app.Activity
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Adapter
import android.widget.AdapterView
import android.view.View

class SpinnerActivity : Activity(), OnItemSelectedListener {
    var currentlySelected : String? = null

    public override fun onNothingSelected(parent: AdapterView<out Adapter?>?) {
        return
    }

    public override fun onItemSelected(parent: AdapterView<out Adapter?>?, view: View?, position: Int, id: Long) {
        currentlySelected = parent?.getItemAtPosition(position).toString()
    }
}