package com.paranoid.mao.tsnddemo.ui.manage

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.replaceFragmentInActivity
import dagger.android.support.DaggerAppCompatActivity


class ManageActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        replaceFragmentInActivity(SensorManageFragment(), R.id.fragment_container)
    }
}
