package com.kinnect.i221152_i220796

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

/** Screen 22: Voice call (PDF page 22). End-call button returns to the Chat. */
class VoiceCallActivity : LiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_call)

        // Teal status bar with light icons to match this full-teal screen
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        findViewById<View>(R.id.btnEndCall).setOnClickListener { finish() }
    }
}