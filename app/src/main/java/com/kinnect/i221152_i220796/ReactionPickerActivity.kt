package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/** UI-only screen. Click listeners navigate; no backend or real media operations. */
class ReactionPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reaction_picker)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnCloseReactions).setOnClickListener { finish() }
        findViewById<View>(R.id.reactionShade).setOnClickListener { finish() }
        // Choosing an emoji only closes the picker. No reaction is stored.
        for (id in listOf(R.id.reaction0, R.id.reaction1, R.id.reaction2,
                R.id.reaction3, R.id.reaction4, R.id.reaction5)) {
            findViewById<View>(id).setOnClickListener { finish() }
        }

    }
}
