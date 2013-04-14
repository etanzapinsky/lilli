/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/3/13
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */


package com.lilli.gulliver

import android.content.Context
import java.io.InputStream

trait Downloader {

    fun getData(resource : String, context : Context?) : InputStream?
}