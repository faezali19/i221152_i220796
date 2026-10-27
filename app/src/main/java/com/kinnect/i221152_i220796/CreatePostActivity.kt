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
    private var selected = emptyList<String>()
    private var uploaded = arrayOfNulls<Record>(0)
    private var publishing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        selected = intent.getStringArrayListExtra("uris")?.distinct()?.take(20).orEmpty()
        uploaded = arrayOfNulls(selected.size)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        once("users/${Backend.uid}") {
            root.set("Jacob West", it.s("name"))
            root.set("JW", initials(it.s("name")))
        }
        findViewById<View>(R.id.btnPostPhotos).setOnClickListener {
            startActivity(Intent(this, PhotoPickerActivity::class.java))
        }
        findViewById<View>(R.id.btnPostCamera).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        showSelectedPhotos()
        selected.forEachIndexed { index, value ->
            val uri = Uri.parse(value)
            Backend.upload(uri, contentResolver.getType(uri) ?: "image/jpeg") { media, error ->
                if (!isFinishing && !isDestroyed) {
                    if (error != null || media == null) {
                        toast(error ?: "Photo upload failed; reopen the composer to retry")
                    } else {
                        uploaded[index] = media
                        root.set("Photo/video", "${uploaded.count { it != null }} photos ready")
                    }
                }
            }
        }
        findViewById<View>(R.id.btnPost).setOnClickListener {
            if (publishing) return@setOnClickListener
            if (uploaded.any { it == null }) {
                toast("Wait for all selected photos to upload. If an upload failed, reopen the composer.")
                return@setOnClickListener
            }
            val postText = text(R.id.postText).trim()
            if (postText.isEmpty() && selected.isEmpty()) {
                toast("Add text or select a photo")
                return@setOnClickListener
            }
            publishing = true
            Backend.api("createPost", mapOf("text" to postText, "media" to uploaded.filterNotNull())) { _, error ->
                if (!isFinishing && !isDestroyed) {
                    publishing = false
                    if (error != null) toast(error) else {
                        go(HomeActivity::class.java)
                        finish()
                    }
                }
            }
        }
    }

    private fun showSelectedPhotos() {
        val preview = findViewById<HorizontalScrollView>(R.id.postMediaPreview)
        val items = findViewById<LinearLayout>(R.id.postMediaItems)
        preview.visibility = if (selected.isEmpty()) View.GONE else View.VISIBLE
        items.removeAllViews()
        val density = resources.displayMetrics.density
        selected.forEachIndexed { index, value ->
            val image = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (160 * density).roundToInt(), (210 * density).roundToInt()
                ).apply { marginEnd = (8 * density).roundToInt() }
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = "Selected photo ${index + 1}"
            }
            Glide.with(this).load(Uri.parse(value)).centerCrop().into(image)
            items.addView(image)
        }
    }
}
