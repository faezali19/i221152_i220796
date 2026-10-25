package com.kinnect.i221152_i220796

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/** Screen 03: Sign up. UI only; buttons just navigate back to Login. */
class SignUpActivity : LiveActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Back arrow -> previous screen (Login)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        // "Log in" link -> back to Login
        findViewById<View>(R.id.btnLoginLink).setOnClickListener { finish() }

        // "Create account" -> back to Login (no real signup logic needed)
        findViewById<View>(R.id.btnCreateAccount).setOnClickListener { finish() }
    }
}