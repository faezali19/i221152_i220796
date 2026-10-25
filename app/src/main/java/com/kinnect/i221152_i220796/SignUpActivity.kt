package com.kinnect.i221152_i220796

import android.os.Bundle
import android.widget.*
import android.text.*
import android.app.DatePickerDialog
class SignUpActivity:LiveActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_sign_up);back();var birthday="2004-03-14";var gender="Male"
 fun date(){DatePickerDialog(this,{_,y,m,d->birthday="%04d-%02d-%02d".format(y,m+1,d);root.set("14",d.toString());root.set("Mar",(m+1).toString());root.set("2004",y.toString())},2004,2,14).show()}
 listOf("14","Mar","2004").forEach{tap(it){date()}};listOf("Female","Male","Custom").forEach{g->tap(g){gender=g;listOf("Female","Male","Custom").forEach{t->val row=root.label(t)?.parent as? android.view.ViewGroup;row?.all()?.filterIsInstance<RadioButton>()?.forEach{it.isChecked=t==g};row?.setBackgroundResource(if(t==g)R.drawable.bg_input_selected else R.drawable.bg_input)}}}
 findViewById<EditText>(R.id.signupPassword).addTextChangedListener(object:TextWatcher{override fun beforeTextChanged(s:CharSequence?,a:Int,c:Int,f:Int){};override fun onTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){val p=s.toString();val score=listOf(p.length>=8,p.any{it.isUpperCase()},p.any{it.isDigit()},p.any{!it.isLetterOrDigit()}).count{it};root.set("Medium",if(score<2)"Weak" else if(score<4)"Medium" else "Strong");val row=root.label("Medium")?.parent as? android.view.ViewGroup;val color=if(score<2)0xffc53948.toInt() else if(score<4)0xffbd8100.toInt() else 0xff0b5f63.toInt();row?.let{r->for(i in 0..2)r.getChildAt(i).backgroundTintList=android.content.res.ColorStateList.valueOf(if(i<(score*3/4).coerceAtLeast(1))color else 0xffdddddd.toInt())}};override fun afterTextChanged(e:Editable?){} })
 click(R.id.btnCreateAccount){val name=(text(R.id.signupFirst)+" "+text(R.id.signupLast)).trim();val email=text(R.id.signupEmail);val pw=text(R.id.signupPassword);if(name.isEmpty()||!findViewById<CheckBox>(R.id.signupTerms).isChecked){toast("Enter your name and accept the terms");return@click};Backend.auth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener{t->if(!t.isSuccessful)toast(t.exception?.localizedMessage?:"Signup failed")else api("register",mapOf("name" to name,"birthday" to birthday,"gender" to gender)){go(EditProfileActivity::class.java);finish()}}}
 tap("Log in"){finish()}
}}
