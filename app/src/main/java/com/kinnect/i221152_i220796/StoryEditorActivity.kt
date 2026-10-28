package com.kinnect.i221152_i220796

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import com.bumptech.glide.Glide
import kotlin.math.roundToInt

class StoryEditorActivity : LiveActivity() {
 private var media: Record? = null
 private var audience = "friends"
 private var caption = ""
 private var publishing = false

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_story_editor)
  back()

  caption = savedInstanceState?.getString("caption").orEmpty()
  audience = savedInstanceState?.getString("audience") ?: "friends"
  showStoryCaption("Saturday plans", caption)

  val value = intent.getStringExtra("uri").orEmpty()
  if (value.isEmpty()) {
   toast("Select a photo or video first")
   finish()
   return
  }

  val uri = Uri.parse(value)
  val type = contentResolver.getType(uri)
   ?: if (value.endsWith(".mp4", ignoreCase = true)) {
    "video/mp4"
   } else {
    "image/jpeg"
   }

  showStoryMedia(root, "kn_story_green", value, type)

  Backend.upload(uri, type) { result, error ->
   if (!isFinishing && !isDestroyed) {
    if (error != null) toast(error) else media = result
   }
  }

  tap("Aa") {
   ask("Story text", caption) {
    caption = it
    showStoryCaption("Saturday plans", caption)
   }
  }

  click(R.id.btnCloseFriends) {
   audience = "close"
   toast("Audience: close friends. Tap the share arrow to publish.")
  }

  click(R.id.btnShareStory) {
   audience = "friends"
   share()
  }

  click(R.id.btnShareArrow) {
   share()
  }

  once("users/${Backend.uid}") {
   root.set("JW", initials(it.s("name")))
  }
 }

 private fun share() {
  if (publishing) return

  val uploaded = media
  if (uploaded == null) {
   toast("Wait for upload to finish")
   return
  }

  publishing = true
  Backend.api(
   "story",
   mapOf(
    "media" to uploaded,
    "label" to caption,
    "audience" to audience
   )
  ) { result, error ->
   if (!isFinishing && !isDestroyed) {
    publishing = false
    if (error != null) {
     toast(error)
    } else {
     go(
      YourStoryActivity::class.java,
      "storyId" to result.orEmpty().s("id")
     )
     finish()
    }
   }
  }
 }

 override fun onStart() {
  super.onStart()
  resumeStoryVideos()
 }

 override fun onStop() {
  pauseStoryVideos()
  super.onStop()
 }

 override fun onSaveInstanceState(outState: Bundle) {
  outState.putString("caption", caption)
  outState.putString("audience", audience)
  super.onSaveInstanceState(outState)
 }
}

fun LiveActivity.showStoryCaption(originalLabel: String, caption: String) {
 root.labels(originalLabel).forEach {
  it.text = caption
  it.visibility = if (caption.isBlank()) View.GONE else View.VISIBLE
 }
}

fun LiveActivity.showStoryMedia(
 screen: View,
 drawable: String,
 url: String,
 type: String
) {
 val holderTag = "storyMedia:$drawable"
 var holder = screen.findViewWithTag<FrameLayout>(holderTag)

 if (holder == null) {
  val original = screen.pics(drawable).firstOrNull() ?: return
  val parent = original.parent as? ViewGroup ?: return
  val position = parent.indexOfChild(original)
  val parameters = original.layoutParams

  parent.removeView(original)
  holder = FrameLayout(this).apply {
   tag = holderTag
   setBackgroundColor(Color.BLACK)
   clipChildren = true
  }
  parent.addView(holder, position, parameters)
 }

 val container = holder
 val mediaKey = "$type|$url"

 // A database update should not restart an unchanged video.
 if (container.childCount == 1 &&
  container.getChildAt(0).tag == mediaKey
 ) {
  return
 }

 for (index in 0 until container.childCount) {
  (container.getChildAt(index) as? VideoView)?.stopPlayback()
 }
 container.removeAllViews()

 if (!type.startsWith("video/")) {
  val image = ImageView(this).apply {
   tag = mediaKey
   scaleType = ImageView.ScaleType.FIT_CENTER
  }
  container.addView(image, FrameLayout.LayoutParams(-1, -1))
  Glide.with(this).load(url).fitCenter().into(image)
  return
 }

 val video = VideoView(this).apply {
  tag = mediaKey
 }
 container.addView(
  video,
  FrameLayout.LayoutParams(-1, -1, Gravity.CENTER)
 )

 var videoWidth = 0
 var videoHeight = 0

 fun resizeVideo() {
  if (videoWidth <= 0 || videoHeight <= 0 ||
   container.width <= 0 || container.height <= 0
  ) {
   return
  }

  val scale = minOf(
   container.width.toFloat() / videoWidth,
   container.height.toFloat() / videoHeight
  )
  val width = (videoWidth * scale).roundToInt().coerceAtLeast(1)
  val height = (videoHeight * scale).roundToInt().coerceAtLeast(1)

  val current = video.layoutParams as FrameLayout.LayoutParams
  if (current.width != width || current.height != height) {
   video.layoutParams =
    FrameLayout.LayoutParams(width, height, Gravity.CENTER)
  }
 }

 container.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
  if (video.parent === container) resizeVideo()
 }

 video.setOnPreparedListener { player ->
  videoWidth = player.videoWidth
  videoHeight = player.videoHeight
  player.isLooping = true

  player.setOnVideoSizeChangedListener { _, width, height ->
   videoWidth = width
   videoHeight = height
   resizeVideo()
  }

  resizeVideo()
  video.start()
 }

 video.setOnErrorListener { _, _, _ ->
  toast("Video could not be played. Check the backend connection.")
  true
 }

 video.setVideoURI(Uri.parse(url))
}

fun LiveActivity.pauseStoryVideos() {
 root.all().filterIsInstance<VideoView>().forEach { it.pause() }
}

fun LiveActivity.resumeStoryVideos() {
 root.all().filterIsInstance<VideoView>().forEach { it.start() }
}