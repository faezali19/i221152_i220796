package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/** Screen 02: Log in. Buttons only navigate; no real login logic. */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // "Log in" -> Home. finish() removes Login so Back doesn't return to it
        findViewById<View>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // "Create new account" -> Sign up
        findViewById<View>(R.id.btnCreateAccount).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}