
package com.emil.chatdet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException


class LogInFragment : Fragment() {
    var isShown = false
    private lateinit var usernameET:EditText
    private lateinit var  passwordET:EditText
    private lateinit var logIn:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log_in, container, false)
        val registration = view.findViewById<Button>(R.id.bt_registration)
        logIn = view.findViewById(R.id.bt_log_in)
        val settings = view.findViewById<ImageButton>(R.id.ib_settings)
        val forgotPassword = view.findViewById<TextView>(R.id.tv_forgot_password)
        val eyeShow = view.findViewById<ImageView>(R.id.iv_eye_show)
        val eyeHide = view.findViewById<ImageView>(R.id.iv_eye_hide)
        val remember = view.findViewById<CheckBox>(R.id.cb_remember)
        val loading = view.findViewById<ProgressBar>(R.id.pb_loading)
       usernameET = view.findViewById(R.id.et_username)
        passwordET = view.findViewById(R.id.et_password)
        val ipPerf = requireContext().getSharedPreferences("Ip", Context.MODE_PRIVATE)
        val dataPerf = requireContext().getSharedPreferences("DataChatDet", Context.MODE_PRIVATE)
        val ip = ipPerf.getString("Ip", ChatDetPrivateKey.IP)
        val isEntered = dataPerf.getBoolean("remember", false)
        if (isEntered) logIn()
        val editor = dataPerf.edit()
        val typePassword = passwordET.inputType
        val typeNormal = usernameET.inputType

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

        logIn.setOnClickListener {
            loading.visibility = View.VISIBLE
            val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            val username = usernameET.text.toString().trim()
            val password = passwordET.text.toString().trim()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val connectToSQLServer = ServerConnection(ip, ChatDetPrivateKey.SERVER_SQL_PORT)
                    connectToSQLServer.sendText(ChatDetPrivateKey.LOG_IN)
                    connectToSQLServer.sendText(username)
                    connectToSQLServer.sendText(password)
                    val response = connectToSQLServer.receiveText()
                    if (response == ChatDetPrivateKey.CORRECT_DATA){
                        val id = connectToSQLServer.receiveText()
                        val isChecked: Boolean = remember.isChecked
                        editor.putBoolean("remember",isChecked)
                        editor.putString("id",id)
                        editor.putString("username",username)
                        editor.apply()
                        logIn()
                    }else{
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(),"Пользователя с такими данными не существует", Toast.LENGTH_SHORT).show()
                        }
                        val vibrator =  requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (vibrator.hasVibrator()) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    }

                }catch (e: IOException){
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(),"Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
                    }
                }finally {
                    activity?.runOnUiThread { loading.visibility = View.GONE}

                }
            }
        }

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
        usernameET.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEditText ()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        registration.setOnClickListener {
            val registrationFragment = RegistrationFragment()
            changeFragment(registrationFragment)
        }
        forgotPassword.setOnClickListener {
            val recoveryFragment = RecoveryPasswordFragment()
            changeFragment(recoveryFragment)
        }
        settings.setOnClickListener {
            val settingsFragment = SettingsFragment()
            changeFragment(settingsFragment)
        }
        return view
    }

    private fun changeFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    private fun checkEditText (){
        val username = usernameET.text.toString().trim()
        val password = passwordET.text.toString().trim()

        if (username.isNotEmpty() && password.isNotEmpty()) logIn.isEnabled = true
        else logIn.isEnabled = false
    }

    private fun logIn (){
        val switchingToChatActivity= Intent(activity, ChatActivity::class.java)
        startActivity(switchingToChatActivity)
        activity?.finish ()
    }

}