package com.kinnect.i221152_i220796

import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts

class EditProfileActivity : LiveActivity() {
 private var target = "avatar"
 private val media = mutableMapOf<String, Any?>()

 private val pick =
  registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
   if (uri != null) {
    Backend.upload(
     uri,
     contentResolver.getType(uri) ?: "image/jpeg"
    ) { uploaded, error ->
     when {
      error != null -> toast(error)
      uploaded == null -> toast("Photo upload returned no data")
      else -> {
       media[target] = uploaded
       val url = uploaded.s("url")

       if (target == "cover") {
        root.pics("ph_scene")
         .firstOrNull()
         ?.photo(url)
       } else {
        avatar(root.label("JW"), url)
       }

       toast("Photo uploaded; Save to apply")
      }
     }
    }
   }
  }

 override fun onCreate(b: Bundle?) {
  super.onCreate(b)
  setContentView(R.layout.activity_edit_profile)
  back()

  click(R.id.btnCancel) {
   if (Backend.uid.isNotEmpty()) {
    once("users/${Backend.uid}") {
     if (it.b("setupComplete")) finish() else logout()
    }
   } else {
    finish()
   }
  }

  once("users/${Backend.uid}") { user ->
   if (user.isEmpty()) {
    api(
     "register",
     mapOf(
      "name" to (
              Backend.auth.currentUser
               ?.email
               ?.substringBefore('@')
               ?: "Kinnect user"
              )
     )
    ) {
     load()
    }
   } else {
    load()
   }
  }

  root.labels("Edit").forEachIndexed { index, view ->
   view.setOnClickListener {
    target = if (index == 0) "avatar" else "cover"
    pick.launch("image/*")
   }
  }

  click(R.id.btnSave) {
   val data = mapOf(
    "name" to text(R.id.editProfileField0),
    "username" to text(R.id.editProfileField1),
    "website" to text(R.id.editProfileField2),
    "bio" to text(R.id.editProfileField3),
    "phone" to text(R.id.editProfileField6),
    "gender" to text(R.id.editProfileField7)
   ) + media

   api("profile", data) {
    go(HomeActivity::class.java)
    finish()
   }
  }
 }

 private fun load() {
  once("users/${Backend.uid}") { user ->
   mapOf(
    R.id.editProfileField0 to "name",
    R.id.editProfileField1 to "username",
    R.id.editProfileField2 to "website",
    R.id.editProfileField3 to "bio"
   ).forEach { (id, key) ->
    findViewById<EditText>(id).setText(user.s(key))
   }

   root.set("JW", initials(user.s("name")))
   avatar(root.label("JW"), user.obj("avatar").s("url"))
   root.pics("ph_scene")
    .firstOrNull()
    ?.photo(user.obj("cover").s("url"))
  }

  once("privateUsers/${Backend.uid}") { user ->
   mapOf(
    R.id.editProfileField5 to "email",
    R.id.editProfileField6 to "phone",
    R.id.editProfileField7 to "gender"
   ).forEach { (id, key) ->
    findViewById<EditText>(id).setText(user.s(key))
   }

   findViewById<EditText>(R.id.editProfileField5).isEnabled = false
  }
 }
}