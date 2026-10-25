package com.kinnect.i221152_i220796

import android.os.Bundle
class MenuActivity:BaseTabActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_menu);setupTabs(4,"Menu");watch("users/${Backend.uid}"){root.set("Jacob West",it.s("name"));root.set("JW",initials(it.s("name")))};click(R.id.btnProfileCard){profile(Backend.uid)};click(R.id.btnLogout){logout()};tap("Friends"){go(FriendsActivity::class.java)};tap("Marketplace"){go(MarketplaceActivity::class.java)}}}
