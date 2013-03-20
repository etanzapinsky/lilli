/**
 * Created with IntelliJ IDEA.
 * User: Etan
 * Date: 3/19/13
 * Time: 1:49 PM
 * To change this template use File | Settings | File Templates.
 */

package com.example

import android.app.Activity
import android.os.Bundle
import com.example.androidapp.R

class HelloKotlin() : Activity() {
    protected override fun onCreate(savedInstanceState : Bundle?) {
        super<Activity>.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }
}