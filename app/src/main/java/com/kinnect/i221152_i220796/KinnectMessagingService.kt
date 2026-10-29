package com.kinnect.i221152_i220796

import android.app.*
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.*
class KinnectMessagingService:FirebaseMessagingService(){
 override fun onNewToken(token:String){if(Backend.uid.isNotEmpty())Backend.db.getReference("tokens/${Backend.uid}/$token").setValue(true)}
 override fun onMessageReceived(m:RemoteMessage){val intent=Intent(this,SplashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);m.data.forEach{intent.putExtra(it.key,it.value)};val id=(m.messageId?:System.nanoTime().toString()).hashCode();val pi=PendingIntent.getActivity(this,id,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);val n=NotificationCompat.Builder(this,"kinnect_messages").setSmallIcon(R.drawable.ic_messenger).setContentTitle(m.notification?.title?:"Kinnect").setContentText(m.notification?.body?:"New message").setContentIntent(pi).setAutoCancel(true).build();try{NotificationManagerCompat.from(this).notify(id,n)}catch(_:SecurityException){} }
}
