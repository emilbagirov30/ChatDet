package com.emil.chatdet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.widget.ImageView
import android.widget.TextView


@SuppressLint("MissingInflatedId")
    class DialogError(context: Context) : Dialog(context,R.style.RoundDialog) {
        init {
            setContentView(R.layout.dialog_error_connection)
        }
}