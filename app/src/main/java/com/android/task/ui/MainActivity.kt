package com.android.task.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.task.R
import com.android.task.databinding.ActivityMainBinding
import com.android.task.ui.products.ProductsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, ProductsFragment())
            .commit()
    }
}
