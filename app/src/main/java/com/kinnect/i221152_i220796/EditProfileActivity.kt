package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/** UI-only screen. Click listeners navigate; no backend or real media operations. */
class EditProfileActivity : LiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        findViewById<View>(R.id.btnCancel).setOnClickListener { finish() }
        findViewById<View>(R.id.btnSave).setOnClickListener { finish() }
        // Save closes this UI-only form; it does not persist edited values.

    }
}
