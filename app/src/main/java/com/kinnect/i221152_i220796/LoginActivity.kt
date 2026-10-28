package com.kinnect.i221152_i220796

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.TouchDelegate
import android.view.View
import android.widget.EditText

class LoginActivity : LiveActivity() {

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_login)

  enablePasswordEye(findViewById(R.id.etPassword))

  val prefs = getSharedPreferences("kinnect", MODE_PRIVATE)
  val last = prefs.getString("email", "").orEmpty()

  root.set("Jacob West", if (last.isEmpty()) "Recent login" else last)
  root.set("JW", if (last.isEmpty()) "K" else last.take(2).uppercase())
  root.set("2", "")

  click(R.id.recentLoginCard) {
   findViewById<EditText>(R.id.etEmail).setText(last)
  }

  click(R.id.btnCreateAccount) {
   go(SignUpActivity::class.java)
  }

  click(R.id.btnLogin) {
   val email = text(R.id.etEmail)
   val password = findViewById<EditText>(R.id.etPassword)
    .text.toString()

   if (email.isEmpty() || password.isEmpty()) {
    toast("Enter email and password")
    return@click
   }

   val button = findViewById<View>(R.id.btnLogin)
   button.isEnabled = false

   Backend.auth.signInWithEmailAndPassword(email, password)
    .addOnCompleteListener { result ->
     if (isFinishing || isDestroyed) {
      return@addOnCompleteListener
     }

     button.isEnabled = true

     if (!result.isSuccessful) {
      toast(result.exception?.localizedMessage ?: "Login failed")
     } else {
      prefs.edit().putString("email", email).apply()

      once("users/${Backend.uid}") { user ->
       val destination =
        if (user.b("setupComplete")) {
         HomeActivity::class.java
        } else {
         EditProfileActivity::class.java
        }

       startActivity(
        Intent(this, destination).addFlags(
         Intent.FLAG_ACTIVITY_NEW_TASK or
                 Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
       )
       finish()
      }
     }
    }
  }

  tap("Forgot password?") {
   ask("Account email", text(R.id.etEmail)) { email ->
    Backend.auth.sendPasswordResetEmail(email.trim())
     .addOnCompleteListener { result ->
      toast(
       if (result.isSuccessful) {
        "Password reset email requested"
       } else {
        result.exception?.localizedMessage
         ?: "Request failed"
       }
      )
     }
   }
  }
 }
}

/*
 * Shared by LoginActivity and SignUpActivity.
 * Keeps the existing eye icon and enlarges its touch area
 * without changing its visible size or the layout.
 */
fun LiveActivity.enablePasswordEye(password: EditText) {
 password.transformationMethod = PasswordTransformationMethod.getInstance()

 root.pics("ic_eye").forEach { eye ->
  eye.contentDescription = "Show password"
  eye.isFocusable = true

  eye.setOnClickListener {
   val start = password.selectionStart
   val end = password.selectionEnd
   val hidden =
    password.transformationMethod is PasswordTransformationMethod

   password.transformationMethod =
    if (hidden) null else PasswordTransformationMethod.getInstance()

   eye.contentDescription =
    if (hidden) "Hide password" else "Show password"

   if (start >= 0 && end >= 0) {
    password.setSelection(
     start.coerceAtMost(password.length()),
     end.coerceAtMost(password.length())
    )
   }
  }

  val parent = eye.parent as? View
  parent?.post {
   val bounds = Rect()
   eye.getHitRect(bounds)

   val targetSize =
    (48 * resources.displayMetrics.density + 0.5f).toInt()

   val extraX = ((targetSize - bounds.width()) / 2).coerceAtLeast(0)
   val extraY = ((targetSize - bounds.height()) / 2).coerceAtLeast(0)

   bounds.inset(-extraX, -extraY)
   bounds.left = bounds.left.coerceAtLeast(0)
   bounds.top = bounds.top.coerceAtLeast(0)
   bounds.right = bounds.right.coerceAtMost(parent.width)
   bounds.bottom = bounds.bottom.coerceAtMost(parent.height)

   parent.touchDelegate = TouchDelegate(bounds, eye)
  }
 }
}