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
import android.widget.EditText
import android.os.AsyncTask
import android.widget.ImageView
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class MyActivity() : Activity() {
    class object {
        public val EXTRA_MESSAGE: String = "com.example.androidapp.MESSAGE"
    }

    var down : Downloader? = null
    var imageView : ImageView? = null

    protected override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val context = getApplicationContext()
        if (context != null)
            down = OriginDownloader(context)
        imageView = findViewById(R.id.received_image) as? ImageView
    }

    public fun requestURL() {
        val editText = findViewById(R.id.url_string) as? EditText
        // This message (the thing in the text box) has to have http:// since the URL object doesn't do that for us
        // it will fail without it.
        val message = editText?.getText().toString()

        if (down?.ready() == true) {
            AsyncDownloader().execute(message)
        }
        else {
            // wruh-wroh
        }
    }

    inner class AsyncDownloader : AsyncTask<String, Int, Bitmap>() {
        /**
         * For now we are going to assume the downloader is only passed one url at a time.
         * It wouldn't be hard to extend it to take multiple urls at once, check out the example here:
         * http://developer.android.com/reference/android/os/AsyncTask.html
         */
        protected override fun doInBackground(vararg p0 : String?) : Bitmap? {
            val first = p0.get(0)
            if (first != null)
                return BitmapFactory.decodeStream(down?.getData(first))
            return null
        }

        protected override fun onPostExecute(result : Bitmap?) {
            imageView?.setImageBitmap(result)
        }

    }

}