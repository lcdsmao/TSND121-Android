package com.paranoid.mao.tsnd121.ui.manage

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.paranoid.mao.tsnd121.R
import com.paranoid.mao.tsnd121.replaceFragmentInActivity


class ManageActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        replaceFragmentInActivity(SensorManageFragment(), R.id.fragment_container)
    }
}
