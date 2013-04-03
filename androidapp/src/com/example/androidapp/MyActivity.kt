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
import java.net.URL
import java.io.InputStream
import android.os.AsyncTask
import android.widget.ImageView
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class MyActivity() : Activity() {
    public final val EXTRA_MESSAGE : String = "com.example.androidapp.MESSAGE"
    private var down : OriginDownloader? = null
    private var imageView : ImageView? = null

    protected override fun onCreate(savedInstanceState : Bundle?) {
        super<Activity>.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        down = OriginDownloader(getApplicationContext())
        imageView = findViewById(R.id.received_image) as ImageView
    }

    public fun requestURL(view : View) {
        val intent = Intent(this, javaClass<RequestUrlActivity>())
        val editText = findViewById(R.id.url_string) as EditText
        // This message (the thing in the text box) has to have http:// since the URL object doesn't do that for us
        // it will fail without it.
        val message = editText.getText().toString()

        if (down!!.ready()) {
            AsyncDownloader<String, Integer, Long>().execute(message)
        }
        else {
            // wruh-wroh
        }
    }

    inner class AsyncDownloader<String, Integer, InputStream> : AsyncTask<String, Integer, InputStream>() {
        /**
         * For now we are going to assume the downloader is only passed one url at a time.
         * It wouldn't be hard to extend it to take multiple urls at once, check out the example here:
         * http://developer.android.com/reference/android/os/AsyncTask.html
         */
        protected override fun doInBackground(vararg resources : String?) : InputStream {
            return down!!.getData(resources.get(0)) as InputStream
        }

        protected override fun onPostExecute(result : InputStream?) {
            val bitmap : Bitmap? = BitmapFactory.decodeStream(result as java.io.InputStream)
            imageView!!.setImageBitmap(bitmap)
        }

    }

}