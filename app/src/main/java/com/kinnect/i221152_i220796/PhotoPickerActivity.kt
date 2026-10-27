package com.kinnect.i221152_i220796

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
class PhotoPickerActivity:LiveActivity(){private var selected= arrayListOf<String>();private val picker=registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){uris->selected=ArrayList(uris.take(20).map{it.toString()});uris.forEach{try{contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:SecurityException){}};render()}
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_photo_picker);selected=b?.getStringArrayList("selected")?:arrayListOf();click(R.id.btnCancel){finish()};click(R.id.btnNext){if(selected.isEmpty())toast("Select media first")else if(intent.getStringExtra("purpose")=="story")go(StoryEditorActivity::class.java,"uri" to selected.first())else startActivity(Intent(this,CreatePostActivity::class.java).putStringArrayListExtra("uris",selected))};click(R.id.btnPickerCamera){go(CameraActivity::class.java,"purpose" to intent.getStringExtra("purpose").orEmpty())};tap("Gallery"){launch()};tap("Video"){go(CameraActivity::class.java,"purpose" to "story","video" to "true")};tap("Recents ⌄"){launch()};root.pics("kn_photo_preview").firstOrNull()?.setOnClickListener{launch()};render();if(b==null)launch()}
 private fun launch(){picker.launch(if(intent.getStringExtra("purpose")=="story")arrayOf("image/*","video/*")else arrayOf("image/*"))}
 private fun render(){findViewById<TextView>(R.id.btnNext).text="Next (${selected.size})";listOf("1","2","0:23").forEach{root.set(it,"")};root.pics("ph_scene").forEachIndexed{i,v->if(i<selected.size){v.photo(selected[i]);v.setOnClickListener{selected.removeAt(i);render()}}else{v.setImageDrawable(null);v.setOnClickListener{launch()}}};root.pics("kn_photo_preview").firstOrNull()?.let{if(selected.isNotEmpty())it.photo(selected.first())else it.setImageDrawable(null)}}
 override fun onSaveInstanceState(b:Bundle){b.putStringArrayList("selected",selected);super.onSaveInstanceState(b)}
}
