package com.emil.chatdet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

class RecoveryPasswordFragment : Fragment() {
    private lateinit var usernameET:EditText
    private lateinit var codeWordET:EditText
    private lateinit var newPasswordET:EditText
    private lateinit var repeatNewPasswordET:EditText
    private lateinit var verify:Button
    private lateinit var change:Button
    private lateinit var connectToSQLServer:ServerConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val view =  inflater.inflate(R.layout.fragment_recovery_password, container, false)
        usernameET = view.findViewById(R.id.et_username)
        codeWordET = view.findViewById(R.id.et_code_word)
       newPasswordET = view.findViewById(R.id.et_new_password)
        repeatNewPasswordET = view.findViewById(R.id.et_repeat_new_password)
        verify = view.findViewById(R.id.bt_verify)
        val back = view.findViewById<Button>(R.id.bt_back)
        change = view.findViewById(R.id.bt_change)
        val loading = view.findViewById<ProgressBar>(R.id.pb_loading)
        val sharedPreferences = requireContext().getSharedPreferences("Ip", Context.MODE_PRIVATE)
        val ip = sharedPreferences.getString("Ip", ChatDetPrivateKey.IP)
        back.setOnClickListener {
            val logInFragment = LogInFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, logInFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        verify.setOnClickListener {
            loading.visibility = View.VISIBLE
            val username = usernameET.text.toString().trim()
            val codeWord = codeWordET.text.toString().trim()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    connectToSQLServer = ServerConnection(ip, ChatDetPrivateKey.SERVER_SQL_PORT)
                    connectToSQLServer.sendText(ChatDetPrivateKey.REPLACE_PASSWORD)
                    connectToSQLServer.sendText(username)
                    connectToSQLServer.sendText(codeWord)
                    val response = connectToSQLServer.receiveText()
                    if (response == ChatDetPrivateKey.USER_EXIST) {
                        activity?.runOnUiThread {
                            newPasswordET.visibility = View.VISIBLE
                            repeatNewPasswordET.visibility = View.VISIBLE
                            change.visibility = View.VISIBLE
                            usernameET.visibility = View.GONE
                            codeWordET.visibility = View.GONE
                            verify.visibility = View.GONE
                        }
                    } else if (response == ChatDetPrivateKey.USER_NOT_EXIST) {
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Пользователя с такими данными не существует", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: IOException) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    activity?.runOnUiThread { loading.visibility = View.GONE}
                }
            }}

            change.setOnClickListener {
                loading.visibility = View.VISIBLE
                val newPassword = newPasswordET.text.toString().trim()
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                       connectToSQLServer.sendText(newPassword)
                        val response = connectToSQLServer.receiveText()
                        if (response == ChatDetPrivateKey.REPLACE_PASSWORD_SUCCESSFUL){
                            activity?.runOnUiThread { Toast.makeText(requireContext(), "Пароль успешно изменён", Toast.LENGTH_SHORT).show() }
                        }else if (response == ChatDetPrivateKey.REPLACE_PASSWORD_ERROR){
                            activity?.runOnUiThread { Toast.makeText(requireContext(), "Возникла ошибка", Toast.LENGTH_SHORT).show() }
                        }
connectToSQLServer.close()
                    } catch (e: IOException) {
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(), "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        activity?.runOnUiThread {  loading.visibility = View.GONE}
                    }
                }

            }

            codeWordET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkEditText()
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })

            usernameET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkEditText()
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })
            newPasswordET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkEditText()
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })
            repeatNewPasswordET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkEditText()
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })

        return view
    }

    private fun checkEditText (){
        val username = usernameET.text.toString().trim()
        val codeWord = codeWordET.text.toString().trim()
        val newPassword = newPasswordET.text.toString().trim()
        val repeatNewPassword = repeatNewPasswordET.text.toString().trim()
        if (username.isNotEmpty() && codeWord.isNotEmpty())
            verify.isEnabled = true
        else
            verify.isEnabled = false


        if (newPassword.isNotEmpty() && repeatNewPassword.isNotEmpty() && (newPassword == repeatNewPassword)) {
            change.isEnabled = true
        } else
            change.isEnabled = false

    }

}