package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : LiveActivity() {
 private val handler = Handler(Looper.getMainLooper())
 private var generation = 0
 private val route = Runnable { routeNow() }

 override fun onCreate(b: Bundle?) {
  super.onCreate(b)
  setContentView(R.layout.activity_splash)
  handler.postDelayed(route, 5000)
 }

 override fun onNewIntent(newIntent: Intent) {
  super.onNewIntent(newIntent)
  setIntent(newIntent)
  generation++
  handler.removeCallbacks(route)
  handler.post(route)
 }

 private fun routeNow() {
  val uid = Backend.uid
  val attempt = ++generation
  if (uid.isEmpty()) {
   go(LoginActivity::class.java)
   finish()
   return
  }
  once("users/$uid") { user ->
   if (attempt != generation || isFinishing || isDestroyed || Backend.uid != uid) return@once
   if (!user.b("setupComplete")) {
    go(EditProfileActivity::class.java)
   } else {
    val chatId = intent.getStringExtra("chatId").orEmpty()
    val callId = intent.getStringExtra("callId").orEmpty()
    // Choose one destination: go() deliberately blocks duplicate navigation.
    when {
     callId.isNotBlank() -> go(VoiceCallActivity::class.java, "callId" to callId)
     chatId.isNotBlank() -> go(ChatActivity::class.java,
      "chatId" to chatId, "other" to intent.getStringExtra("other").orEmpty())
     else -> go(HomeActivity::class.java)
    }
   }
   finish()
  }
 }

 override fun onDestroy() {
  generation++
  handler.removeCallbacks(route)
  super.onDestroy()
 }
}
