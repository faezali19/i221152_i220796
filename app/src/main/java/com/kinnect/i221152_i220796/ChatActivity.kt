package com.kinnect.i221152_i220796

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.UUID

class ChatActivity : LiveActivity() {
 private var chatId = ""
 private var other = ""
 private var replyId = ""
 private var replyText = ""
 private var rows = emptyList<Record>()
 private var firstSnapshot = true
 private var resumed = false
 private var busy = false
 private var pickerOpen = false
 private var reading = false
 private var readAgain = false
 private var calling = false
 private var openingSharedPost = false
 private var pending: Record? = null
 private var screenshot: ScreenshotMonitor? = null
 private lateinit var input: EditText
 private lateinit var messages: RecyclerView
 private lateinit var manager: LinearLayoutManager
 private lateinit var header: View
 private val messageAdapter = MessageAdapter()

 private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
  pickerOpen = false
  if (uri != null && !busy) {
   setBusy(true, "Uploading photo...")
   Backend.upload(uri, contentResolver.getType(uri) ?: "image/jpeg") { media, error ->
    if (!isFinishing && !isDestroyed) {
     if (error != null || media == null) {
      setBusy(false)
      toast(error ?: "Photo upload failed")
     } else {
      setBusy(false)
      send(media)
     }
    }
   }
  }
 }

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_chat)
  back()
  chatId = intent.getStringExtra("chatId").orEmpty()
  other = intent.getStringExtra("other").orEmpty()
  if (chatId.isEmpty()) {
   toast("Open a conversation from Chats")
   finish()
   return
  }

  // Clear sample content before the first frame is drawn.
  root.set("Aisha Khan", "Conversation")
  root.set("AK", "")
  root.set("Active now", "")
  root.set("You're friends on Kinnect · Lives in Karachi", "Kinnect conversation")
  root.set("TODAY 4:49 PM", "")
  input = findViewById(R.id.messageInput)
  input.hint = "Aa"
  input.setSingleLine(true)
  input.imeOptions = EditorInfo.IME_ACTION_SEND

  // Replace the nested ScrollView/RecyclerView with ONE scrolling list.
  messages = findViewById(R.id.messagesList)
  val column = messages.parent as LinearLayout
  val scroll = column.parent as ScrollView
  val parent = scroll.parent as ViewGroup
  val position = parent.indexOfChild(scroll)
  val parameters = scroll.layoutParams
  header = column.getChildAt(0)
  column.removeView(header)
  column.removeView(messages)
  parent.removeView(scroll)
  parent.addView(messages, position, parameters)
  messages.setPadding(column.paddingLeft, 0, column.paddingRight, 0)
  messages.clipToPadding = false
  manager = LinearLayoutManager(this).apply { stackFromEnd = true }
  messages.layoutManager = manager
  messages.adapter = messageAdapter
  messages.itemAnimator = null
  messages.isNestedScrollingEnabled = true

  if (other.isNotEmpty()) {
   once("users/$other") { user ->
    root.set("Aisha Khan", user.s("name"))
    root.set("AK", initials(user.s("name")))
    header.set("Aisha Khan", user.s("name"))
    header.set("AK", initials(user.s("name")))
   }
   watch("presence/$other") {
    root.set("Active now", when {
     it.b("online") -> "Active now"
     it.n("lastSeen") > 0 -> "Active ${age(it.n("lastSeen"))} ago"
     else -> "Offline"
    })
   }
  }

  watch("messages/$chatId") { data ->
   val incoming = data.rows().sortedWith(
    compareBy<Record> { it.n("createdAt") }.thenBy { it.s("id") }
   )
   val wasAtBottom = !messages.canScrollVertically(1)
   val oldIds = rows.map { it.s("id") }.toSet()
   val newMessages = incoming.filter { it.s("id") !in oldIds }
   val scrollToEnd = firstSnapshot || wasAtBottom ||
           newMessages.any { it.s("ownerId") == Backend.uid }
   firstSnapshot = false
   rows = incoming
   messageAdapter.notifyDataSetChanged()
   if (scrollToEnd) scrollToLatest()
   if (resumed && newMessages.any { it.s("ownerId") != Backend.uid }) markRead()
  }

  root.pics("ic_thumb").forEach { icon ->
   icon.setOnClickListener {
    if (!busy) send()
   }
  }
  listOf("ic_image", "ic_camera", "ic_add_circle").forEach { name ->
   root.pics(name).forEach { icon ->
    icon.setOnClickListener {
     if (!busy && !pickerOpen) {
      pickerOpen = true
      picker.launch("image/*")
     }
    }
   }
  }
  input.setOnEditorActionListener { _, action, event ->
   val enter = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
           event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0
   if (action == EditorInfo.IME_ACTION_SEND || enter) {
    if (!busy && input.text.isNotBlank()) send()
    true
   } else false
  }
  input.setOnLongClickListener {
   if (replyId.isEmpty() || busy) false else {
    choices("Reply", listOf("Cancel reply")) {
     replyId = ""
     replyText = ""
     updateHint()
    }
    true
   }
  }
  click(R.id.btnCall) { call(false) }
  root.pics("ic_video").forEach { it.setOnClickListener { call(true) } }
  screenshot = ScreenshotMonitor(this) {
   api("screenshot", mapOf("chatId" to chatId))
  }
 }

 private fun scrollToLatest() {
  messages.post {
   if (!isDestroyed && messageAdapter.itemCount > 0) {
    messages.scrollToPosition(messageAdapter.itemCount - 1)
   }
  }
 }

 private fun updateHint() {
  input.hint = if (replyId.isEmpty()) "Aa" else "Replying: $replyText"
 }

 private fun setBusy(value: Boolean, label: String = "Sending...") {
  busy = value
  input.isEnabled = !value
  if (value) input.hint = label else updateHint()
  listOf("ic_thumb", "ic_image", "ic_camera", "ic_add_circle").forEach { name ->
   root.pics(name).forEach {
    it.isEnabled = !value
    it.alpha = if (value) 0.45f else 1f
   }
  }
 }

 private fun send(media: Record? = null) {
  if (busy) return
  val draft = input.text.toString()
  val value = if (draft.isBlank() && media == null) "👍" else draft
  val previous = pending
  val request = if (previous != null && previous.s("text") == value &&
   previous.s("replyId") == replyId && (media == null || previous["media"] == media)
  ) previous else mapOf<String, Any?>(
   "chatId" to chatId,
   "text" to value,
   "media" to media,
   "replyId" to replyId,
   "requestId" to UUID.randomUUID().toString()
  )
  pending = request
  submit(request)
 }

 private fun submit(request: Record) {
  if (busy) return
  setBusy(true)
  Backend.api("message", request) { _, error ->
   if (!isFinishing && !isDestroyed) {
    setBusy(false)
    if (error != null) {
     // A timeout can happen after saving. Retry with the SAME ID.
     androidx.appcompat.app.AlertDialog.Builder(this)
      .setTitle("Message not confirmed")
      .setMessage(error)
      .setPositiveButton("Retry") { _, _ -> submit(request) }
      .setNegativeButton("Keep draft") { _, _ -> }
      .show()
    } else {
     pending = null
     input.setText("")
     replyId = ""
     replyText = ""
     updateHint()
     scrollToLatest()
    }
   }
  }
 }

 private fun markRead() {
  if (!resumed || chatId.isEmpty()) return
  if (reading) { readAgain = true; return }
  reading = true
  Backend.api("readChat", mapOf("chatId" to chatId)) { _, _ ->
   reading = false
   if (readAgain) {
    readAgain = false
    if (!isDestroyed) markRead()
   }
  }
 }

 private fun call(video: Boolean) {
  if (calling) return
  calling = true
  Backend.api("startCall", mapOf("chatId" to chatId, "video" to video)) { result, error ->
   calling = false
   if (!isFinishing && !isDestroyed) {
    if (error != null) toast(error)
    else go(VoiceCallActivity::class.java, "callId" to result.orEmpty().s("id"))
   }
  }
 }

 private fun actions(message: Record) {
  if (busy || message.s("kind") == "system" || message.b("deleted")) return
  val options = mutableListOf("Reply", "Heart / unheart")
  if (message.s("ownerId") == Backend.uid &&
   Backend.now - message.n("createdAt") < 300000
  ) {
   options.add("Edit")
   options.add("Delete")
  }
  choices("Message", options) { selection ->
   when (options[selection]) {
    "Reply" -> {
     replyId = message.s("id")
     replyText = message.s("text").ifBlank {
      if (message.obj("sharedPost").isNotEmpty()) "Shared post" else "Photo"
     }
     updateHint()
     input.requestFocus()
    }
    "Heart / unheart" -> api("heart", mapOf("chatId" to chatId, "messageId" to message.s("id")))
    "Edit" -> ask("Edit message", message.s("text")) {
     api("changeMessage", mapOf("chatId" to chatId, "messageId" to message.s("id"), "operation" to "edit", "text" to it))
    }
    "Delete" -> api("changeMessage", mapOf("chatId" to chatId, "messageId" to message.s("id"), "operation" to "delete"))
   }
  }
 }

 private fun openSharedPost(post: Record) {
  if (openingSharedPost || isFinishing || isDestroyed) return
  val postId = post.s("id")
  if (postId.isBlank()) {
   toast("This shared post is unavailable")
   return
  }
  openingSharedPost = true
  toast("Opening post photos...")
  Backend.db.getReference("posts").child(postId).get()
   .addOnSuccessListener { snapshot ->
    openingSharedPost = false
    if (isFinishing || isDestroyed) return@addOnSuccessListener
    if (!snapshot.exists()) {
     toast("This post is no longer available")
     return@addOnSuccessListener
    }
    val urls = snapshot.child("images").children.mapNotNull { item ->
     item.child("url").getValue(String::class.java)?.takeIf { it.isNotBlank() }
    }.distinct()
    if (urls.isEmpty()) toast("This post has no photos")
    else go(MediaViewerActivity::class.java, "urls" to urls.joinToString("\n"))
   }
   .addOnFailureListener {
    openingSharedPost = false
    if (!isFinishing && !isDestroyed) {
     toast("Cannot open this post. Check your connection and access to the author's posts.")
    }
   }
 }

 private inner class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  override fun getItemCount() = rows.size + 1
  override fun getItemViewType(position: Int) = if (position == 0) 0 else 1
  override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
   val view = if (type == 0) header else LayoutInflater.from(parent.context)
    .inflate(R.layout.item_message, parent, false)
   if (type == 0) view.layoutParams = RecyclerView.LayoutParams(-1, -2)
   return object : RecyclerView.ViewHolder(view) {}
  }
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
   if (position == 0) return
   val message = rows[position - 1]
   val view = holder.itemView
   val body = view.findViewById<TextView>(R.id.messageBody)
   val time = view.findViewById<TextView>(R.id.messageTime)
   val image = view.findViewById<ImageView>(R.id.messagePhoto)
   val mine = message.s("ownerId") == Backend.uid
   val system = message.s("kind") == "system"
   (view as LinearLayout).gravity = when {
    system -> Gravity.CENTER
    mine -> Gravity.END
    else -> Gravity.START
   }
   val parts = mutableListOf<String>()
   val reply = message.obj("reply")
   val post = message.obj("sharedPost")
   if (reply.isNotEmpty()) parts.add("↪ ${reply.s("text")}")
   if (message.s("text").isNotBlank()) parts.add(message.s("text"))
   if (post.isNotEmpty()) parts.add("Shared post" + post.s("text").let { if (it.isBlank()) "" else ": $it" })
   body.text = parts.joinToString("\n")
   body.visibility = if (parts.isEmpty()) View.GONE else View.VISIBLE
   body.setBackgroundResource(if (mine) R.drawable.bg_message_out else R.drawable.bg_message_in)
   time.text = "${age(message.n("createdAt"))}" +
           (if (message.b("edited")) " · Edited" else "") +
           (if (message.obj("hearts").isNotEmpty()) " · ❤️ ${message.obj("hearts").size}" else "")
   val url = message.obj("image").s("url").ifEmpty { post.obj("image").s("url") }
   image.visibility = if (url.isEmpty()) View.GONE else View.VISIBLE
   image.setOnClickListener(null)
   body.setOnClickListener(null)
   if (post.isNotEmpty()) {
    body.setOnClickListener { openSharedPost(post) }
   }
   if (url.isNotEmpty()) {
    image.photo(url)
    image.setOnClickListener {
     if (post.isNotEmpty()) openSharedPost(post)
     else go(MediaViewerActivity::class.java, "urls" to url)
    }
   }
   listOf<View>(view, body, image, time).forEach {
    it.setOnLongClickListener { actions(message); true }
   }
  }
 }

 override fun onResume() {
  super.onResume()
  resumed = true
  screenshot?.start()
  markRead()
 }

 override fun onPause() {
  resumed = false
  screenshot?.stop()
  super.onPause()
 }
}
