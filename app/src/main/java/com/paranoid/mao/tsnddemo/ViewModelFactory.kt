package com.paranoid.mao.tsnddemo

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.paranoid.mao.tsnddemo.data.DataRepository
import com.paranoid.mao.tsnddemo.ui.main.MainViewModel
import com.paranoid.mao.tsnddemo.ui.manage.ManageViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewModelFactory @Inject constructor(
        private val dataRepository: DataRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) =
            with(modelClass) {
                when {
                    isAssignableFrom(MainViewModel::class.java) ->
                        MainViewModel(dataRepository)
                    isAssignableFrom(ManageViewModel::class.java) ->
                            ManageViewModel(dataRepository)
                    else ->
                        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T
}