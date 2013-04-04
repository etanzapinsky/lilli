package com.example.androidapp

import android.app.Activity
import android.os.Bundle
import android.os.Build
import android.view.MenuItem
import android.content.Intent
import android.widget.TextView
import android.support.v4.app.NavUtils


/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 3/20/13
 * Time: 4:00 PM
 * To change this template use File | Settings | File Templates.
 */
class RequestUrlActivity() : Activity() {
    protected override fun onCreate(savedInstanceState : Bundle?) {
        super<Activity>.onCreate(savedInstanceState)

        val intent = getIntent()
        val message = intent?.getStringExtra(MyActivity().EXTRA_MESSAGE)
        setContentView(R.layout.display_message)
        val textView = findViewById(R.id.display_message_result) as TextView
        textView.setText(message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar()?.setDisplayHomeAsUpEnabled(true)
        }
    }

    protected fun onOptionsItemSelected(item : MenuItem) : Boolean {
        if(item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}