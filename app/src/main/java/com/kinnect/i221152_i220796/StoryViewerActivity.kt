package com.kinnect.i221152_i220796

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText

class StoryViewerActivity : LiveActivity() {

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_story_viewer)
  back()

  val storyId = intent.getStringExtra("storyId").orEmpty()
  if (storyId.isEmpty()) {
   toast("Open a story from Home")
   finish()
   return
  }

  var owner = ""
  showStoryCaption("Karachi nights", "")

  watch("storyFeeds/${Backend.uid}/$storyId") { story ->
   if (story.isEmpty() || story.n("expiresAt") <= Backend.now) {
    toast("Story expired")
    finish()
    return@watch
   }

   owner = story.s("ownerId")
   showStoryCaption("Karachi nights", story.s("label"))
   root.set("5h ·", "${age(story.n("createdAt"))} ·")

   val media = story.obj("media")
   showStoryMedia(
    root,
    "kn_story_night",
    media.s("url"),
    media.s("type")
   )

   once("users/$owner") { user ->
    root.set("Omar Farooq", user.s("name"))
    root.set("OF", initials(user.s("name")))
   }
  }

  api("viewStory", mapOf("storyId" to storyId))

  mapOf(
   "ic_kn_story_thumb" to "like",
   "ic_kn_story_love" to "love",
   "ic_kn_story_laugh" to "haha"
  ).forEach { (iconName, reaction) ->
   root.pics(iconName).forEach { icon ->
    icon.setOnClickListener {
     api(
      "storyReaction",
      mapOf("storyId" to storyId, "reaction" to reaction)
     ) {
      toast("Reaction sent")
     }
    }
   }
  }

  root.all().filterIsInstance<EditText>().firstOrNull()?.apply {
   imeOptions = EditorInfo.IME_ACTION_SEND

   setOnEditorActionListener { _, actionId, _ ->
    if (actionId != EditorInfo.IME_ACTION_SEND) {
     false
    } else {
     val message = text.toString().trim()

     if (owner.isNotEmpty() && message.isNotEmpty()) {
      api("openChat", mapOf("other" to owner)) { chat ->
       api(
        "message",
        mapOf(
         "chatId" to chat.s("id"),
         "text" to "Story reply: $message"
        )
       ) {
        setText("")
        toast("Reply sent")
       }
      }
     }
     true
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
}