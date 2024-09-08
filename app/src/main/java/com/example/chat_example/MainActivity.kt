package com.example.chat_example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ekghanti_livechat_sdk.LiveChat
import com.example.ekghanti_livechat_sdk.WebviewLivechat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btn1: Button = findViewById(R.id.webviewBtn)
        btn1.setOnClickListener {

            val intent = Intent(this, WebviewLivechat::class.java)
            startActivity(intent)


        }

        val btn2: Button = findViewById(R.id.livechatBtn)
        btn2.setOnClickListener {

            val intent = Intent(this, LiveChat::class.java)
            startActivity(intent)

        }




    }
}