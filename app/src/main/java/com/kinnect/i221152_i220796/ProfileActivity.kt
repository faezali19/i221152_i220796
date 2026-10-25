package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/** UI-only screen. Click listeners navigate; no backend or real media operations. */
class ProfileActivity : LiveActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        findViewById<View>(R.id.btnProfileSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        findViewById<View>(R.id.btnFindFriends).setOnClickListener {
            startActivity(Intent(this, FriendsActivity::class.java))
        }
        findViewById<View>(R.id.btnAddStory).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

    }
}
