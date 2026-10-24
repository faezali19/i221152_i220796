package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** UI-only navigation. Intent extras carry the selected display identity, not account data. */
class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.rowOmar).setOnClickListener {
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra("person_name", "Omar Farooq")
            intent.putExtra("person_initials", "OF")
            startActivity(intent)
        }
        findViewById<View>(R.id.rowOmarS).setOnClickListener {
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra("person_name", "Omar Siddiqui")
            intent.putExtra("person_initials", "OS")
            startActivity(intent)
        }
        findViewById<View>(R.id.rowOmarT).setOnClickListener {
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra("person_name", "Omar Tariq")
            intent.putExtra("person_initials", "OT")
            startActivity(intent)
        }

    }
}
