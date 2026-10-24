package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** UI-only navigation. Intent extras carry the selected display identity, not account data. */
class ChatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.chatRow1).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("person_name", "Aisha Khan")
            intent.putExtra("person_initials", "AK")
            startActivity(intent)
        }
        findViewById<View>(R.id.chatRow2).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("person_name", "Design crew")
            intent.putExtra("person_initials", "DC")
            startActivity(intent)
        }
        findViewById<View>(R.id.chatRow3).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("person_name", "Lina Marsh")
            intent.putExtra("person_initials", "LM")
            startActivity(intent)
        }
        findViewById<View>(R.id.chatRow4).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("person_name", "Omar Farooq")
            intent.putExtra("person_initials", "OF")
            startActivity(intent)
        }
        findViewById<View>(R.id.chatRow5).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("person_name", "Bilal Ahmed")
            intent.putExtra("person_initials", "BA")
            startActivity(intent)
        }
        findViewById<View>(R.id.chatRow6).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("person_name", "Noor Fatima")
            intent.putExtra("person_initials", "NF")
            startActivity(intent)
        }

    }
}
