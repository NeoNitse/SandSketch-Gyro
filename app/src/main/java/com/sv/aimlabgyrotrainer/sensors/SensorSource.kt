package com.sv.aimlabgyrotrainer.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


class SensorSource(context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val gyroSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val hasGyroscope: Boolean get() = gyroSensor != null
    val hasAccelerometer: Boolean get() = accelSensor != null

    private var gyroCallback: ((Float, Float, Float, Long) -> Unit)? = null
    private var accelCallback: ((Float, Float, Float, Long) -> Unit)? = null

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_GYROSCOPE -> gyroCallback?.invoke(
                    event.values[0], event.values[1], event.values[2], event.timestamp
                )
                Sensor.TYPE_ACCELEROMETER -> accelCallback?.invoke(
                    event.values[0], event.values[1], event.values[2], event.timestamp
                )
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /** Se llama en onResume: registra los sensores disponibles. */
    fun start(
        onGyroscope: (Float, Float, Float, Long) -> Unit,
        onAccelerometer: (Float, Float, Float, Long) -> Unit
    ) {
        gyroCallback = onGyroscope
        accelCallback = onAccelerometer
        gyroSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        accelSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    /** Se llama en onPause: libera los sensores para no gastar batería. */
    fun stop() {
        sensorManager.unregisterListener(listener)
        gyroCallback = null
        accelCallback = null
    }
}