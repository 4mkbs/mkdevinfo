package com.sakib.devinfo.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.os.Handler
import android.os.Looper
import android.os.HandlerThread
import java.util.concurrent.ConcurrentHashMap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sakib.devinfo.R
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class SensorsFragment : Fragment(), SensorEventListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sensorsAdapter: SensorsAdapter
    private lateinit var sensorManager: SensorManager
    // Latest raw values (no UI formatting) keyed by sensor type
    private val sensorValues = ConcurrentHashMap<Int, FloatArray>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val uiHandler = Handler(Looper.getMainLooper())
    private var sensorHandlerThread: HandlerThread? = null
    private var sensorBgHandler: Handler? = null
    private var uiUpdateRunnable: Runnable? = null
    private val uiUpdateIntervalMs = 400L
    private var listenersRegistered = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_sensors, container, false)
        
        initViews(root)
        setupSensors()
    loadSensorInformation() // will start UI updates after adapter ready
        
        return root
    }

    private fun initViews(root: View) {
        recyclerView = root.findViewById(R.id.recyclerViewSensors)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupSensors() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun loadSensorInformation() {
        coroutineScope.launch(Dispatchers.Default) {
            val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
            val sensorItems = sensors.map { sensor ->
                SensorItem(
                    name = sensor.name,
                    type = getSensorTypeName(sensor.type),
                    vendor = sensor.vendor,
                    version = sensor.version.toString(),
                    power = "${sensor.power} mA",
                    resolution = sensor.resolution.toString(),
                    maxRange = sensor.maximumRange.toString(),
                    sensorTypeInt = sensor.type
                )
            }
            withContext(Dispatchers.Main) {
                sensorsAdapter = SensorsAdapter()
                recyclerView.adapter = sensorsAdapter
                sensorsAdapter.submitList(sensorItems)
                registerSensorListeners()
                startThrottledUiUpdates()
            }
        }
    }

    private fun registerSensorListeners() {
        if (listenersRegistered) return
        val commonSensorTypes = listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_RELATIVE_HUMIDITY,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR
        )

        // Start background thread if not started
        if (sensorHandlerThread == null) {
            sensorHandlerThread = HandlerThread("SensorEventsThread", Thread.NORM_PRIORITY).apply { start() }
            sensorBgHandler = Handler(sensorHandlerThread!!.looper)
        }

        val rateUs = 100_000 // 100ms sampling; reduces pressure
        commonSensorTypes.forEach { type ->
            sensorManager.getDefaultSensor(type)?.let { sensor ->
                sensorManager.registerListener(this, sensor, rateUs, rateUs, sensorBgHandler)
            }
        }
        listenersRegistered = true
    }

    private fun getSensorTypeName(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer"
            Sensor.TYPE_PROXIMITY -> "Proximity"
            Sensor.TYPE_LIGHT -> "Light"
            Sensor.TYPE_PRESSURE -> "Pressure"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "Temperature"
            Sensor.TYPE_RELATIVE_HUMIDITY -> "Humidity"
            Sensor.TYPE_GRAVITY -> "Gravity"
            Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
            Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
            Sensor.TYPE_ORIENTATION -> "Orientation"
            Sensor.TYPE_STEP_COUNTER -> "Step Counter"
            Sensor.TYPE_STEP_DETECTOR -> "Step Detector"
            Sensor.TYPE_HEART_RATE -> "Heart Rate"
            else -> "Type $type"
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Store latest values only; UI will pick them up on next throttled cycle
        // Minimal work: copy values; avoid heavy formatting here
        sensorValues[event.sensor.type] = event.values.clone()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    private fun formatValues(sensorType: Int, values: FloatArray): String {
        return try {
            when (sensorType) {
                Sensor.TYPE_ACCELEROMETER -> "X: %.2f, Y: %.2f, Z: %.2f m/s²".format(values[0], values[1], values[2])
                Sensor.TYPE_GYROSCOPE -> "X: %.2f, Y: %.2f, Z: %.2f rad/s".format(values[0], values[1], values[2])
                Sensor.TYPE_MAGNETIC_FIELD -> "X: %.2f, Y: %.2f, Z: %.2f µT".format(values[0], values[1], values[2])
                Sensor.TYPE_PROXIMITY -> "Distance: %.2f cm".format(values[0])
                Sensor.TYPE_LIGHT -> "Light: %.2f lx".format(values[0])
                Sensor.TYPE_PRESSURE -> "Pressure: %.2f hPa".format(values[0])
                Sensor.TYPE_AMBIENT_TEMPERATURE -> "Temperature: %.2f °C".format(values[0])
                Sensor.TYPE_RELATIVE_HUMIDITY -> "Humidity: %.2f%%".format(values[0])
                else -> values.joinToString(", ") { "%.2f".format(it) }
            }
        } catch (e: Exception) {
            "--"
        }
    }

    private fun startThrottledUiUpdates() {
        if (uiUpdateRunnable != null) return
        uiUpdateRunnable = object : Runnable {
            override fun run() {
                if (!isAdded) return
                if (!::sensorsAdapter.isInitialized) {
                    uiHandler.postDelayed(this, uiUpdateIntervalMs)
                    return
                }
                // Off-main snapshot & formatting, then diff submit
                coroutineScope.launch(Dispatchers.Default) {
                    val currentList = sensorsAdapter.currentList
                    if (currentList.isEmpty()) {
                        withContext(Dispatchers.Main) { uiHandler.postDelayed(uiUpdateRunnable!!, uiUpdateIntervalMs) }
                        return@launch
                    }
                    var changed = false
                    val updated = ArrayList<SensorItem>(currentList.size)
                    currentList.forEach { item ->
                        val raw = sensorValues[item.sensorTypeInt]
                        if (raw != null) {
                            val formatted = formatValues(item.sensorTypeInt, raw)
                            val hash = raw.fold(1) { acc, f -> (31 * acc + (f * 100).roundToInt()) }
                            if (formatted != item.values || hash != item.valueHash) {
                                updated.add(item.copy(values = formatted, valueHash = hash))
                                changed = true
                            } else updated.add(item)
                        } else updated.add(item)
                    }
                    if (changed) {
                        withContext(Dispatchers.Main) { sensorsAdapter.submitList(updated) }
                    }
                }
                uiHandler.postDelayed(this, uiUpdateIntervalMs)
            }
        }
        uiHandler.post(uiUpdateRunnable!!)
    }

    override fun onResume() {
        super.onResume()
        registerSensorListeners() // guarded by flag; UI updates will auto-start after adapter ready
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        listenersRegistered = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager.unregisterListener(this)
        listenersRegistered = false
        uiUpdateRunnable?.let { uiHandler.removeCallbacks(it) }
        uiUpdateRunnable = null
        coroutineScope.cancel()
        sensorValues.clear()
        // Stop background thread
        sensorHandlerThread?.quitSafely()
        sensorHandlerThread = null
        sensorBgHandler = null
    }
}
