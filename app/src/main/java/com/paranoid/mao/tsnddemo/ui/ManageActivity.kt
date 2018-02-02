package com.paranoid.mao.tsnddemo.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.paranoid.mao.tsnddemo.R


class ManageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SensorManageFragment())
                .commit()
    }

}
