package com.example.ekghanti_livechat_sdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ekghanti_livechat_sdk.adapter.ChatAdapter
import com.example.ekghanti_livechat_sdk.apiInterface.ApiInterface
import com.example.ekghanti_livechat_sdk.model.chat.ChatData
import com.example.ekghanti_livechat_sdk.model.chat.Message
import com.example.ekghanti_livechat_sdk.socket.WebSocketListener
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.WebSocket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.FileOutputStream

class LiveChat : AppCompatActivity() {

    private lateinit var webSocket: WebSocket
    lateinit var myRecyclerView: RecyclerView
    lateinit var myAdapter: ChatAdapter

    private val messageList: MutableList<Message> = mutableListOf()

    private val PICK_IMAGE_REQUEST = 1

    private var imageUri: Uri? = null
    private var uploadedUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_live_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val scrollView: ScrollView = findViewById(R.id.scrollView)

        val listener = WebSocketListener { newMessage ->
            runOnUiThread {
                messageList.add(newMessage)
                myAdapter.notifyItemInserted(messageList.size - 1)
                myRecyclerView.scrollToPosition(messageList.size - 1)
                scrollView.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
        }

        socketConnection(listener)
        setupRecyclerView(listener)

//       click me button
//        val button: Button = findViewById(R.id.clickMeBtn)
//        button.setOnClickListener {
//            listener.onClickMe()
//        }

        // send message button click
        val sendButton: ImageButton = findViewById(R.id.sendMessageButton)
        val messageEditor: EditText = findViewById(R.id.editTextText)
        sendButton.setOnClickListener {
            val textMsg = messageEditor.text.toString()
            val pickedImage: ImageView = findViewById(R.id.imageView)

            if (uploadedUrl != null) {
                listener.sendMessage(uploadedUrl!!, "image")
                clearImageSelection(pickedImage)
            } else if (textMsg.any { it.isLetter() }) {
                listener.sendMessage(textMsg, "text")
                //messageEditor.requestFocus()
                //clearMessageInput(messageEditor)
                messageEditor.post {
                    //clearMessageInput(messageEditor)

                    messageEditor.requestFocus()  // Re-request focus after clearing
                    messageEditor.setText("")
                    //scrollView.post {
                    //    scrollView.fullScroll(View.FOCUS_DOWN)
                    //}
                }
            }
            //scrollView.post {
            //    scrollView.fullScroll(View.FOCUS_DOWN)
            //}
        }

        //image picker function
        val imagePicker: ImageButton = findViewById(R.id.addAttachment)
        imagePicker.setOnClickListener {
            openGallery()
        }


    }


    //function to get image from gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val pickedImage: ImageView = findViewById(R.id.imageView)
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("imageUri", data.toString())
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            pickedImage.visibility = ImageView.VISIBLE
            imageUri = data.data!!
            Picasso.get().load(imageUri).into(pickedImage)

            Log.e("image", imageUri.toString())
            onUpload()
        }
    }

    //function to get image extension
    private fun getFileExtensionFromUri(uri: Uri): String? {
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }

    //funtion to upload image
    private fun onUpload() {
        val fileDir = applicationContext.filesDir
        val fileExtension = imageUri?.let { getFileExtensionFromUri(it) }

        val file = File(fileDir, "image.${fileExtension}")
        val inputStream = imageUri?.let { contentResolver.openInputStream(it) }
        val outputStream = FileOutputStream(file)
        Log.e("file extension", file.toString())
        inputStream?.copyTo(outputStream)

        val requestBody = file.asRequestBody("image/*".toMediaType())
        val part = MultipartBody.Part.createFormData("sending_file", file.name, requestBody)


        //val logging = HttpLoggingInterceptor().apply {
        //    level = HttpLoggingInterceptor.Level.BODY // Log request and response body
        //}
        //
        //val okHttpClient = OkHttpClient.Builder()
        //    .addInterceptor(logging)
        //    .build()


        val retrofitBuilderUpload = Retrofit.Builder().baseUrl("https://chat.orbit360.cx:8443/")
            //.client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()).build()
            .create(ApiInterface::class.java)

        val retrofitData = retrofitBuilderUpload.uploadImage(part)
        retrofitData.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                Log.e("Image upload sddd8888888uccess", response.toString())
                if (response.isSuccessful) {
                    uploadedUrl = response.body().toString()
                    Log.e("Image upload success", "URL: $uploadedUrl")
                } else {
                    Log.e("Image upload failed", "Response code: ${response.code()}")
                }

            }

            override fun onFailure(call: Call<String?>, p1: Throwable) {
                Toast.makeText(
                    this@LiveChat, "Failed to load data. Please try again.", Toast.LENGTH_SHORT
                ).show()
                Log.e("upload error", p1.toString())
            }
        })


    }

    //function to setup recyclerView
    private fun setupRecyclerView(listener: WebSocketListener) {
        myRecyclerView = findViewById(R.id.recyclerView)
        myAdapter = ChatAdapter(this, messageList){message ->
            //Toast.makeText(this, "Button clicked for message: $message", Toast.LENGTH_SHORT).show()
            listener.sendMessage(message, "text", isButton = true)
        }
        myRecyclerView.adapter = myAdapter
        myRecyclerView.layoutManager = LinearLayoutManager(this)
    }



    //socket connection
    private fun socketConnection(listener: WebSocketListener) {
        val client = OkHttpClient()
        val request: Request = Request.Builder().url("wss://chat.orbit360.cx:8443/").build()
        webSocket = client.newWebSocket(request, listener)
    }



    //function to api call
//    private fun loadData() {
//        val retrofitBuilder =
//            Retrofit.Builder().baseUrl("https://chat.orbit360.cx:8443/chatStorageWhook/")
//                .addConverterFactory(GsonConverterFactory.create()).build()
//                .create(ApiInterface::class.java)
//
//        val retrofitData = retrofitBuilder.getData()
//
//        retrofitData.enqueue(object : Callback<ChatData?> {
//            override fun onResponse(call: Call<ChatData?>, response: Response<ChatData?>) {
//                response.body()?.messages?.let { messages ->
//                    messageList.addAll(messages)  // Update the message list
//                    myAdapter.notifyDataSetChanged()  // Notify the adapter
//                }
//                //val datalist = response.body()?.messages!!;
//            }
//
//            override fun onFailure(call: Call<ChatData?>, p1: Throwable) {
//                Toast.makeText(
//                    this@LiveChat, "Failed to load data. Please try again.", Toast.LENGTH_SHORT
//                ).show()
//                //println("${p1} oneone this is error")
//            }
//        })
//
//    }


    //function to open gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // function to clear selected image
    private fun clearImageSelection(pickedImage: ImageView) {
        imageUri = null
        uploadedUrl = null
        pickedImage.setImageDrawable(null)
        pickedImage.visibility = ImageView.GONE
    }

    //function to clear message input
    private fun clearMessageInput(messageEditor: EditText) {
        //messageEditor.setText("")
        messageEditor.requestFocus()
        imageUri = null
        uploadedUrl = null
    }


}