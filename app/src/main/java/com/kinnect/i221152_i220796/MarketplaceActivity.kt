package com.kinnect.i221152_i220796

import android.os.Bundle

/** Screen 23: Marketplace (PDF page 23). */
class MarketplaceActivity : BaseTabActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marketplace)
        setupTabs(2, "Marketplace")
    }
}