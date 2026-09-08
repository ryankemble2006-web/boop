package com.boop.shieldturbo.apps

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object AppRoutes {
    fun launch(context: Context, packageName: String): Intent? =
        context.packageManager.getLeanbackLaunchIntentForPackage(packageName)
            ?: context.packageManager.getLaunchIntentForPackage(packageName)

    fun info(packageName: String): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
}
