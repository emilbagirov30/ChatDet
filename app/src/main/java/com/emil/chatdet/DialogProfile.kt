package com.emil.chatdet

import android.app.Dialog
import android.content.Context
import android.widget.ImageView
import android.widget.TextView

class DialogProfile(context: Context) : Dialog(context,R.style.RoundDialog) {
    init {
        setContentView(R.layout.dialog_user_profile)
        val avatar = findViewById<ImageView>(R.id.iv_avatar)
        val username = findViewById<TextView>(R.id.tv_username)
    }

}