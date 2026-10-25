package com.kinnect.i221152_i220796

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.database.*

fun View.all():List<View> = listOf(this)+if(this is ViewGroup)(0 until childCount).flatMap{getChildAt(it).all()} else emptyList()
fun View.labels(text:String)=all().filterIsInstance<TextView>().filter{it.tag=="t:$text"}
fun View.label(text:String)=labels(text).firstOrNull()
fun View.pics(name:String)=all().filterIsInstance<ImageView>().filter{it.tag=="i:$name"}
fun View.set(text:String,value:String){labels(text).forEach{it.text=value}}
fun initials(name:String)=name.split(' ').filter{it.isNotEmpty()}.take(2).joinToString(""){it.take(1).uppercase()}
fun age(time:Long):String {val minutes=((Backend.now-time)/60000).coerceAtLeast(0);return if(minutes<1)"now" else if(minutes<60)"${minutes}m" else if(minutes<1440)"${minutes/60}h" else "${minutes/1440}d"}
fun ImageView.photo(url:String){ if(url.isEmpty())setImageResource(R.drawable.ph_scene) else Glide.with(this).load(url).placeholder(R.drawable.ph_scene).into(this) }
open class LiveActivity:AppCompatActivity(){
 val root:View get()=findViewById(android.R.id.content)
 private val listeners=mutableMapOf<String,Pair<Query,ValueEventListener>>()
 private var incomingDialog:AlertDialog?=null
 private var offered=""
 override fun onCreate(state:Bundle?){super.onCreate(state)}
 override fun onStart(){super.onStart();if(Backend.uid.isNotEmpty()&&this !is VoiceCallActivity)watch("incoming/${Backend.uid}","incoming") { data->
  val c=data.rows().firstOrNull{it.s("status")=="ringing"&&Backend.now-it.n("createdAt")<60000}
  if(c==null){incomingDialog?.dismiss();offered=""}else if(offered!=c.s("id")){offered=c.s("id");incomingDialog=AlertDialog.Builder(this).setTitle(if(c.b("video"))"Incoming video call" else "Incoming voice call").setMessage("Answer this Kinnect call?").setPositiveButton("Answer"){_,_->api("callAction",mapOf("callId" to c.s("id"),"operation" to "accept")){go(VoiceCallActivity::class.java,"callId" to c.s("id"))}}.setNegativeButton("Decline"){_,_->api("callAction",mapOf("callId" to c.s("id"),"operation" to "declined")) {}}.setCancelable(false).show()}
 }}
 override fun onDestroy(){listeners.values.forEach{it.first.removeEventListener(it.second)};listeners.clear();incomingDialog?.dismiss();super.onDestroy()}
 fun watch(path:String,key:String=path,done:(Record)->Unit)=watch(Backend.db.getReference(path),key,done)
 fun watch(query:Query,key:String,done:(Record)->Unit){listeners.remove(key)?.let{it.first.removeEventListener(it.second)};val l=object:ValueEventListener{override fun onDataChange(s:DataSnapshot){done(s.value.record())};override fun onCancelled(e:DatabaseError){toast(e.message)}};listeners[key]=query to l;query.addValueEventListener(l)}
 fun once(path:String,done:(Record)->Unit){Backend.db.getReference(path).get().addOnSuccessListener{done(it.value.record())}.addOnFailureListener{toast(it.localizedMessage?:"Read failed")}}
 fun api(action:String,data:Record=emptyMap(),done:(Record)->Unit={})=Backend.api(action,data){r,e->if(!isDestroyed){if(e!=null)toast(e)else done(r?:emptyMap())}}
 fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
 fun go(c:Class<*>,vararg extras:Pair<String,String>){startActivity(Intent(this,c).apply{extras.forEach{putExtra(it.first,it.second)}})}
 fun click(id:Int,done:()->Unit){findViewById<View>(id)?.setOnClickListener{done()}}
 fun tap(text:String,done:()->Unit){root.labels(text).forEach{it.setOnClickListener{done()}}}
 fun text(id:Int)=findViewById<TextView>(id)?.text?.toString()?.trim().orEmpty()
 fun back(){click(R.id.btnBack){finish()};click(R.id.btnClose){finish()}}
 fun ask(title:String,initial:String="",done:(String)->Unit){val input=EditText(this).apply{setText(initial);setPadding(32,16,32,16)};AlertDialog.Builder(this).setTitle(title).setView(input).setPositiveButton("Save"){_,_->done(input.text.toString())}.setNegativeButton("Cancel",null).show()}
 fun choices(title:String,items:List<String>,done:(Int)->Unit){AlertDialog.Builder(this).setTitle(title).setItems(items.toTypedArray()){_,i->done(i)}.show()}
 fun list(id:Int,layout:Int,rows:List<Record>,columns:Int=1,bind:(View,Record)->Unit){val r=findViewById<RecyclerView>(id)?:return;r.isNestedScrollingEnabled=false;r.layoutManager=if(columns>1)GridLayoutManager(this,columns)else LinearLayoutManager(this);r.adapter=object:RecyclerView.Adapter<RowHolder>(){override fun onCreateViewHolder(p:ViewGroup,t:Int)=RowHolder(layoutInflater.inflate(layout,p,false));override fun getItemCount()=rows.size;override fun onBindViewHolder(h:RowHolder,i:Int)=bind(h.itemView,rows[i])}}
 fun profile(uid:String){go(if(uid==Backend.uid)ProfileActivity::class.java else OtherProfileActivity::class.java,"uid" to uid)}
 fun openChat(other:String,postId:String=""){api("openChat",mapOf("other" to other)){r->if(postId.isEmpty())go(ChatActivity::class.java,"chatId" to r.s("id"),"other" to other)else api("message",mapOf("chatId" to r.s("id"),"postId" to postId)){toast("Post shared")}}}
 fun chooseFriend(done:(String)->Unit){once("friends/${Backend.uid}"){f->once("users"){u->val ids=f.keys.toList();if(ids.isEmpty())toast("Add a friend first")else choices("Choose friend",ids.map{u.obj(it).s("name")}){done(ids[it])}}}}
 fun logout(){val leaving=Backend.uid;com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener{t->if(leaving.isNotEmpty())Backend.db.getReference("tokens/$leaving/$t").removeValue()};KinnectApp.offline();Backend.auth.signOut();startActivity(Intent(this,LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK));finish()}
}
class RowHolder(v:View):RecyclerView.ViewHolder(v)
