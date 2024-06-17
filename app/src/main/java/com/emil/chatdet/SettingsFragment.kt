package com.emil.chatdet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


class SettingsFragment : Fragment() {
    private var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val ipEt = view.findViewById<EditText>(R.id.et_ip)
        ipEt.filters = arrayOf(IpAddressInputFilter())
        var ip:String
        val back = view.findViewById<Button>(R.id.bt_back)
        val saveIp = view.findViewById<Button>(R.id.bt_save)
        back.setOnClickListener {
            val logInFragment = LogInFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, logInFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        val sharedPreferences = requireActivity().getSharedPreferences("Ip", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        saveIp.setOnClickListener {
            ip = ipEt.text.toString().trim()
            for (i in ip.indices) {
                if (ip[i] == '.') {
                    count++
                }
            }

            if (ip.length < 7 || count != 3 || ip.length > 15)
                Toast.makeText(requireContext(), "Неверный формат", Toast.LENGTH_SHORT).show()
            else{
                Toast.makeText(requireContext(), "ip сохранен", Toast.LENGTH_SHORT).show()
                ipEt.setText("")
                editor.putString("Ip", ip)
                editor.apply()
            }
            count = 0
        }

        return view
    }


}