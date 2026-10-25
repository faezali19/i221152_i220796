package com.kinnect.i221152_i220796

import android.os.Bundle
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
class SearchActivity:LiveActivity(){private var users:Record=emptyMap();override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_search);back();findViewById<EditText>(R.id.searchInput).apply{setText("");hint="Search name or username";doAfterTextChanged{show()}};watch("users"){users=it;show()}}
 private fun show(){val q=text(R.id.searchInput).lowercase();val rows=users.rows().filter{it.s("id")!=Backend.uid&&it.b("setupComplete")&&(it.s("name").lowercase().contains(q)||it.s("username").contains(q.removePrefix("@")))};list(R.id.searchList,R.layout.item_search,rows){v,u->v.set("OS",initials(u.s("name")));v.set("Omar Siddiqui",u.s("name"));v.set("12 mutual friends · FAST-NUCES","@${u.s("username")}");v.setOnClickListener{profile(u.s("id"))};v.pics("ic_person_add").forEach{it.setOnClickListener{api("friend",mapOf("other" to u.s("id"),"operation" to "request")){toast("Request sent")}}}}}
}
