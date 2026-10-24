package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/** UI-only screen. Click listeners navigate; no backend or real media operations. */
class HomeActivity : BaseTabActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupTabs(0)
        findViewById<View>(R.id.composerAvatar).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<View>(R.id.btnComment).setOnClickListener {
            startActivity(Intent(this, CommentsActivity::class.java))
        }
        findViewById<View>(R.id.btnLike).setOnClickListener {
            startActivity(Intent(this, ReactionPickerActivity::class.java))
        }
        // A long press also opens the reaction design.
        findViewById<View>(R.id.btnLike).setOnLongClickListener {
            startActivity(Intent(this, ReactionPickerActivity::class.java))
            true
        }
        findViewById<View>(R.id.composerBox).setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
        findViewById<View>(R.id.btnPhotoIcon).setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
        findViewById<View>(R.id.storyCreate).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        findViewById<View>(R.id.storyOmar).setOnClickListener {
            startActivity(Intent(this, StoryViewerActivity::class.java))
        }

    }
}
