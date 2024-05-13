package com.yury.recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yury.recyclerview.ui.main.FullFragment

class FullActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, FullFragment.newInstance())
                .commitNow()
        }
    }
}