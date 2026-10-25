package com.kinnect.i221152_i220796
class KinnectApp:android.app.Application(){companion object{fun offline(){if(Backend.uid.isNotEmpty())Backend.db.getReference("presence/${Backend.uid}").setValue(mapOf("online" to false,"lastSeen" to com.google.firebase.database.ServerValue.TIMESTAMP))}}}
