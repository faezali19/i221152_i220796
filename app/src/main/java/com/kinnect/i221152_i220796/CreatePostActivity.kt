package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/** UI-only screen. Click listeners navigate; no backend or real media operations. */
class CreatePostActivity : LiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnPost).setOnClickListener { finish() }
        findViewById<View>(R.id.btnPostPhotos).setOnClickListener {
            startActivity(Intent(this, PhotoPickerActivity::class.java))
        }
        findViewById<View>(R.id.btnPostCamera).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        // Post closes this mock composer; there is no real publishing.

    }
}
