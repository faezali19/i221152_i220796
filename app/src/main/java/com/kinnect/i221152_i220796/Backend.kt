package com.kinnect.i221152_i220796

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

typealias Record = Map<String, Any?>
@Suppress("UNCHECKED_CAST") fun Any?.record(): Record = this as? Record ?: emptyMap()
fun Record.s(k:String)=this[k]?.toString()?:""
fun Record.n(k:String)=(this[k] as? Number)?.toLong()?:0L
fun Record.b(k:String)=this[k]==true
fun Record.obj(k:String)=this[k].record()
fun Record.rows()=entries.map { (k,v)->v.record()+mapOf("id" to k) }
object Backend {
 val auth=FirebaseAuth.getInstance()
 val db=FirebaseDatabase.getInstance()
 val uid get()=auth.currentUser?.uid?:""
 var offset=0L
 val now get()=System.currentTimeMillis()+offset
 private val main=android.os.Handler(android.os.Looper.getMainLooper())
 private val workers=java.util.concurrent.Executors.newFixedThreadPool(3)
 private val base="http://127.0.0.1:8080"
 private fun unpack(value:Any?):Any?=when(value){
  is org.json.JSONObject->value.keys().asSequence().associateWith{unpack(value.get(it))}
  is org.json.JSONArray->(0 until value.length()).map{unpack(value.get(it))}
  org.json.JSONObject.NULL->null
  else->value
 }
 private fun request(route:String,type:String,body:(java.io.OutputStream)->Unit,done:(Record?,String?)->Unit){
  val user=auth.currentUser
  if(user==null){done(null,"Please log in");return}
  user.getIdToken(false).addOnSuccessListener{token->workers.execute{
   var connection:java.net.HttpURLConnection?=null
   try{
    connection=java.net.URL(base+route).openConnection() as java.net.HttpURLConnection
    val c=connection
    c.requestMethod="POST";c.connectTimeout=15000;c.readTimeout=60000;c.doOutput=true
    c.setRequestProperty("Authorization","Bearer "+token.token)
    c.setRequestProperty("Content-Type",type);c.setChunkedStreamingMode(65536)
    c.outputStream.use(body)
    val status=c.responseCode
    val response=(if(status in 200..299)c.inputStream else c.errorStream)?.bufferedReader()?.use{it.readText()}?:"{}"
    val obj=org.json.JSONObject(response)
    if(status !in 200..299)throw java.io.IOException(obj.optString("error","Server error $status"))
    val data=unpack(obj).record();main.post{done(data,null)}
   }catch(e:Exception){main.post{done(null,e.localizedMessage?:"Local backend unavailable. Run menus 12 and 13.")}}
   finally{connection?.disconnect()}
  }}.addOnFailureListener{done(null,it.localizedMessage)}
 }
 fun api(action:String,data:Record=emptyMap(),done:(Record?,String?)->Unit){
  val bytes=org.json.JSONObject(data+mapOf("action" to action)).toString().toByteArray(Charsets.UTF_8)
  request("/api","application/json",{it.write(bytes)},done)
 }
 fun upload(uri:Uri,type:String,done:(Record?,String?)->Unit){
  val context=auth.app.applicationContext
  request("/upload",type,{out->context.contentResolver.openInputStream(uri)?.use{it.copyTo(out)}?:throw java.io.IOException("Cannot open selected media")},done)
 }
}
