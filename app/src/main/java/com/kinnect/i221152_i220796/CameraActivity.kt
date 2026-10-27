package com.kinnect.i221152_i220796

import android.Manifest
import android.os.Bundle
import android.net.Uri
import android.content.Intent
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.video.*
import java.io.File
class CameraActivity:LiveActivity(){private var provider:ProcessCameraProvider?=null;private var image:ImageCapture?=null;private var capture:VideoCapture<Recorder>?=null;private var recording:Recording?=null;private var front=false;private var flash=false;private var video=false;private lateinit var preview:PreviewView
 private val permission=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){p->if(p[Manifest.permission.CAMERA]==true)startCamera()else toast("Camera permission is required")}
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_camera);back();video=intent.getStringExtra("video")=="true";val old=root.pics("kn_story_green").first();val parent=old.parent as ViewGroup;val index=parent.indexOfChild(old);val params=old.layoutParams;parent.removeView(old);preview=PreviewView(this);parent.addView(preview,index,params)
 click(R.id.btnShutter){if(video)record()else photograph()};findViewById<android.view.View>(R.id.btnShutter)?.setOnLongClickListener{video=true;bind();record();true};click(R.id.btnCameraGallery){go(PhotoPickerActivity::class.java,"purpose" to intent.getStringExtra("purpose").orEmpty())};tap("STORY"){video=false;bind();toast("Story photo selected. Hold shutter for video.")};tap("POST"){video=false;bind()};tap("LIVE"){video=true;bind();toast("Video selected; tap shutter to start / stop")};tap("BOOMERANG"){video=true;bind();toast("Video selected")};tap("TEXT"){toast("Capture a photo, then add text in the story editor")};root.pics("ic_kn_story_refresh").forEach{it.setOnClickListener{if(recording==null){front=!front;bind()}}};root.pics("ic_kn_story_flash").forEach{it.setOnClickListener{flash=!flash;image?.flashMode=if(flash)ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF}};permission.launch(arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO))}
 private fun startCamera(){val future=ProcessCameraProvider.getInstance(this);future.addListener({try{provider=future.get();bind()}catch(e:Exception){toast(e.localizedMessage?:"Camera unavailable")}},ContextCompat.getMainExecutor(this))}
 private fun bind(){try{provider?.unbindAll();val p=Preview.Builder().build();p.setSurfaceProvider(preview.surfaceProvider);image=ImageCapture.Builder().build();capture=VideoCapture.withOutput(Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.SD)).build());provider?.bindToLifecycle(this,if(front)CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,p,if(video)capture else image)}catch(e:Exception){toast(e.localizedMessage?:"Camera unavailable")}}
 private fun photograph(){val file=File(cacheDir,"photo_${System.currentTimeMillis()}.jpg");image?.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(),ContextCompat.getMainExecutor(this),object:ImageCapture.OnImageSavedCallback{override fun onImageSaved(o:ImageCapture.OutputFileResults){next(file,false)};override fun onError(e:ImageCaptureException){toast(e.localizedMessage?:"Capture failed")}})}
 private fun record(){recording?.let{it.stop();recording=null;return};val file=File(cacheDir,"video_${System.currentTimeMillis()}.mp4");var pending=capture?.output?.prepareRecording(this,FileOutputOptions.Builder(file).setFileSizeLimit(45L*1024*1024).build())?:return;if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)==android.content.pm.PackageManager.PERMISSION_GRANTED)pending=pending.withAudioEnabled();recording=pending.start(ContextCompat.getMainExecutor(this)){event->when(event){is VideoRecordEvent.Start->toast("Recording — tap shutter to stop");is VideoRecordEvent.Finalize->{recording=null;if(!event.hasError()||event.error==VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED)next(file,true)else toast("Video recording failed: ${event.error}")}}}}
 private fun next(file:File,isVideo:Boolean){val uri=FileProvider.getUriForFile(this,"$packageName.files",file).toString();if(isVideo||intent.getStringExtra("purpose")=="story")go(StoryEditorActivity::class.java,"uri" to uri)else startActivity(Intent(this,CreatePostActivity::class.java).putStringArrayListExtra("uris",arrayListOf(uri)))}
 override fun onStop(){recording?.stop();recording=null;super.onStop()}
}
