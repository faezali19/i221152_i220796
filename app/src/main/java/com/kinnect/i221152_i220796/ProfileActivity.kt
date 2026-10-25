package com.kinnect.i221152_i220796

import android.os.Bundle
import android.widget.TextView
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class ProfileActivity : LiveActivity() {
 override fun onCreate(b: Bundle?) {
  super.onCreate(b)
  setContentView(R.layout.activity_profile)
  back()

  click(R.id.btnEditProfile) {
   go(EditProfileActivity::class.java)
  }
  click(R.id.btnFindFriends) {
   go(FriendsActivity::class.java)
  }
  click(R.id.btnProfileSearch) {
   go(SearchActivity::class.java)
  }
  click(R.id.btnAddStory) {
   go(PhotoPickerActivity::class.java, "purpose" to "story")
  }

  root.pics("ic_camera").forEach { view ->
   view.setOnClickListener {
    go(EditProfileActivity::class.java)
   }
  }

  watch("users/${Backend.uid}") { user ->
   root.set("Jacob West", user.s("name"))
   root.set("JW", initials(user.s("name")))
   root.set(
    "Digital goodies designer. Everything is designed.",
    user.s("bio")
   )
   root.set(
    "Designer at Pixsellz",
    "@${user.s("username")}"
   )
   root.set("Studied at FAST-NUCES", user.s("website"))
   root.set("Lives in Karachi, Pakistan", "")

   val joined = java.text.SimpleDateFormat(
    "MMMM yyyy",
    java.util.Locale.getDefault()
   ).format(java.util.Date(user.n("createdAt")))

   root.set("Joined March 2019", "Joined $joined")

   root.pics("ph_scene")
    .firstOrNull()
    ?.photo(user.obj("cover").s("url"))

   avatar(
    root.label("JW"),
    user.obj("avatar").s("url")
   )
  }

  watch("friends/${Backend.uid}") { friends ->
   root.set("834 friends", "${friends.size} friends")

   val ids = friends.keys.take(3)
   val placeholders = listOf(
    "LM" to "Lina Marsh",
    "AK" to "Aisha Khan",
    "OF" to "Omar Farooq"
   )

   placeholders.forEachIndexed { index, (initial, name) ->
    if (index < ids.size) {
     once("users/${ids[index]}") { user ->
      root.set(initial, initials(user.s("name")))
      root.set(name, user.s("name"))
      root.label(name)?.setOnClickListener {
       profile(ids[index])
      }
     }
    } else {
     root.set(initial, "")
     root.set(name, "")
    }
   }
  }

  tap("Posts") {
   go(HomeActivity::class.java)
  }
  tap("Photos") {
   ownPhotos(Backend.uid)
  }
  tap("Videos") {
   toast("Your videos appear in your active stories")
  }
  tap("Groups") {
   toast("Groups are outside Assignment 2")
  }
 }
}

fun LiveActivity.avatar(view: TextView?, url: String) {
 if (view == null || url.isBlank()) return

 Glide.with(this)
  .asDrawable()
  .load(url)
  .circleCrop()
  .into(
   object : CustomTarget<Drawable>() {
    override fun onResourceReady(
     resource: Drawable,
     transition: Transition<in Drawable>?
    ) {
     view.backgroundTintList = null
     view.backgroundTintMode = null
     view.text = ""
     view.background = resource
    }

    override fun onLoadCleared(placeholder: Drawable?) {
     // Keep the initials while the photo is loading.
    }
   }
  )
}

fun LiveActivity.ownPhotos(uid: String) {
 Backend.db.getReference("posts")
  .orderByChild("ownerId")
  .equalTo(uid)
  .get()
  .addOnSuccessListener { snapshot ->
   val urls = snapshot.value
    .record()
    .rows()
    .flatMap { post ->
     (post["images"] as? List<*>)
      ?.map { image -> image.record().s("url") }
      .orEmpty()
    }

   if (urls.isEmpty()) {
    toast("No photos yet")
   } else {
    go(
     MediaViewerActivity::class.java,
     "urls" to urls.joinToString("\n")
    )
   }
  }
  .addOnFailureListener {
   toast("Photos are visible to friends")
  }
}