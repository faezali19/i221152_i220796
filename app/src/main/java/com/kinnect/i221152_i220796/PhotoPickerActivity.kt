package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/** UI-only screen. Click listeners navigate; no backend or real media operations. */
class PhotoPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_picker)
        findViewById<View>(R.id.btnCancel).setOnClickListener { finish() }
        findViewById<View>(R.id.btnNext).setOnClickListener { finish() }
        findViewById<View>(R.id.btnPickerCamera).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        // Next returns to whichever screen opened this mock picker.

    }
}
