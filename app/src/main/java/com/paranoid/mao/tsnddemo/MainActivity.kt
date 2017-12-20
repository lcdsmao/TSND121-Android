package com.paranoid.mao.tsnddemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.View
import jp.walkmate.tsndservice.Listener.TSNDConnectionListener
import jp.walkmate.tsndservice.Service.Impl.TSNDServiceImpl
import jp.walkmate.tsndservice.Service.TSNDService
import jp.walkmate.tsndservice.Thread.TSNDConnectionThread
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startBackgroundThread()
        // AP14021941 00:07:80:76:87:6e
        val tsndService: TSNDService = TSNDServiceImpl("00:07:80:76:87:6E")
        button.setOnClickListener { _ -> backgroundHandler?.post {
            if (!tsndService.isConnected) {
                tsndService.connect()
                Log.v("TSDN", "" + tsndService.isConnected)
            }
        }}
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Background Thread")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper)
    }
}
