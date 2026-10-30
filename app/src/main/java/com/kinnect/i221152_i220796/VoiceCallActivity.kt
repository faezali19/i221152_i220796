package com.kinnect.i221152_i220796

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.internal.EncryptionConfig

class VoiceCallActivity : LiveActivity() {
 private var engine: RtcEngine? = null
 private var callId = ""
 private var call: Record = emptyMap()
 private var joining = false
 private var muted = false
 private var speaker = true
 private var ending = false
 private var offered = false
 private var timeoutPending = false
 private var timeoutRetryAt = 0L
 private var remoteBox: FrameLayout? = null
 private var localBox: FrameLayout? = null
 private var joinedAt = 0L
 private val timer = Handler(Looper.getMainLooper())
 private val tick = object : Runnable {
  override fun run() {
   if (isFinishing || isDestroyed) return
   if (joinedAt > 0) {
    val seconds = ((Backend.now - joinedAt) / 1000).coerceAtLeast(0)
    root.set("03:12", "%02d:%02d".format(seconds / 60, seconds % 60))
   }
   if (call.s("status") == "ringing" && call.n("createdAt") > 0 &&
    Backend.now - call.n("createdAt") >= 30000 &&
    !timeoutPending && Backend.now >= timeoutRetryAt) {
    timeoutPending = true
    Backend.api("callAction", mapOf("callId" to callId, "operation" to "timeout")) { _, _ ->
     timeoutPending = false
     timeoutRetryAt = Backend.now + 5000
    }
   }
   timer.postDelayed(this, 1000)
  }
 }
 private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
  if (grants.values.all { it }) join()
  else { toast("Microphone / camera permission is required"); end() }
 }

 override fun onCreate(state: Bundle?) {
  super.onCreate(state)
  setContentView(R.layout.activity_voice_call)
  callId = intent.getStringExtra("callId").orEmpty()
  if (callId.isEmpty()) { finish(); return }
  root.set("Ammi", "Kinnect call")
  root.set("AM", "")
  root.set("03:12", "Connecting…")
  click(R.id.btnEndCall) { end() }
  onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
   override fun handleOnBackPressed() { end() }
  })
  root.pics("ic_mic").forEach { icon ->
   val action = View.OnClickListener {
    muted = !muted
    engine?.muteLocalAudioStream(muted)
    icon.alpha = if (muted) .4f else 1f
   }
   icon.setOnClickListener(action)
   (icon.parent as? View)?.setOnClickListener(action)
  }
  root.pics("ic_volume").forEach { icon ->
   val action = View.OnClickListener {
    speaker = !speaker
    engine?.setEnableSpeakerphone(speaker)
    icon.alpha = if (speaker) 1f else .4f
   }
   icon.setOnClickListener(action)
   (icon.parent as? View)?.setOnClickListener(action)
  }
  watch("calls/$callId") { value ->
   call = value
   if (value.isEmpty() || value.s("status") in listOf("ended", "declined", "missed")) {
    toast(value.s("status").ifEmpty { "Call ended" })
    finish()
    return@watch
   }
   val other = if (value.s("caller") == Backend.uid) value.s("callee") else value.s("caller")
   once("users/$other") { user ->
    root.set("Ammi", user.s("name"))
    root.set("AM", initials(user.s("name")))
   }
   if (value.s("status") == "active") {
    if (!joining) {
     joining = true
     permissions.launch(if (value.b("video")) arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
     else arrayOf(Manifest.permission.RECORD_AUDIO))
    }
   } else if (value.s("callee") == Backend.uid && !offered) {
    offered = true
    choices("Incoming ${if (value.b("video")) "video" else "voice"} call", listOf("Answer", "Decline")) { index ->
     api("callAction", mapOf("callId" to callId, "operation" to if (index == 0) "accept" else "declined"))
    }
   } else if (value.s("caller") == Backend.uid) root.set("03:12", "Ringing…")
  }
  timer.post(tick)
 }

 private fun dp(value: Int) = (value * resources.displayMetrics.density + .5f).toInt()

 private fun prepareVideoLayout() {
  if (remoteBox != null) return
  val content = findViewById<ViewGroup>(android.R.id.content)
  val voice = content.getChildAt(0) as LinearLayout
  val security = voice.getChildAt(0)
  val nameRow = root.label("Ammi")!!.parent as View
  val status = root.label("03:12")!!
  val controls = findViewById<View>(R.id.btnEndCall).parent as View
  listOf(security, nameRow, status, controls).forEach { (it.parent as ViewGroup).removeView(it) }
  val frame = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
  val remote = FrameLayout(this)
  remoteBox = remote
  frame.addView(remote, FrameLayout.LayoutParams(-1, -1))
  val info = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
   gravity = Gravity.CENTER_HORIZONTAL
   setPadding(dp(8), dp(12), dp(8), dp(12))
   setBackgroundColor(0x990B5F63.toInt())
  }
  info.addView(security, LinearLayout.LayoutParams(-2, -2))
  info.addView(nameRow, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(6) })
  info.addView(status, LinearLayout.LayoutParams(-2, -2))
  frame.addView(info, FrameLayout.LayoutParams(-1, -2, Gravity.TOP))
  val local = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
  localBox = local
  frame.addView(local, FrameLayout.LayoutParams(dp(96), dp(144), Gravity.TOP or Gravity.END).apply {
   topMargin = dp(144); rightMargin = dp(12)
  })
  val bottom = FrameLayout(this).apply {
   setPadding(dp(8), dp(12), dp(8), dp(20))
   setBackgroundColor(0xCC0B5F63.toInt())
  }
  bottom.addView(controls, FrameLayout.LayoutParams(-2, -2, Gravity.CENTER))
  frame.addView(bottom, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
  content.removeView(voice)
  content.addView(frame, ViewGroup.LayoutParams(-1, -1))
 }

 private fun join() {
  api("callToken", mapOf("callId" to callId)) { token ->
   if (isFinishing || isDestroyed || call.s("status") != "active") return@api
   try {
    if (call.b("video")) prepareVideoLayout()
    val events = object : IRtcEngineEventHandler() {
     override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
      runOnUiThread { joinedAt = Backend.now }
     }
     override fun onUserJoined(uid: Int, elapsed: Int) {
      runOnUiThread {
       if (!isFinishing && call.b("video")) {
        val surface = SurfaceView(this@VoiceCallActivity)
        remoteBox?.removeAllViews()
        remoteBox?.addView(surface, FrameLayout.LayoutParams(-1, -1))
        engine?.setupRemoteVideo(VideoCanvas(surface, VideoCanvas.RENDER_MODE_HIDDEN, uid))
       }
      }
     }
     override fun onTokenPrivilegeWillExpire(token: String?) {
      runOnUiThread { if (!isFinishing) api("callToken", mapOf("callId" to callId)) { engine?.renewToken(it.s("token")) } }
     }
     override fun onError(err: Int) { runOnUiThread { if (!isFinishing) toast("Agora error $err") } }
    }
    engine = RtcEngine.create(RtcEngineConfig().apply {
     mContext = this@VoiceCallActivity; mAppId = token.s("appId"); mEventHandler = events
    })
    val encryption = EncryptionConfig().apply {
     encryptionMode = EncryptionConfig.EncryptionMode.AES_128_GCM2
     encryptionKey = token.s("encryptionKey")
     val salt = android.util.Base64.decode(token.s("salt"), android.util.Base64.DEFAULT)
     System.arraycopy(salt, 0, encryptionKdfSalt, 0, 32)
    }
    val encrypted = engine!!.enableEncryption(true, encryption)
    if (encrypted != 0) error("Encryption failed: $encrypted")
    engine?.enableAudio()
    engine?.setEnableSpeakerphone(speaker)
    engine?.muteLocalAudioStream(muted)
    if (call.b("video")) {
     engine?.enableVideo()
     val local = SurfaceView(this).apply { setZOrderMediaOverlay(true) }
     localBox?.addView(local, FrameLayout.LayoutParams(-1, -1))
     engine?.setupLocalVideo(VideoCanvas(local, VideoCanvas.RENDER_MODE_HIDDEN, token.n("uid").toInt()))
     engine?.startPreview()
    }
    val options = ChannelMediaOptions().apply {
     channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
     clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
     publishMicrophoneTrack = true; publishCameraTrack = call.b("video")
     autoSubscribeAudio = true; autoSubscribeVideo = call.b("video")
    }
    val result = engine!!.joinChannel(token.s("token"), token.s("channel"), token.n("uid").toInt(), options)
    if (result != 0) error("Join failed: $result")
   } catch (e: Exception) { toast(e.localizedMessage ?: "Call failed"); end() }
  }
 }

 private fun end() {
  if (ending) return
  if (callId.isEmpty()) { finish(); return }
  ending = true
  Backend.api("callAction", mapOf("callId" to callId, "operation" to "ended")) { _, error ->
   ending = false
   if (error == null) finish() else toast(error)
  }
 }
 override fun onDestroy() {
  timer.removeCallbacks(tick)
  engine?.leaveChannel()
  if (engine != null) { engine = null; Thread { RtcEngine.destroy() }.start() }
  super.onDestroy()
 }
}
