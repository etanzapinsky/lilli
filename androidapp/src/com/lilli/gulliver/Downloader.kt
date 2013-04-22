package com.lilli.gulliver

import android.content.Context
import java.io.File

trait Downloader {
    fun getData(resource : String, context : Context?, options : Map<String, String?>?) : File?
}