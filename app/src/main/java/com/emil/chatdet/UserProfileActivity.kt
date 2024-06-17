package com.emil.chatdet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import java.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException

class UserProfileActivity : AppCompatActivity() {
    private lateinit var  save:Button
    private var photoIsAvaiable: Boolean = false
    private var currentBitmap:Bitmap? = null
    private lateinit var avatar: ImageView
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        val ipPref = getSharedPreferences("Ip", Context.MODE_PRIVATE)
        val dataPref = getSharedPreferences("DataChatDet", Context.MODE_PRIVATE)
        val id = dataPref.getString("id", "0")
        val ip = ipPref.getString("Ip", ChatDetPrivateKey.IP)
        val loadingData = findViewById<ProgressBar>(R.id.pb_loading)
        val loadingUpdate = findViewById<ProgressBar>(R.id.pb_loading_update)
        val usernameET = findViewById<EditText>(R.id.et_username)
        val passwordET =findViewById<EditText>(R.id.et_password)
        save =findViewById(R.id.bt_save)
        val back =findViewById<Button>(R.id.bt_back)
        val codeWordET = findViewById<EditText>(R.id.et_code_word)
         avatar = findViewById(R.id.iv_user_avatar)
        val select = findViewById<ImageView>(R.id.iv_select)
        val changeUsername = findViewById<ImageButton>(R.id.change_username)
        val changePassword = findViewById<ImageButton>(R.id.change_password)
        val changeCodeWord = findViewById<ImageButton>(R.id.change_code_word)

        back.setOnClickListener {
            val switchingToChatActivity = Intent(this@UserProfileActivity, ChatActivity::class.java)
            startActivity(switchingToChatActivity)
            finish()
        }
        changeUsername.setOnClickListener { changeData(usernameET,it) }
        changePassword.setOnClickListener { changeData(passwordET,it) }
        changeCodeWord.setOnClickListener { changeData(codeWordET,it) }

        select.setOnClickListener { openGallery() }

        save.setOnClickListener {
            loadingUpdate.visibility = View.VISIBLE
           val username = usernameET.text.toString().trim()
            val password = passwordET.text.toString().trim()
            val codeWord = codeWordET.text.toString().trim()

            if (username.length>0&&password.length>0&&codeWord.length>0){
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val connectToSQLServer = ServerConnection(ip, ChatDetPrivateKey.SERVER_SQL_PORT)
                    if (photoIsAvaiable){
                    connectToSQLServer.sendText(ChatDetPrivateKey.CHANGE_USER_DATA)
                    val stream = ByteArrayOutputStream()
                    currentBitmap?.compress(Bitmap.CompressFormat.JPEG, 35, stream)
                    val byteArray = stream.toByteArray()
                    connectToSQLServer.sendText( Base64.getEncoder().encodeToString(byteArray))
                    photoIsAvaiable = false
}else{ connectToSQLServer.sendText(ChatDetPrivateKey.CHANGE_USER_DATA_NA) }
                        connectToSQLServer.sendText(id)
                        connectToSQLServer.sendText(username)
                        connectToSQLServer.sendText(password)
                        connectToSQLServer.sendText(codeWord)
                        val response = connectToSQLServer.receiveText()
                        if (response == ChatDetPrivateKey.CHANGE_DATA_SUCCESSFUL){
                            runOnUiThread { recreate() }
                        }else if (response == ChatDetPrivateKey.CHANGE_DATA_ERROR){
                            runOnUiThread { Toast.makeText(this@UserProfileActivity, "Возникла ошибка", Toast.LENGTH_SHORT).show() }
                        }
                    } catch (e: IOException) {
                        runOnUiThread { Toast.makeText(this@UserProfileActivity, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
                        }
                    }finally { runOnUiThread { loadingUpdate.visibility = View.GONE } }}
            }else{
                Toast.makeText(this@UserProfileActivity,"Ошибка: Пустое поле",Toast.LENGTH_SHORT).show()
            }
        }

        val infoLl = findViewById<LinearLayout>(R.id.ll_info)
        infoLl.visibility = View.GONE
        loadingData.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val connectToSQLServer = ServerConnection(ip, ChatDetPrivateKey.SERVER_SQL_PORT)
                connectToSQLServer.sendText(ChatDetPrivateKey.GET_ALL_USER_DATA)
                connectToSQLServer.sendText(id)
                val username = connectToSQLServer.receiveText()
                val password = connectToSQLServer.receiveText()
                val codeWord = connectToSQLServer.receiveText()
                runOnUiThread {
                    usernameET.setText(username)
                    passwordET.setText(password)
                    codeWordET.setText(codeWord)
                }
                val avatarString = connectToSQLServer.receiveText()
                if (avatarString!="NULL") {
                    val imageBytes = Base64.getDecoder().decode(avatarString)
                    val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if(imageBitmap != null) {
                        runOnUiThread {
                            avatar.setImageBitmap(Avatar.getRoundedBitmap(imageBitmap))
                            avatar.alpha=0.5f
                            select.alpha=0.8f
                        }
                    }

                }
            }catch (e: IOException){
               runOnUiThread {
                    Toast.makeText(this@UserProfileActivity,"Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
                }
            }finally {
               runOnUiThread {
                   loadingData.visibility = View.GONE
                   infoLl.visibility = View.VISIBLE
               }
            }
        }
    }

    private fun changeData (et:EditText,bt: View){
        et.isEnabled = true
        et.setSelection(et.text.length)
        et.requestFocus()
        bt.visibility = View.GONE
        save.isEnabled = true
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, RegistrationFragment.PICK_IMAGE_REQUEST)
    }
    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RegistrationFragment.PICK_IMAGE_REQUEST && resultCode == -1) {
            val uri: Uri? = data?.data
            uri?.let {
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                val openFileDescriptor =contentResolver.openFileDescriptor(uri, "r")
                val exifInterface = openFileDescriptor?.fileDescriptor?.let { ExifInterface(it) }
                val orientation = exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

                val rotatedBitmap: Bitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> Avatar.rotateImage(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> Avatar.rotateImage(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> Avatar.rotateImage(bitmap, 270f)
                    else -> bitmap
                }
                currentBitmap = rotatedBitmap
                val drawable = BitmapDrawable(resources, Avatar.getRoundedBitmap(rotatedBitmap))
                avatar.setImageDrawable(drawable)
                avatar.alpha=0.75f
                save.isEnabled = true
                photoIsAvaiable = true
            }
        }
    }



}