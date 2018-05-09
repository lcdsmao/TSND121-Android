package com.paranoid.mao.tsnddemo.ui.manage

import android.os.Bundle
import com.paranoid.mao.tsnddemo.R
import com.paranoid.mao.tsnddemo.replaceFragmentInActivity
import dagger.android.support.DaggerAppCompatActivity


class ManageActivity : DaggerAppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        replaceFragmentInActivity(SensorManageFragment(), R.id.fragment_container)
    }
}
