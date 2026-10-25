package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/** UI-only screen. Click listeners navigate; no backend or real media operations. */
class StoryEditorActivity : LiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw the full-screen mockup behind the hidden system bars.
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        setContentView(R.layout.activity_story_editor)
        // These screens are fullscreen mockups; there is no camera permission or recording.
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        findViewById<View>(R.id.btnClose).setOnClickListener { finish() }
        // All share controls preview Your story. Nothing is uploaded.
        for (id in listOf(R.id.btnShareStory, R.id.btnCloseFriends, R.id.btnShareArrow)) {
            findViewById<View>(id).setOnClickListener {
                startActivity(Intent(this, YourStoryActivity::class.java))
            }
        }

    }
}
