package com.kinnect.i221152_i220796

import android.os.*
import android.content.Intent
class SplashActivity:LiveActivity(){
 private val handler=Handler(Looper.getMainLooper())
 private val route=Runnable{if(Backend.uid.isEmpty()){go(LoginActivity::class.java);finish()}else once("users/${Backend.uid}"){u->if(u.b("setupComplete")){go(HomeActivity::class.java);if(intent.hasExtra("chatId"))go(ChatActivity::class.java,"chatId" to intent.getStringExtra("chatId").orEmpty(),"other" to intent.getStringExtra("other").orEmpty());if(intent.hasExtra("callId"))go(VoiceCallActivity::class.java,"callId" to intent.getStringExtra("callId").orEmpty())}else go(EditProfileActivity::class.java);finish()}}
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_splash);handler.postDelayed(route,5000)}
 override fun onDestroy(){handler.removeCallbacks(route);super.onDestroy()}
}
