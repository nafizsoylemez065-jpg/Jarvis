package com.nafiz.jarvis.commands

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

object Flashlight {

    private var torchOn = false

    fun set(context: Context, on: Boolean): Boolean = runCatching {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList.firstOrNull { id ->
            manager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return false
        manager.setTorchMode(cameraId, on)
        torchOn = on
        true
    }.getOrDefault(false)

    fun toggle(context: Context): Boolean = set(context, !torchOn)
}
