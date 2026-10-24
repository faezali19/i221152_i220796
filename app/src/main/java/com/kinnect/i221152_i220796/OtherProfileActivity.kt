package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** UI-only navigation. Intent extras carry the selected display identity, not account data. */
class OtherProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_profile)
        // Default identity preserves the supplied PDF page 17 example.
        val personName = intent.getStringExtra("person_name") ?: "Omar Farooq"
        val personInitials = intent.getStringExtra("person_initials") ?: "OF"
        findViewById<TextView>(R.id.otherProfileTitle).text = personName
        findViewById<TextView>(R.id.otherProfileName).text = personName
        findViewById<TextView>(R.id.otherProfileInitials).text = personInitials
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnOtherSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        findViewById<View>(R.id.btnMessage).setOnClickListener {
            val chatIntent = Intent(this, ChatActivity::class.java)
            chatIntent.putExtra("person_name", personName)
            chatIntent.putExtra("person_initials", personInitials)
            startActivity(chatIntent)
        }
        // Photos, details and action controls remain static PDF mock content.

    }
}
