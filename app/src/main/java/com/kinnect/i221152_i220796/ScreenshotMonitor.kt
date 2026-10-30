package com.kinnect.i221152_i220796

import android.app.Activity
import android.os.Build
/** Android 14+ reports supported user screenshots; older Android versions have no reliable public callback. */
class ScreenshotMonitor(private val activity:Activity,private val captured:()->Unit){
 private var callback:Any?=null
 fun start(){if(Build.VERSION.SDK_INT>=34&&callback==null){val c=Activity.ScreenCaptureCallback{captured()};callback=c;activity.registerScreenCaptureCallback(activity.mainExecutor,c)}}
 fun stop(){if(Build.VERSION.SDK_INT>=34){(callback as? Activity.ScreenCaptureCallback)?.let{activity.unregisterScreenCaptureCallback(it)};callback=null}}
}
