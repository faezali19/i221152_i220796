package com.kinnect.i221152_i220796

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import java.util.Calendar
import java.util.Locale

class SignUpActivity : LiveActivity() {

 private var birthYear = 2004
 private var birthMonth = 2 // Calendar months start at zero.
 private var birthDay = 14
 private var gender = "Male"
 private var dateDialog: DatePickerDialog? = null

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_sign_up)
  back()

  birthYear = savedInstanceState?.getInt("birthYear", 2004) ?: 2004
  birthMonth = savedInstanceState?.getInt("birthMonth", 2) ?: 2
  birthDay = savedInstanceState?.getInt("birthDay", 14) ?: 14
  gender = savedInstanceState?.getString("gender") ?: "Male"

  val password = findViewById<EditText>(R.id.signupPassword)
  enablePasswordEye(password)

  findViewById<CheckBox>(R.id.signupTerms).apply {
   isClickable = true
   isFocusable = true
  }

  // Make each entire birthday box clickable, including its arrow.
  listOf("14", "Mar", "2004").forEach { originalLabel ->
   val value = root.label(originalLabel)
   val innerColumn = value?.parent as? ViewGroup
   val card = innerColumn?.parent as? ViewGroup

   if (card != null) {
    makeWholeCardClickable(card) {
     showBirthdayPicker()
    }
   }
  }
  updateBirthdayLabels()

  // Make each entire gender box clickable, including its radio circle.
  listOf("Female", "Male", "Custom").forEach { option ->
   val card = root.label(option)?.parent as? ViewGroup

   if (card != null) {
    makeWholeCardClickable(card) {
     gender = option
     updateGenderSelection()
    }
   }
  }
  updateGenderSelection()

  password.addTextChangedListener(object : TextWatcher {
   override fun beforeTextChanged(
    s: CharSequence?,
    start: Int,
    count: Int,
    after: Int
   ) = Unit

   override fun onTextChanged(
    s: CharSequence?,
    start: Int,
    before: Int,
    count: Int
   ) {
    updatePasswordStrength(s.toString())
   }

   override fun afterTextChanged(s: Editable?) = Unit
  })
  updatePasswordStrength(password.text.toString())

  click(R.id.btnCreateAccount) {
   val name =
    (text(R.id.signupFirst) + " " + text(R.id.signupLast)).trim()
   val email = text(R.id.signupEmail)
   val enteredPassword = password.text.toString()
   val acceptedTerms =
    findViewById<CheckBox>(R.id.signupTerms).isChecked

   if (name.isEmpty() || !acceptedTerms) {
    toast("Enter your name and accept the terms")
    return@click
   }

   val birthday = String.format(
    Locale.US,
    "%04d-%02d-%02d",
    birthYear,
    birthMonth + 1,
    birthDay
   )

   Backend.auth.createUserWithEmailAndPassword(email, enteredPassword)
    .addOnCompleteListener { result ->
     if (isFinishing || isDestroyed) {
      return@addOnCompleteListener
     }

     if (!result.isSuccessful) {
      toast(result.exception?.localizedMessage ?: "Signup failed")
     } else {
      api(
       "register",
       mapOf(
        "name" to name,
        "birthday" to birthday,
        "gender" to gender
       )
      ) {
       go(EditProfileActivity::class.java)
       finish()
      }
     }
    }
  }

  tap("Log in") {
   finish()
  }
 }

 private fun makeWholeCardClickable(
  card: ViewGroup,
  action: () -> Unit
 ) {
  // Let taps on the child labels/arrows/radio circles reach the card.
  card.all().filter { it !== card }.forEach { child ->
   child.setOnClickListener(null)
   child.isClickable = false
   child.isLongClickable = false
   child.isFocusable = false
  }

  card.isFocusable = true
  card.setOnClickListener {
   action()
  }
 }

 private fun showBirthdayPicker() {
  if (dateDialog?.isShowing == true) return

  val dialog = DatePickerDialog(
   this,
   { _, year, month, day ->
    birthYear = year
    birthMonth = month
    birthDay = day
    updateBirthdayLabels()
   },
   birthYear,
   birthMonth,
   birthDay
  )

  dialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
  dialog.setOnDismissListener {
   dateDialog = null
  }
  dateDialog = dialog
  dialog.show()
 }

 private fun updateBirthdayLabels() {
  root.set("14", birthDay.toString())
  root.set("Mar", (birthMonth + 1).toString())
  root.set("2004", birthYear.toString())
 }

 private fun updateGenderSelection() {
  listOf("Female", "Male", "Custom").forEach { option ->
   val card = root.label(option)?.parent as? ViewGroup
    ?: return@forEach

   val selected = option == gender

   card.setBackgroundResource(
    if (selected) R.drawable.bg_input_selected
    else R.drawable.bg_input
   )

   card.all().filterIsInstance<RadioButton>().forEach {
    it.isChecked = selected
   }
  }
 }

 private fun updatePasswordStrength(password: String) {
  val score = listOf(
   password.length >= 8,
   password.any { it.isUpperCase() },
   password.any { it.isDigit() },
   password.any { !it.isLetterOrDigit() }
  ).count { it }

  root.set(
   "Medium",
   when {
    score < 2 -> "Weak"
    score < 4 -> "Medium"
    else -> "Strong"
   }
  )

  val color = when {
   score < 2 -> 0xffc53948.toInt()
   score < 4 -> 0xffbd8100.toInt()
   else -> 0xff0b5f63.toInt()
  }

  val row = root.label("Medium")?.parent as? ViewGroup ?: return
  val filledBars = (score * 3 / 4).coerceAtLeast(1)

  for (index in 0..2) {
   row.getChildAt(index)?.backgroundTintList =
    ColorStateList.valueOf(
     if (index < filledBars) color else 0xffdddddd.toInt()
    )
  }
 }

 override fun onSaveInstanceState(outState: Bundle) {
  outState.putInt("birthYear", birthYear)
  outState.putInt("birthMonth", birthMonth)
  outState.putInt("birthDay", birthDay)
  outState.putString("gender", gender)
  super.onSaveInstanceState(outState)
 }

 override fun onDestroy() {
  dateDialog?.dismiss()
  dateDialog = null
  super.onDestroy()
 }
}