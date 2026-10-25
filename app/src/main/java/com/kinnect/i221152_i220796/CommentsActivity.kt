package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/** UI-only screen. Click listeners navigate; no backend or real media operations. */
class CommentsActivity : LiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        // Reply text is editable; sending comments is outside this assignment.

    }
}
