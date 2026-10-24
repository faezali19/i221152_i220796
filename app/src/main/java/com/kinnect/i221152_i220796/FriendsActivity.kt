package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** UI-only navigation. Intent extras carry the selected display identity, not account data. */
class FriendsActivity : BaseTabActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)
        setupTabs(1, "Friends")
        findViewById<View>(R.id.requestRow1).setOnClickListener {
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra("person_name", "Sara Iqbal")
            intent.putExtra("person_initials", "SI")
            startActivity(intent)
        }
        findViewById<View>(R.id.requestRow2).setOnClickListener {
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra("person_name", "Bilal Ahmed")
            intent.putExtra("person_initials", "BA")
            startActivity(intent)
        }
        findViewById<View>(R.id.requestRow3).setOnClickListener {
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra("person_name", "Noor Fatima")
            intent.putExtra("person_initials", "NF")
            startActivity(intent)
        }
        findViewById<View>(R.id.requestRow4).setOnClickListener {
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra("person_name", "Daniyal Shah")
            intent.putExtra("person_initials", "DS")
            startActivity(intent)
        }

    }
}
