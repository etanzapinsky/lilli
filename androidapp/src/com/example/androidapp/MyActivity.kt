/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 3/19/13
 * Time: 1:49 PM
 * To change this template use File | Settings | File Templates.
 */

package com.example.androidapp

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.widget.EditText
import com.example.androidapp.R
import com.example.androidapp.RequestUrlActivity

class MyActivity() : Activity() {
    public final val EXTRA_MESSAGE : String = "com.example.androidapp.MESSAGE"
    protected override fun onCreate(savedInstanceState : Bundle?) {
        super<Activity>.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }

    public fun requestURL(view : View) {
        val intent = Intent(this, javaClass<RequestUrlActivity>())
        val editText = findViewById(R.id.url_string) as EditText
        val message = editText.getText().toString()
        intent.putExtra(EXTRA_MESSAGE, message)
        startActivity(intent)
    }
}