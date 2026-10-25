package com.kinnect.i221152_i220796

import android.os.Bundle
import android.view.View
class NotificationsActivity:BaseTabActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_notifications);setupTabs(3,"Notifications");watch("notifications/${Backend.uid}"){d->list(R.id.notificationsList,R.layout.item_notification,d.rows().sortedByDescending{it.n("createdAt")}){v,n->v.set("Sara Iqbal sent you a friend request.",n.s("text"));v.set("20m",age(n.n("createdAt")));once("users/${n.s("from")}"){v.set("SI",initials(it.s("name")))};val isRequest=n.s("type")=="friend";listOf("Confirm","Delete").forEach{t->v.label(t)?.apply{visibility=if(isRequest)View.VISIBLE else View.GONE;setOnClickListener{api("friend",mapOf("other" to n.s("from"),"operation" to if(t=="Confirm")"accept" else "delete")){visibility=View.GONE}}}};v.setOnClickListener{if(n.s("postId").isNotEmpty())go(CommentsActivity::class.java,"postId" to n.s("postId"))else profile(n.s("from"))}}}}}
