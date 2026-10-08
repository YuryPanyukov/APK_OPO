package com.yury.recyclerview

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ApkoOpoApplication : Application()
{
  override fun onCreate() {
    super.onCreate()
  }
}
