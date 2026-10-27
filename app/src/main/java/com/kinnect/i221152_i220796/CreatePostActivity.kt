package com.kinnect.i221152_i220796

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import kotlin.math.roundToInt

class CreatePostActivity : LiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnPost).setOnClickListener {
            // Publishing is implemented in a later stage.
            finish()
        }

        findViewById<View>(R.id.btnPostPhotos).setOnClickListener {
            startActivity(Intent(this, PhotoPickerActivity::class.java))
        }

        findViewById<View>(R.id.btnPostCamera).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        showSelectedPhotos()
    }

    private fun showSelectedPhotos() {
        val uris = intent.getStringArrayListExtra("uris")
            ?.distinct()
            ?.take(20)
            .orEmpty()

        val preview = findViewById<HorizontalScrollView>(R.id.postMediaPreview)
        val items = findViewById<LinearLayout>(R.id.postMediaItems)

        if (uris.isEmpty()) {
            preview.visibility = View.GONE
            return
        }

        preview.visibility = View.VISIBLE
        items.removeAllViews()

        val density = resources.displayMetrics.density
        val imageWidth = (160 * density).roundToInt()
        val imageHeight = (210 * density).roundToInt()
        val gap = (8 * density).roundToInt()

        uris.forEachIndexed { index, value ->
            val image = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    imageWidth,
                    imageHeight
                ).apply {
                    marginEnd = gap
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = "Selected photo ${index + 1}"
            }

            Glide.with(this)
                .load(Uri.parse(value))
                .centerCrop()
                .into(image)

            items.addView(image)
        }
    }
}