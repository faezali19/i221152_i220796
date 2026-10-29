package com.kinnect.i221152_i220796

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

class KinnectApp : Application(), Application.ActivityLifecycleCallbacks {
 private var started = 0
 private var connected = false
 private var generation = 0

 override fun onCreate() {
  super.onCreate()
  registerActivityLifecycleCallbacks(this)
  Backend.db.getReference(".info/serverTimeOffset").addValueEventListener(object : ValueEventListener {
   override fun onDataChange(snapshot: DataSnapshot) {
    Backend.offset = (snapshot.value as? Number)?.toLong() ?: 0L
   }
   override fun onCancelled(error: DatabaseError) {}
  })
  Backend.auth.addAuthStateListener { auth ->
   generation++
   val uid = auth.currentUser?.uid.orEmpty()
   if (uid.isNotEmpty()) {
    syncPresence()
    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
     if (Backend.uid == uid) Backend.db.getReference("tokens/$uid/$token").setValue(true)
    }
   }
  }
  Backend.db.getReference(".info/connected").addValueEventListener(object : ValueEventListener {
   override fun onDataChange(snapshot: DataSnapshot) {
    connected = snapshot.value == true
    generation++
    if (connected) syncPresence()
   }
   override fun onCancelled(error: DatabaseError) {}
  })
  if (Build.VERSION.SDK_INT >= 26) {
   getSystemService(NotificationManager::class.java).createNotificationChannel(
    NotificationChannel("kinnect_messages", "Kinnect messages and calls", NotificationManager.IMPORTANCE_HIGH)
   )
  }
 }

 private fun syncPresence() {
  val uid = Backend.uid
  val attempt = ++generation
  if (uid.isEmpty() || !connected) return
  val reference = Backend.db.getReference("presence/$uid")
  // Install the server-side disconnect action before publishing online.
  reference.onDisconnect().setValue(mapOf("online" to false, "lastSeen" to ServerValue.TIMESTAMP))
   .addOnSuccessListener {
    if (generation == attempt && Backend.uid == uid && connected) {
     reference.setValue(mapOf("online" to (started > 0), "lastSeen" to ServerValue.TIMESTAMP))
    }
   }
 }

 override fun onActivityStarted(activity: Activity) {
  started++
  if (started == 1) syncPresence()
 }
 override fun onActivityStopped(activity: Activity) {
  started = (started - 1).coerceAtLeast(0)
  if (started == 0) {
   generation++ // Invalidate any pending request to publish online.
   offline()
  }
 }
 override fun onActivityCreated(activity: Activity, state: Bundle?) {}
 override fun onActivityResumed(activity: Activity) {}
 override fun onActivityPaused(activity: Activity) {}
 override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) {}
 override fun onActivityDestroyed(activity: Activity) {}

 companion object {
  fun offline() {
   val uid = Backend.uid
   if (uid.isNotEmpty()) Backend.db.getReference("presence/$uid")
    .setValue(mapOf("online" to false, "lastSeen" to ServerValue.TIMESTAMP))
  }
 }
}
