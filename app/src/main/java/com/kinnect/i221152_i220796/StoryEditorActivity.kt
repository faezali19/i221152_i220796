package com.kinnect.i221152_i220796

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.VideoView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide

class StoryEditorActivity : LiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        setContentView(R.layout.activity_story_editor)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        findViewById<View>(R.id.btnClose).setOnClickListener {
            finish()
        }

        showStoryMedia()

        // These are still preview controls; publication is implemented later.
        listOf(
            R.id.btnShareStory,
            R.id.btnCloseFriends,
            R.id.btnShareArrow
        ).forEach { id ->
            findViewById<View>(id).setOnClickListener {
                startActivity(
                    android.content.Intent(
                        this,
                        YourStoryActivity::class.java
                    )
                )
            }
        }
    }

    private fun showStoryMedia() {
        val value = intent.getStringExtra("uri") ?: return
        val uri = Uri.parse(value)
        val mimeType = contentResolver.getType(uri).orEmpty()
        val path = value.substringBefore('?').lowercase()

        val isVideo = mimeType.startsWith("video/") ||
                path.endsWith(".mp4") ||
                path.endsWith(".3gp") ||
                path.endsWith(".webm") ||
                path.endsWith(".mkv") ||
                path.endsWith(".mov")

        findViewById<ImageView>(R.id.storyBackdrop).visibility = View.GONE
        findViewById<View>(R.id.storySampleSticker).visibility = View.GONE

        if (isVideo) {
            val video = findViewById<VideoView>(R.id.storyVideoPreview)
            video.visibility = View.VISIBLE
            video.setVideoURI(uri)
            video.setOnPreparedListener { player ->
                player.isLooping = true
                video.start()
            }
            video.setOnErrorListener { _, _, _ ->
                toast("Could not preview this video")
                true
            }
        } else {
            val image = findViewById<ImageView>(R.id.storyImagePreview)
            image.visibility = View.VISIBLE

            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(image)
        }
    }
}