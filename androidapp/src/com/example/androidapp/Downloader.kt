/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 4/3/13
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */


package com.example.androidapp

import java.io.InputStream

trait Downloader {

    fun ready() : Boolean

    fun getData(resource : String) : InputStream?

    fun getDownloadTime() : Int
}