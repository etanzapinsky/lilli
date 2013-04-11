/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 3/19/13
 * Time: 1:49 PM
 * To change this template use File | Settings | File Templates.
 */

package com.lilli.gulliver

import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import android.os.AsyncTask
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import android.graphics.BitmapFactory
import java.io.InputStream

class MyActivity() : Activity() {
    class object {
        public val EXTRA_MESSAGE: String = "com.lilli.gulliver.MESSAGE"
    }

    var down : Downloader? = null
    var imageView : ImageView? = null
    var responseMessage : TextView? = null

    protected override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val context = getApplicationContext()
        if (context != null)
            down = OriginDownloader(context)
        imageView = findViewById(R.id.received_image) as? ImageView
        responseMessage = findViewById(R.id.response_text) as? TextView
    }

    public fun requestURL(view : View) {
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

    inner class AsyncDownloader : AsyncTask<String, Int, InputStream?>() {
        var startTime : Long = 0

        protected override fun onPreExecute() {
            super.onPreExecute()
            startTime = System.nanoTime()
        }

        /**
         * For now we are going to assume the downloader is only passed one url at a time.
         * It wouldn't be hard to extend it to take multiple urls at once, check out the lilli here:
         * http://developer.android.com/reference/android/os/AsyncTask.html
         */
        protected override fun doInBackground(vararg p0 : String?) : InputStream? {
            val first = p0.get(0)

            if (first != null) {
                return down?.getData(first, getApplicationContext())
            }

            return null
        }

        protected override fun onPostExecute(result : InputStream?) {
            if (result != null) {
                val elapsed = System.nanoTime() - startTime
                val bitmap = BitmapFactory.decodeStream(result)
                imageView?.setImageBitmap(bitmap)
//                responseMessage?.setText("Path: " + result.getAbsolutePath() + "\n" + (elapsed / 1000000) + " millisecs")
                // Since we know it's an image we can do this:
                // NOTE: large images run out of memory to display --> dont do it.
//                imageView?.setImageBitmap(BitmapFactory.decodeFile(result.getAbsolutePath()))
            }
            else {
                responseMessage?.setText("Please enter a valid URL")
            }
        }

    }

}