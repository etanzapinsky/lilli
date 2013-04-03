/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/3/13
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */


package com.example.androidapp

import android.os.AsyncTask
import java.io.InputStream


abstract class Downloader<String, Integer, Long> : AsyncTask<String, Integer, Long>() {

    abstract fun ready() : Boolean

    abstract protected fun getData(resource : String) : InputStream?
}