package com.emil.chatdet

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AccountDetailsActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)
        val logInFragment = LogInFragment()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, logInFragment)
            commit()
        }
    }
}