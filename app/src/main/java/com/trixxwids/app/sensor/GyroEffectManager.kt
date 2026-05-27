package com.trixxwids.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.View
import kotlin.math.abs

class GyroEffectManager(private val context: Context) {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var isEnabled = false
    private var isAvailable = false
    private var targetView: View? = null

    private val sensorListener = object : SensorEventListener {
        private val lowPassFilter = LowPassFilter(0.3f)
        private var tiltX = 0f
        private var tiltY = 0f

        override fun onSensorChanged(event: SensorEvent) {
            if (!isEnabled || targetView == null) return

            val ax: Float
            val ay: Float
            val az: Float

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                ax = lowPassFilter.filter(event.values[0], 0)
                ay = lowPassFilter.filter(event.values[1], 1)
                az = lowPassFilter.filter(event.values[2], 2)
            } else {
                return
            }

            val maxTilt = 15f
            tiltX = (ay / 9.8f * maxTilt).coerceIn(-maxTilt, maxTilt)
            tiltY = (-ax / 9.8f * maxTilt).coerceIn(-maxTilt, maxTilt)

            if (abs(tiltX) < 0.5f) tiltX = 0f
            if (abs(tiltY) < 0.5f) tiltY = 0f

            targetView?.rotationX = tiltY
            targetView?.rotationY = tiltX
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun checkAvailability(): Boolean {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sensorManager == null) {
            isAvailable = false
            return false
        }
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val hasAccelerometer = accelerometer != null
        gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        isAvailable = hasAccelerometer
        return isAvailable
    }

    fun setTargetView(view: View) {
        targetView = view
    }

    fun enable() {
        if (!isAvailable && !checkAvailability()) return
        if (isEnabled) return
        sensorManager?.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        isEnabled = true
    }

    fun disable() {
        if (!isEnabled) return
        sensorManager?.unregisterListener(sensorListener)
        targetView?.rotationX = 0f
        targetView?.rotationY = 0f
        isEnabled = false
    }

    fun isEnabled(): Boolean = isEnabled
    fun isAvailable(): Boolean = isAvailable

    fun toggle(): Boolean {
        if (isEnabled) {
            disable()
            return false
        } else {
            if (!isAvailable && !checkAvailability()) return false
            enable()
            return true
        }
    }

    fun onPause() {
        if (isEnabled) {
            sensorManager?.unregisterListener(sensorListener)
        }
    }

    fun onResume() {
        if (isEnabled) {
            sensorManager?.registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun onDestroy() {
        disable()
        sensorManager = null
        accelerometer = null
        gyroscope = null
        targetView = null
    }

    private class LowPassFilter(alpha: Float) {
        private val alpha: Float
        private val lastValues = FloatArray(3)

        init {
            this.alpha = if (alpha > 1f) 1f else if (alpha <= 0f) 0.1f else alpha
        }

        fun filter(value: Float, index: Int): Float {
            val filtered = lastValues[index] + alpha * (value - lastValues[index])
            lastValues[index] = filtered
            return filtered
        }
    }
}
