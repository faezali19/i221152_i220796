package com.kinnect.i221152_i220796

import android.os.Bundle
import android.widget.*
class MediaViewerActivity:LiveActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);val column=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(0xffffffff.toInt())};column.addView(TextView(this).apply{text="‹  Photos";textSize=20f;setTextColor(0xffffffff.toInt());setBackgroundColor(0xff0b5f63.toInt());setPadding(24,24,24,24);setOnClickListener{finish()}});intent.getStringExtra("urls").orEmpty().split('\n').filter{it.isNotEmpty()}.forEach{url->column.addView(ImageView(this).apply{layoutParams=LinearLayout.LayoutParams(-1,resources.displayMetrics.widthPixels);adjustViewBounds=true;photo(url)})};setContentView(ScrollView(this).apply{addView(column)})}}
