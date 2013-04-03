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


abstract class Downloader {

    abstract fun ready() : Boolean

    abstract fun getData(resource : String) : InputStream?
}