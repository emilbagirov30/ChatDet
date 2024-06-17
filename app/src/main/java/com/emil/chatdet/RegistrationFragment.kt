package com.emil.chatdet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Base64


class RegistrationFragment : Fragment() {
    private lateinit var avatar: ImageView
    private var photoIsAvaiable: Boolean = false
    private var currentBitmap:Bitmap? = null
    private lateinit var usernameET:EditText
    private lateinit var  passwordET:EditText
    private lateinit var codeWordET:EditText
    private lateinit var register:Button
    var isShown = false
    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registration, container, false)
        val back = view.findViewById<Button>(R.id.bt_back)
        usernameET = view.findViewById(R.id.et_username)
        passwordET = view.findViewById(R.id.et_password)
        val error = view.findViewById<TextView>(R.id.tv_error)
        codeWordET = view.findViewById(R.id.et_code_word)
        register = view.findViewById(R.id.bt_register)
        val eyeShow = view.findViewById<ImageView>(R.id.iv_eye_show)
        val eyeHide = view.findViewById<ImageView>(R.id.iv_eye_hide)
        val loading = view.findViewById<ProgressBar>(R.id.pb_loading)
        val hintShow = view.findViewById<ImageView>(R.id.iv_hint_show)
        avatar = view.findViewById(R.id.iv_avatar)
        val sharedPreferences = requireContext().getSharedPreferences("Ip", Context.MODE_PRIVATE)
        val ip = sharedPreferences.getString("Ip", ChatDetPrivateKey.IP)
        avatar.setOnClickListener { openGallery() }
        val typePassword = passwordET.inputType
        val typeNormal = codeWordET.inputType
        eyeShow.setOnClickListener {
            val cursorPosition = passwordET.selectionStart
            passwordET.inputType = typeNormal
            isShown = true
            eyeShow.visibility = View.GONE
            eyeHide.visibility = View.VISIBLE
            passwordET.setSelection(cursorPosition)
            }
        eyeHide.setOnClickListener {
            val cursorPosition = passwordET.selectionStart
            passwordET.inputType = typePassword
            isShown = false
            eyeHide.visibility = View.GONE
            eyeShow.visibility = View.VISIBLE
            passwordET.setSelection(cursorPosition)
        }
        hintShow.setOnClickListener {
            val popupView = layoutInflater.inflate(R.layout.tooltip, null)
            val hint = popupView.findViewById<TextView>(R.id.tv_hint_message)
            hint.text = "Потребуется для восстановления пароля"
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val popupWindow = PopupWindow(popupView, width, height, true)
            popupWindow.showAsDropDown(hintShow, 0, -hintShow.height-65)
            popupView.setOnClickListener { popupWindow.dismiss() }
        }


        usernameET.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEditText ()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        codeWordET.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEditText ()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

       passwordET.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEditText ()
                if (!s.isNullOrEmpty()) {
                    if (isShown) {
                        eyeShow.visibility = View.GONE
                        eyeHide.visibility = View.VISIBLE
                    }else{
                        eyeHide.visibility = View.GONE
                        eyeShow.visibility = View.VISIBLE
                    }
                }else{
                    eyeShow.visibility = View.GONE
                    eyeHide.visibility = View.GONE
                    isShown = false
                    passwordET.inputType = typePassword
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        back.setOnClickListener {
            val logInFragment = LogInFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, logInFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        register.setOnClickListener {
            loading.visibility = View.VISIBLE
            val username = usernameET.text.toString().trim()
            val password = passwordET.text.toString().trim()
            val codeWord =codeWordET.text.toString().trim()
            error.visibility = View.GONE
            usernameET.background.setColorFilter(ContextCompat.getColor(requireContext(), R.color.edit_text_default_color), PorterDuff.Mode.SRC_ATOP)
            lifecycleScope.launch(Dispatchers.IO) {
                try{
                val connectToSQLServer = ServerConnection (ip,ChatDetPrivateKey.SERVER_SQL_PORT)
                if(photoIsAvaiable)
                    connectToSQLServer.sendText(ChatDetPrivateKey.REGISTRATION_APP_KEY_AVATAR)
                else
                    connectToSQLServer.sendText(ChatDetPrivateKey.REGISTRATION_APP_KEY)
                connectToSQLServer.sendText(username)
                val response = connectToSQLServer.receiveText()
                if (response == ChatDetPrivateKey.USER_NOT_EXIST) {
                    connectToSQLServer.sendText(password)
                    connectToSQLServer.sendText(codeWord)
                    if (photoIsAvaiable) {
                        val stream = ByteArrayOutputStream()
                        currentBitmap?.compress(Bitmap.CompressFormat.JPEG, 35, stream)
                        val byteArray = stream.toByteArray()
                        connectToSQLServer.sendText( Base64.getEncoder().encodeToString(byteArray))
                        photoIsAvaiable = false
                    }
                        activity?.runOnUiThread { avatar.setImageResource(R.drawable.selection_avatar)
                            usernameET.setText("")
                            passwordET.setText("")
                            codeWordET.setText("")
                            Toast.makeText(requireContext(),"Вы успешно зарегестрированы",Toast.LENGTH_SHORT).show()
                        }
                    isShown = false

                }else if (response == ChatDetPrivateKey.USER_EXIST){
                    usernameET.background.setColorFilter(ContextCompat.getColor(requireContext(), R.color.edit_text_error), PorterDuff.Mode.SRC_ATOP)
                    activity?.runOnUiThread { error.visibility = View.VISIBLE}
                }
                    connectToSQLServer.close()
            }catch (e:IOException){
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(),"Ошибка подключения к серверу",Toast.LENGTH_SHORT).show()
                    }
            }finally {
                    activity?.runOnUiThread {   loading.visibility = View.GONE}

            }

            }

        }
        return view
    }

    private fun checkEditText (){
        val username = usernameET.text.toString().trim()
        val password = passwordET.text.toString().trim()
        val codeWord = codeWordET.text.toString().trim()
        if (username.isNotEmpty() && password.isNotEmpty() && codeWord.isNotEmpty()) {
            register.isEnabled = true
        }else
            register.isEnabled = false
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1) {
            val uri: Uri? = data?.data
            uri?.let {
                val bitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
                val openFileDescriptor = requireContext().contentResolver.openFileDescriptor(uri, "r")
                val exifInterface = openFileDescriptor?.fileDescriptor?.let { ExifInterface(it) }
                val orientation = exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

                val rotatedBitmap: Bitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> Avatar.rotateImage(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> Avatar.rotateImage(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 ->Avatar.rotateImage(bitmap, 270f)
                    else -> bitmap
                }
         currentBitmap = rotatedBitmap
                val drawable = BitmapDrawable(resources, Avatar.getRoundedBitmap(rotatedBitmap))
                avatar.setImageDrawable(drawable)
                photoIsAvaiable = true
            }
        }
    }


}