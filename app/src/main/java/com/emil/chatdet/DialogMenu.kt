package com.emil.chatdet

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView



@SuppressLint("MissingInflatedId")
class DialogMenu(private val activity: Activity) : Dialog(activity, R.style.RoundDialog) {
        init {
            setContentView(R.layout.dialog_menu)

          val hintShow = findViewById<ImageView>(R.id.iv_hint_show)
            val exit = findViewById<Button>(R.id.bt_exit)
            val profile = findViewById<Button>(R.id.bt_profile)
            val dataPerf = context.getSharedPreferences("DataChatDet", Context.MODE_PRIVATE)
            val editor = dataPerf.edit()
            exit.setOnClickListener {
            editor.putBoolean("remember",false)
            editor.apply()
                val switchingToAccountDetailsActivity = Intent( activity, AccountDetailsActivity::class.java)
                activity.startActivity(switchingToAccountDetailsActivity)
                activity.finish()
            }
            profile.setOnClickListener {
                val switchingToProfileActivity = Intent(context, UserProfileActivity::class.java)
                context.startActivity(switchingToProfileActivity)

            }
            hintShow.setOnClickListener {
                val popupView = layoutInflater.inflate(R.layout.tooltip, null)
                val hint = popupView.findViewById<TextView>(R.id.tv_hint_message)
                hint.text = "Код для подключения \n или создания сессии"

                val width = LinearLayout.LayoutParams.WRAP_CONTENT
                val height = LinearLayout.LayoutParams.WRAP_CONTENT

                val popupWindow = PopupWindow(popupView, width, height, true)
                popupWindow.showAsDropDown(hintShow, 0, -hintShow.height-65)

                popupView.setOnClickListener {
                    popupWindow.dismiss()
                }
            }

        }

}