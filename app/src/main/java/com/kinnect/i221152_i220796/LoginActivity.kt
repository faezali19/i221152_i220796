package com.kinnect.i221152_i220796

import android.os.Bundle
import android.widget.*
import android.content.Intent
class LoginActivity:LiveActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_login)
 val prefs=getSharedPreferences("kinnect",MODE_PRIVATE);val last=prefs.getString("email","").orEmpty();root.set("Jacob West",if(last.isEmpty())"Recent login" else last);root.set("JW",if(last.isEmpty())"K" else last.take(2).uppercase());root.set("2","")
 click(R.id.recentLoginCard){findViewById<EditText>(R.id.etEmail).setText(last)}
 click(R.id.btnCreateAccount){go(SignUpActivity::class.java)}
 click(R.id.btnLogin){val email=text(R.id.etEmail);val password=text(R.id.etPassword);if(email.isEmpty()||password.isEmpty()){toast("Enter email and password");return@click};findViewById<android.view.View>(R.id.btnLogin).isEnabled=false
 Backend.auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{findViewById<android.view.View>(R.id.btnLogin).isEnabled=true;if(!it.isSuccessful)toast(it.exception?.localizedMessage?:"Login failed")else{prefs.edit().putString("email",email).apply();once("users/${Backend.uid}"){u->startActivity(Intent(this,if(u.b("setupComplete"))HomeActivity::class.java else EditProfileActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK));finish()}}}}
 tap("Forgot password?"){ask("Account email",text(R.id.etEmail)){email->Backend.auth.sendPasswordResetEmail(email).addOnCompleteListener{toast(if(it.isSuccessful)"Password reset email requested" else it.exception?.localizedMessage?:"Request failed")}}}
}}
