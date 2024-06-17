package com.emil.chatdet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.media.ExifInterface
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


class ChatActivity : AppCompatActivity() {
    private val listServers = CopyOnWriteArrayList<() -> Unit>()
    private val listBrokenConnections = CopyOnWriteArrayList<() -> Unit>()
    private val listMessages = LinkedList<Any>()
    private val listUsernames = ArrayList<String>()
    private val listDetectorResult = ArrayList<String>()
    private var errorDialogIsDisplayed = false
    private var isLoadedPicture = false
    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var chatList: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var ip: String
    private lateinit var id: String
    private lateinit var username: String
    private lateinit var accessCode: String
    private lateinit var message: String
    private lateinit var outputFile: String
    private lateinit var soundFile: File
    private lateinit var messageET: EditText
    private lateinit var info: TextView
    private lateinit var checkPhoto: FrameLayout
    private lateinit var picture: ImageView
    private lateinit var cancelPicture: ImageView
    private lateinit var down: ImageButton
    private lateinit var record: ImageView
    private lateinit var img: ImageView
    private lateinit var send: ImageView
    private lateinit var myAvatar: String
    private val timesList = ArrayList<String>()
    private lateinit var currentBitmap: Bitmap
    private var connectToServerText: ServerConnection?=null
    private var connectToServerDatabase: ServerConnection?=null
    private var connectToServerDataForwarding: ServerConnection?=null
    private var connectToServerPicture: ServerConnection?=null
    private var connectToServerUsername: ServerConnection?=null
    private var connectToServerSound: ServerConnection?=null
    private var connectToServerDetector: ServerConnection?=null
    private var connectToServerStatus: ServerConnection?=null
    private lateinit var dataPerf: SharedPreferences
    private val userDataMap = ConcurrentHashMap<String, Any>()
    val PERMISSIONS_RECORD_AUDIO = 1
    private var secondsElapsed = 0
    private var job: Job? = null
    private var jobAmplitude: Job? = null
    @SuppressLint("MissingInflatedId", "NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        listServers.add { connectToServerStatus() }
        listServers.add { connectToServerTextMessage() }
        listServers.add { connectToServerDatabase() }
        listServers.add { connectToServerDataForwarding() }
        listServers.add { connectToServerUsername() }
        listServers.add { connectToServerPicture() }
        listServers.add { connectToServerSound() }
        listServers.add { connectToServerDetector() }
        val ipPref = getSharedPreferences("Ip", Context.MODE_PRIVATE)
        ip = ipPref.getString("Ip", ChatDetPrivateKey.IP).toString()
        dataPerf = getSharedPreferences("DataChatDet", Context.MODE_PRIVATE)
        username = dataPerf.getString("username", "username").toString()
        id = dataPerf.getString("id", "0").toString()
        val menu = DialogMenu(this)
        val connect = menu.findViewById<Button>(R.id.bt_connect)
        val accessCodeET = menu.findViewById<EditText>(R.id.et_access_code)
        val shortСode = menu.findViewById<TextView>(R.id.tv_error)
        val volumeAmplitude = findViewById<ProgressBar>(R.id.pb_volume_amplitude)
        val stopwatch = findViewById<TextView>(R.id.tv_stopwatch_record)
        val stopRecording = findViewById<ImageView>(R.id.iv_stop_record)
        down = findViewById(R.id.ib_down)
        info = findViewById(R.id.tv_info)
        checkPhoto = findViewById(R.id.fl_check_picture)
        picture = findViewById(R.id.iv_user_picture)
        cancelPicture = findViewById(R.id.iv_cancel_picture)
        chatList = findViewById(R.id.rv_chat_list)
        chatList.layoutManager = LinearLayoutManager(this@ChatActivity)
        val file = File(getExternalFilesDir(null as String?), "ChatDet")
       soundFile = file
        if (!file.exists()) {
            soundFile.mkdir()
        }
        adapter = ChatAdapter(this@ChatActivity, listMessages, listUsernames, username, userDataMap,timesList,listDetectorResult)
        chatList.adapter = adapter
        send = findViewById(R.id.iv_send)
        record = findViewById(R.id.iv_record)
        img = findViewById(R.id.iv_img)
        messageET = findViewById(R.id.et_message)

        chatList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled( chatList: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled( chatList, dx, dy)
                val layoutManager =  chatList.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (lastVisibleItemPosition == totalItemCount - 1) down.visibility = View.GONE
                 else  down.visibility = View.VISIBLE
            }
        })
        down.setOnClickListener { chatList.smoothScrollToPosition(adapter.itemCount - 1) }

        fun clearImage() {
            messageET.isEnabled = true
            messageET.alpha = 1f
            record.visibility = View.VISIBLE
            img.visibility = View.VISIBLE
            send.visibility = View.GONE
            checkPhoto.visibility = View.GONE
            isLoadedPicture = false
        }

        fun preparingForRecording (){
            messageET.visibility = View.GONE
            img.visibility = View.GONE
            record.visibility = View.GONE
            send.visibility=View.VISIBLE
            stopwatch.visibility = View.VISIBLE
            volumeAmplitude.visibility = View.VISIBLE
            stopRecording.visibility = View.VISIBLE
        }

        @SuppressLint("SuspiciousIndentation")
        fun updateVolume (){
            jobAmplitude = CoroutineScope(Dispatchers.Main).launch {

                val maxAmplitude: Int = mediaRecorder!!.maxAmplitude * 100 / 35000
                        runOnUiThread {
                            volumeAmplitude.progress = maxAmplitude
                        }
                delay(25)
                updateVolume()
            }

        }

        fun updateStopwatch () {
            secondsElapsed++
            val minutes = secondsElapsed / 60
            val seconds = secondsElapsed % 60
            stopwatch.text = String.format("%02d:%02d", minutes, seconds)
            job = CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                updateStopwatch()
            }
        }



        fun startRecording (){
            updateStopwatch ()
            updateVolume()
            isRecording = true
            outputFile = File(soundFile, "chatdet_record_" + System.currentTimeMillis() + ".3gp").absolutePath
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder?.setOutputFile(outputFile)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
        }



        fun stopRecording() {
            isRecording = false
            jobAmplitude?.cancel()
            job?.cancel()
           mediaRecorder?.stop()
            mediaRecorder?.release()
            messageET.visibility = View.VISIBLE
            img.visibility = View.VISIBLE
            record.visibility = View.VISIBLE
            send.visibility=View.GONE
            stopwatch.visibility = View.GONE
            volumeAmplitude.visibility=View.GONE
            stopRecording.visibility = View.GONE
            secondsElapsed = 0
        }




        record.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@ChatActivity, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this@ChatActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this@ChatActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            { ActivityCompat.requestPermissions(this@ChatActivity, arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_RECORD_AUDIO)
            } else {
                preparingForRecording ()
                startRecording ()
                lifecycleScope.launch(Dispatchers.IO) {
                    connectToServerStatus?.sendText("$username записывает голосовое сообщение..")
                }
            }
        }

        stopRecording.setOnClickListener {
            stopRecording()
            lifecycleScope.launch(Dispatchers.IO) {
                connectToServerStatus?.sendText("")
            }
        }

        send.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                connectToServerUsername?.sendText(username)
                connectToServerDetector?.sendText(ChatDetPrivateKey.GET_CURRENT_MESSAGE)
                connectToServerStatus?.sendText(ChatDetPrivateKey.END_OF_EDITING)
            }
            if (!isLoadedPicture) {
                if (isRecording){
                    stopRecording()
                    lifecycleScope.launch(Dispatchers.IO) {
                        val fis = FileInputStream(outputFile)
                        val audioData = ByteArray(File(outputFile).length().toInt())
                        fis.read(audioData)
                        fis.close()
                        println( audioData.size)
                        connectToServerSound?.sendText(Base64.getEncoder().encodeToString(audioData))
                    }
                }else {
                    message = messageET.text.toString().trim()
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            connectToServerText?.sendText(message)
                        } catch (e: IOException) {
                            runOnUiThread { Toast.makeText(this@ChatActivity, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
                            }
                        }finally {
                            runOnUiThread { messageET.setText("") }
                        }
                    }
                }
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    val stream = ByteArrayOutputStream()
                    currentBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
                    val byteArray = stream.toByteArray()
                    connectToServerPicture?.sendText(Base64.getEncoder().encodeToString(byteArray))
                    runOnUiThread { clearImage() }
                }
            }
        }

        img.setOnClickListener {
            openGallery()

        }
        cancelPicture.setOnClickListener {
            clearImage()
            lifecycleScope.launch(Dispatchers.IO) {
                connectToServerStatus?.sendText("")
            }
        }

        messageET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentText = s.toString().trim()

                if (currentText.isNotEmpty()) {
                    send.visibility = View.VISIBLE
                    record.visibility = View.GONE
                    img.visibility = View.GONE
                } else {
                    send.visibility = View.GONE
                    record.visibility = View.VISIBLE
                    img.visibility = View.VISIBLE
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    delay(500)
                    connectToServerStatus?.sendText("$username печатает..")
                }
            }

            override fun afterTextChanged(s: Editable?) {
                lifecycleScope.launch(Dispatchers.IO) {
                    delay (2500)
                    connectToServerStatus?.sendText(ChatDetPrivateKey.END_OF_EDITING)
                }
            }
        })




        connect.setOnClickListener {
            accessCode = accessCodeET.text.toString().trim()
            if (accessCode.length > 3) {
                listServers.forEach { it() }
                menu.dismiss()
            } else {
                accessCodeET.background.setColorFilter(
                    getColor(this@ChatActivity, R.color.edit_text_error), PorterDuff.Mode.SRC_ATOP)
                    shortСode.visibility = View.VISIBLE
            }
        }
        menu.setCancelable(false)
        menu.show()
    }

    private fun showErrorDialog() {
        errorDialogIsDisplayed = true
        val error = DialogError(this)
        val reconnect = error.findViewById<Button>(R.id.bt_reconnect)
        reconnect.setOnClickListener {
            listBrokenConnections.forEach { it() }
            errorDialogIsDisplayed = false
            error.dismiss()
            listBrokenConnections.clear()
        }
        error.setCancelable(false)
        error.show()
    }

    private fun connectToServerStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                connectToServerStatus =
                    ServerConnection(ip, ChatDetPrivateKey.SERVER_STATUS_PORT)
                connectToServerStatus?.sendText(accessCode)
                    connectToServerStatus?.sendText("$username пoдключился")

                var status: String?
                var ping:String?
                while (true) {
                     ping  = connectToServerStatus?.receiveText()
                    status =  connectToServerStatus?.receiveText()

                    if (status != null) {
                        if (status.contains("пoдключился")||status.contains("oтключился")) {
                            runOnUiThread { Toast.makeText(this@ChatActivity, status, Toast.LENGTH_SHORT).show() }
                                connectToServerDataForwarding?.sendText("$username${ChatDetPrivateKey.DELIMITER}$myAvatar")

                        }
                       else runOnUiThread { info.setText(status) }
                    } else break
                }

            } catch (e: IOException) {
                runOnUiThread {
                    connectToServerStatus?.close()
                    listBrokenConnections.add { connectToServerStatus() }
                    if (!errorDialogIsDisplayed) showErrorDialog()
                }
            }
        }
    }



    private fun connectToServerDetector() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                connectToServerDetector =
                    ServerConnection(ip, ChatDetPrivateKey.SERVER_DETECTOR_PORT)
                connectToServerDetector?.sendText(accessCode)
                var result: String?
                var ping: String
                while (true) {
                    ping =  connectToServerDetector?.receiveText()!!
                    result =   connectToServerDetector?.receiveText()
                    if (result != null) {
                        listDetectorResult.add(result)
                    } else break
                }

            } catch (e: IOException) {
                runOnUiThread {
                    connectToServerDetector?.close()
                    listBrokenConnections.add { connectToServerDetector() }
                    if (!errorDialogIsDisplayed) showErrorDialog()
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun connectToServerSound() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                connectToServerSound = ServerConnection(ip, ChatDetPrivateKey.SERVER_SOUND_PORT)
                connectToServerSound?.sendText(accessCode)
                var sound:String?
                var ping: String
                while (true) {
                    ping = connectToServerSound?.receiveText()!!
                    sound = connectToServerSound?.receiveText()

                    if (sound != null) {
                        val soundBytes = Base64.getDecoder().decode(sound)
                        val tempFile = File.createTempFile("chatdet_temp_s", ".3gp")
                        val fos = FileOutputStream(tempFile)
                        fos.write( soundBytes)
                        fos.close()
                        timesList.add(getCurrentTime())
                        listMessages.add(tempFile)
                        runOnUiThread {
                            adapter.notifyItemChanged(listMessages.size-1)
                            chatList.scrollToPosition(adapter.itemCount - 1)
                        }
                    } else break
                }

            } catch (e: IOException) {
                runOnUiThread {
                    connectToServerSound?.close()
                    listBrokenConnections.add {  connectToServerSound() }
                    if (!errorDialogIsDisplayed) showErrorDialog()
                }
            }
        }
    }

    // (connectToServerText.receiveText().also { message = it })!=null
    @RequiresApi(Build.VERSION_CODES.O)

    private fun connectToServerPicture() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                connectToServerPicture = ServerConnection(ip, ChatDetPrivateKey.SERVER_PICTURE_PORT)
                connectToServerPicture?.sendText(accessCode)
                var picture: String?
                var ping: String
                while (true) {
                    ping = connectToServerPicture?.receiveText()!!
                    picture = connectToServerPicture?.receiveText()
                    if (picture != null) {
                        val imageBytes = Base64.getDecoder().decode(picture)
                        val tempFile = File.createTempFile("chatdet_temp_p", ".jpg")
                        val fos = FileOutputStream(tempFile)
                       fos.write(imageBytes)
                        fos.close()
                        timesList.add(getCurrentTime())
                        listMessages.add(tempFile)
                        runOnUiThread {
                            adapter.notifyItemChanged(listMessages.size-1)
                            chatList.scrollToPosition(adapter.itemCount - 1)
                        }
                    } else break
                }

            } catch (e: IOException) {
                runOnUiThread {
                    connectToServerPicture?.close()
                    listBrokenConnections.add { connectToServerPicture() }
                    if (!errorDialogIsDisplayed) showErrorDialog()
                }
            }
        }
    }



    private fun connectToServerUsername() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                connectToServerUsername =
                    ServerConnection(ip, ChatDetPrivateKey.SERVER_USERNAMES_PORT)
                connectToServerUsername?.sendText(accessCode)
                var name: String?
                var ping: String
                while (true) {
                    ping = connectToServerUsername?.receiveText()!!
                    name = connectToServerUsername?.receiveText()
                    if (name != null) listUsernames.add(name)
                    else break
                }

            } catch (e: IOException) {
                runOnUiThread {
                    connectToServerUsername?.close()
                    listBrokenConnections.add { connectToServerUsername() }
                    if (!errorDialogIsDisplayed) showErrorDialog()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun connectToServerDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                connectToServerDatabase = ServerConnection(ip, ChatDetPrivateKey.SERVER_SQL_PORT)
                connectToServerDatabase?.sendText(ChatDetPrivateKey.GET_AVATAR)
                connectToServerDatabase?.sendText(id)
                myAvatar = connectToServerDatabase?.receiveText()!!
                connectToServerDataForwarding?.sendText("$username${ChatDetPrivateKey.DELIMITER}$myAvatar")
            } catch (e: IOException) {
                runOnUiThread {
                    connectToServerDatabase?.close()
                    listBrokenConnections.add { connectToServerDatabase() }
                    if (!errorDialogIsDisplayed) showErrorDialog()
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun connectToServerDataForwarding() {
      lifecycleScope.launch(Dispatchers.IO) {
            try {
                connectToServerDataForwarding =
                    ServerConnection(ip, ChatDetPrivateKey.SERVER_FORWARDING_PORT)
                connectToServerDataForwarding?.sendText(accessCode)
                var data: String?
                var ping: String
                while (true) {
                    ping = connectToServerDataForwarding?.receiveText()!!
                    data = connectToServerDataForwarding?.receiveText()
                    if (data != null) {
                        val parts = data.split(ChatDetPrivateKey.DELIMITER)
                        val username = parts[0]
                        val avatar = parts[1]
                        if (avatar != "NULL") {
                            val imageBytes = Base64.getDecoder().decode(avatar)
                            val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            val roundedAvatar = Avatar.getRoundedBitmap(imageBitmap)
                            val tempFile = File.createTempFile("chatdet_temp_", ".png")
                            val fos = FileOutputStream(tempFile)
                            roundedAvatar.compress(Bitmap.CompressFormat.PNG, 40, fos);
                            fos.close()
                            userDataMap.put(username, tempFile)
                        } else userDataMap.put(username, "NULL")
                    } else break
                }
            } catch (e: IOException) {
                runOnUiThread {
                    connectToServerDataForwarding?.close()
                    listBrokenConnections.add { connectToServerDataForwarding() }
                    if (!errorDialogIsDisplayed) showErrorDialog()
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun connectToServerTextMessage() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                connectToServerText = ServerConnection(ip, ChatDetPrivateKey.SERVER_SMS_PORT)
                connectToServerText?.sendText(accessCode)
                var message: String?
                var ping: String
                while (true) {
                    ping = connectToServerText?.receiveText()!!
                    message = connectToServerText?.receiveText()
                    if (message != null) {
                        timesList.add(getCurrentTime())
                        listMessages.add(message.replace(ChatDetPrivateKey.NEW_LINE_KEY, "\n"))
                        runOnUiThread {
                            adapter.notifyItemChanged(listMessages.size-1)
                            chatList.scrollToPosition(adapter.itemCount - 1)
                        }

                    } else break
                }

            } catch (e: IOException) {
                runOnUiThread {
                    connectToServerText?.close()
                    listBrokenConnections.add { connectToServerTextMessage() }
                    if (!errorDialogIsDisplayed) showErrorDialog()

                }
            }
        }
    }

    private fun displayPhoto() {
        isLoadedPicture = true
        checkPhoto.visibility = View.VISIBLE
        messageET.isEnabled = false
        messageET.alpha = 0.75f
        record.visibility = View.GONE
        img.visibility = View.GONE
        send.visibility = View.VISIBLE
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("HH:mm")
        val currentTime = Date()
        return formatter.format(currentTime)
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, RegistrationFragment.PICK_IMAGE_REQUEST)
        lifecycleScope.launch(Dispatchers.IO) {
            connectToServerStatus?.sendText("$username выбирает изображение..")
        }
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleScope.launch(Dispatchers.IO) {
        if (requestCode == RegistrationFragment.PICK_IMAGE_REQUEST && resultCode == -1) {
            val uri: Uri? = data?.data
            uri?.let {
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                val openFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                val exifInterface =
                    openFileDescriptor?.fileDescriptor?.let { ExifInterface(it) }
                val orientation = exifInterface?.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                val rotatedBitmap: Bitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> Avatar.rotateImage(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> Avatar.rotateImage(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> Avatar.rotateImage(bitmap, 270f)
                    else -> bitmap
                }
                // val path = MediaStore.Images.Media.insertImage(this@ChatActivity.contentResolver,rotatedBitmap, "Image", null)
                // val ur = Uri.parse(path)

                currentBitmap = rotatedBitmap
                val tempFile = File.createTempFile("chatdet_temp_", ".jpg")
                val fos = FileOutputStream(tempFile)
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
                fos.close()
                runOnUiThread {
                Picasso.get().load(tempFile).into(picture)
                    displayPhoto()
                }
                connectToServerStatus?.sendText(ChatDetPrivateKey.END_OF_EDITING)


            }
            }

        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_menu -> {
                recreate()
                return true
            }

            R.id.m_exit -> {
                val editor = dataPerf.edit()
                editor.putBoolean("remember", false)
                editor.apply()
                val switchingToAccountDetailsActivity =
                    Intent(this@ChatActivity, AccountDetailsActivity::class.java)
                startActivity(switchingToAccountDetailsActivity)
                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_RECORD_AUDIO -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@ChatActivity, "Необходимо предоставить разрешение", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch(Dispatchers.IO) {
            connectToServerStatus?.sendText("$username oтключился")
        }
        val cacheDir = applicationContext.cacheDir
        //cacheDir.deleteRecursively()
        cacheDir.listFiles()?.forEach { it.delete() }
        connectToServerText?.close()
        connectToServerDataForwarding?.close()
        connectToServerDatabase?.close()
        connectToServerDetector?.close()
        connectToServerSound?.close()
        connectToServerUsername?.close()
        connectToServerPicture?.close()
        connectToServerStatus?.close()
        job?.cancel()
        jobAmplitude?.cancel()
    }
}


