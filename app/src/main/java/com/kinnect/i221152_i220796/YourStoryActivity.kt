package com.kinnect.i221152_i220796

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog

class YourStoryActivity : LiveActivity() {
 private var viewers = emptyList<String>()
 private var viewerDialog: AlertDialog? = null

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_your_story)
  back()

  val storyId = intent.getStringExtra("storyId").orEmpty()
  if (storyId.isEmpty()) {
   toast("Open a story from Home")
   finish()
   return
  }

  showStoryCaption("Saturday plans", "")
  root.set("Seen by 24", "Seen by 0")

  val viewerArea = root.label("Seen by 24")?.parent as? ViewGroup
  if (viewerArea != null) {
   viewerArea.all().filter { it !== viewerArea }.forEach {
    it.setOnClickListener(null)
    it.isClickable = false
    it.isFocusable = false
   }
   viewerArea.isFocusable = true
   viewerArea.setOnClickListener { openViewers() }
  }

  updateReactions(emptyMap())

  watch("stories/$storyId") { story ->
   if (story.isEmpty() || story.n("expiresAt") <= Backend.now) {
    toast("Story expired")
    finish()
    return@watch
   }

   showStoryCaption("Saturday plans", story.s("label"))

   val audience =
    if (story.s("audience") == "close") "Close friends" else "Friends"

   root.set(
    "11h · Close friends",
    "${age(story.n("createdAt"))} · $audience"
   )

   val media = story.obj("media")
   showStoryMedia(
    root,
    "kn_story_green",
    media.s("url"),
    media.s("type")
   )
  }

  once("users/${Backend.uid}") {
   root.set("JW", initials(it.s("name")))
  }

  watch("storyViews/$storyId") { records ->
   viewers = records.keys.filter { it != Backend.uid }
   root.set("Seen by 24", "Seen by ${viewers.size}")

   listOf("AK", "LM", "OF").forEachIndexed { index, tag ->
    val avatar = root.label(tag)
    val uid = viewers.getOrNull(index)

    avatar?.visibility =
     if (uid == null) View.GONE else View.VISIBLE

    if (uid != null) {
     once("users/$uid") { user ->
      if (viewers.getOrNull(index) == uid) {
       root.set(tag, initials(user.s("name")))
      }
     }
    }
   }
  }

  watch("storyReactions/$storyId") {
   updateReactions(it)
  }

  click(R.id.btnCreateAnother) {
   go(PhotoPickerActivity::class.java, "purpose" to "story")
  }

  tap("Create") {
   go(PhotoPickerActivity::class.java, "purpose" to "story")
  }
 }

 private fun openViewers() {
  if (viewerDialog?.isShowing == true) return

  val ids = viewers.toList()
  if (ids.isEmpty()) {
   toast("No viewers yet")
   return
  }

  val names = ids.indices.map { "Loading viewer ${it + 1}..." }
   .toMutableList()

  val adapter = ArrayAdapter(
   this,
   android.R.layout.simple_list_item_1,
   names
  )

  val dialog = AlertDialog.Builder(this)
   .setTitle("Viewers (${ids.size})")
   .setAdapter(adapter) { _, position ->
    profile(ids[position])
   }
   .setNegativeButton("Close", null)
   .create()

  viewerDialog = dialog
  dialog.setOnDismissListener { viewerDialog = null }
  dialog.show()

  ids.forEachIndexed { index, uid ->
   Backend.db.getReference("users/$uid").get()
    .addOnCompleteListener { task ->
     if (!isDestroyed && dialog.isShowing) {
      val name = if (task.isSuccessful) {
       task.result.value.record().s("name")
      } else {
       ""
      }

      names[index] = name.ifBlank { "Viewer ${index + 1}" }
      adapter.notifyDataSetChanged()
     }
    }
  }
 }

 private fun updateReactions(reactions: Record) {
  val actual = reactions.entries
   .sortedBy { it.key }
   .map { it.value.toString() }
   .filter { it in setOf("like", "love", "haha") }

  // Start with the largest existing bubble.
  val icons = root.pics("ic_kn_story_love")
   .sortedByDescending {
    (it.parent as? View)?.layoutParams?.width ?: 0
   }

  icons.forEachIndexed { index, icon ->
   val bubble = icon.parent as? View
   val reaction = actual.getOrNull(index)

   bubble?.visibility =
    if (reaction == null) View.GONE else View.VISIBLE

   if (reaction != null) {
    bubble?.alpha = 1f
    icon.alpha = 1f
    icon.visibility = View.VISIBLE
    icon.contentDescription = reaction

    icon.setImageResource(
     when (reaction) {
      "like" -> R.drawable.ic_kn_story_thumb
      "haha" -> R.drawable.ic_kn_story_laugh
      else -> R.drawable.ic_kn_story_love
     }
    )
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

 override fun onDestroy() {
  viewerDialog?.dismiss()
  viewerDialog = null
  super.onDestroy()
 }
}