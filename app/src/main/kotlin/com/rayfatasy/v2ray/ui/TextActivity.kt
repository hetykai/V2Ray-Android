package com.rayfatasy.v2ray.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.rayfatasy.v2ray.R
import kotlinx.android.synthetic.main.activity_text.*

class TextActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)

        title = intent.getStringExtra("title")
        text_view.text = intent.getStringExtra("text")
    }
}