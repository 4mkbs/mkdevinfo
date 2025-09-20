package com.sakib.devinfo.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sakib.devinfo.R
import kotlinx.coroutines.*

class CameraFragment : Fragment() {

    private lateinit var tvCameraSupport: TextView
    private lateinit var tvCameraCount: TextView
    private lateinit var tvFrontCamera: TextView
    private lateinit var tvBackCamera: TextView
    private lateinit var tvFlashSupport: TextView
    private lateinit var tvAutoFocus: TextView
    private lateinit var tvZoomSupport: TextView
    private lateinit var tvVideoRecording: TextView
    private lateinit var tvBackCameraResolution: TextView
    private lateinit var tvBackCameraAperture: TextView
    private lateinit var tvBackCameraFocal: TextView
    private lateinit var tvBackCameraISO: TextView
    private lateinit var tvFrontCameraResolution: TextView
    private lateinit var tvFrontCameraAperture: TextView
    private lateinit var tvFrontCameraFocal: TextView
    private lateinit var tvFrontCameraFeatures: TextView
    private lateinit var tvVideoResolution: TextView
    private lateinit var tvVideoFrameRate: TextView
    private lateinit var tvVideoStabilization: TextView
    private lateinit var tvSlowMotion: TextView
    private lateinit var tvCameraAPI: TextView
    private lateinit var tvCamera2Support: TextView
    private lateinit var tvCameraHardwareLevel: TextView
    private lateinit var tvCameraPermission: TextView

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_camera, container, false)
        
        initViews(root)
        loadCameraInformation()
        
        return root
    }

    private fun initViews(root: View) {
        tvCameraSupport = root.findViewById(R.id.tvCameraSupport)
        tvCameraCount = root.findViewById(R.id.tvCameraCount)
        tvFrontCamera = root.findViewById(R.id.tvFrontCamera)
        tvBackCamera = root.findViewById(R.id.tvBackCamera)
        tvFlashSupport = root.findViewById(R.id.tvFlashSupport)
        tvAutoFocus = root.findViewById(R.id.tvAutoFocus)
        tvZoomSupport = root.findViewById(R.id.tvZoomSupport)
        tvVideoRecording = root.findViewById(R.id.tvVideoRecording)
        tvBackCameraResolution = root.findViewById(R.id.tvBackCameraResolution)
        tvBackCameraAperture = root.findViewById(R.id.tvBackCameraAperture)
        tvBackCameraFocal = root.findViewById(R.id.tvBackCameraFocal)
        tvBackCameraISO = root.findViewById(R.id.tvBackCameraISO)
        tvFrontCameraResolution = root.findViewById(R.id.tvFrontCameraResolution)
        tvFrontCameraAperture = root.findViewById(R.id.tvFrontCameraAperture)
        tvFrontCameraFocal = root.findViewById(R.id.tvFrontCameraFocal)
        tvFrontCameraFeatures = root.findViewById(R.id.tvFrontCameraFeatures)
        tvVideoResolution = root.findViewById(R.id.tvVideoResolution)
        tvVideoFrameRate = root.findViewById(R.id.tvVideoFrameRate)
        tvVideoStabilization = root.findViewById(R.id.tvVideoStabilization)
        tvSlowMotion = root.findViewById(R.id.tvSlowMotion)
        tvCameraAPI = root.findViewById(R.id.tvCameraAPI)
        tvCamera2Support = root.findViewById(R.id.tvCamera2Support)
        tvCameraHardwareLevel = root.findViewById(R.id.tvCameraHardwareLevel)
        tvCameraPermission = root.findViewById(R.id.tvCameraPermission)
    }

    private fun loadCameraInformation() {
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    checkCameraPermission()
                    checkBasicCameraInfo()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        checkCamera2Info()
                    } else {
                        checkLegacyCameraInfo()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvCameraSupport.text = "Camera Support: Error checking"
                }
            }
        }
    }

    private suspend fun checkCameraPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        withContext(Dispatchers.Main) {
            tvCameraPermission.text = "Camera Permission: ${if (hasPermission) "Granted" else "Not Granted"}"
        }
    }

    private suspend fun checkBasicCameraInfo() {
        val packageManager = requireContext().packageManager
        
        val hasCameraFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        val hasCameraAny = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        val hasFrontCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
        val hasFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        val hasAutoFocus = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)

        withContext(Dispatchers.Main) {
            tvCameraSupport.text = "Camera Support: ${if (hasCameraFeature || hasCameraAny) "Yes" else "No"}"
            tvFrontCamera.text = "Front Camera: ${if (hasFrontCamera) "Available" else "Not Available"}"
            tvBackCamera.text = "Back Camera: ${if (hasCameraFeature) "Available" else "Not Available"}"
            tvFlashSupport.text = "Flash Support: ${if (hasFlash) "Yes" else "No"}"
            tvAutoFocus.text = "Auto Focus: ${if (hasAutoFocus) "Yes" else "No"}"
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun checkLegacyCameraInfo() {
        try {
            val numberOfCameras = Camera.getNumberOfCameras()
            
            withContext(Dispatchers.Main) {
                tvCameraCount.text = "Number of Cameras: $numberOfCameras"
                tvCameraAPI.text = "Camera API Level: Legacy Camera API"
                tvCamera2Support.text = "Camera2 API Support: Not Available (API < 21)"
                tvCameraHardwareLevel.text = "Hardware Level: Legacy"
                
                // Basic video recording check
                val packageManager = requireContext().packageManager
                val hasVideoRecording = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                tvVideoRecording.text = "Video Recording: ${if (hasVideoRecording) "Supported" else "Not Supported"}"
                
                // Set basic values for legacy cameras
                tvZoomSupport.text = "Zoom Support: Basic"
                tvBackCameraResolution.text = "Max Resolution: Standard (API limitations)"
                tvFrontCameraResolution.text = "Max Resolution: Standard (API limitations)"
                tvVideoResolution.text = "Max Video Resolution: Standard"
                tvVideoFrameRate.text = "Frame Rates: 30 FPS"
                tvVideoStabilization.text = "Video Stabilization: Not Available"
                tvSlowMotion.text = "Slow Motion: Not Available"
                tvBackCameraAperture.text = "Aperture: Not Available (API < 21)"
                tvFrontCameraAperture.text = "Aperture: Not Available (API < 21)"
                tvBackCameraFocal.text = "Focal Length: Not Available (API < 21)"
                tvFrontCameraFocal.text = "Focal Length: Not Available (API < 21)"
                tvBackCameraISO.text = "ISO Range: Not Available (API < 21)"
                tvFrontCameraFeatures.text = "Special Features: Basic"
            }
            
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                tvCameraCount.text = "Number of Cameras: Error checking"
            }
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun checkCamera2Info() {
        try {
            val cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIds = cameraManager.cameraIdList

            withContext(Dispatchers.Main) {
                tvCameraCount.text = "Number of Cameras: ${cameraIds.size}"
                tvCameraAPI.text = "Camera API Level: Camera2 API"
                tvCamera2Support.text = "Camera2 API Support: Available"
            }

            if (cameraIds.isNotEmpty()) {
                var backCameraId: String? = null
                var frontCameraId: String? = null

                // Find back and front cameras
                for (cameraId in cameraIds) {
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    
                    when (facing) {
                        CameraCharacteristics.LENS_FACING_BACK -> backCameraId = cameraId
                        CameraCharacteristics.LENS_FACING_FRONT -> frontCameraId = cameraId
                    }
                }

                // Check hardware level
                backCameraId?.let { cameraId ->
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    checkCameraHardwareLevel(characteristics)
                    checkBackCameraDetails(characteristics)
                    checkVideoCapabilities(characteristics)
                }

                frontCameraId?.let { cameraId ->
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    checkFrontCameraDetails(characteristics)
                }

                // Check general features
                checkCameraFeatures(cameraManager, cameraIds)
            }

        } catch (e: CameraAccessException) {
            withContext(Dispatchers.Main) {
                tvCameraCount.text = "Number of Cameras: Access denied"
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                tvCameraCount.text = "Number of Cameras: Error checking"
            }
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun checkCameraHardwareLevel(characteristics: CameraCharacteristics) {
        val hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        val levelString = when (hardwareLevel) {
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3"
            else -> "Unknown"
        }

        withContext(Dispatchers.Main) {
            tvCameraHardwareLevel.text = "Hardware Level: $levelString"
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun checkBackCameraDetails(characteristics: CameraCharacteristics) {
        try {
            // Resolution
            val streamConfigMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = streamConfigMap?.getOutputSizes(android.graphics.ImageFormat.JPEG)
            val maxSize = sizes?.maxByOrNull { it.width * it.height }
            val resolution = maxSize?.let { "${it.width} x ${it.height}" } ?: "Unknown"

            // Aperture
            val apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
            val apertureString = apertures?.joinToString(", ") { "f/${it}" } ?: "Unknown"

            // Focal length
            val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            val focalString = focalLengths?.joinToString(", ") { "${it}mm" } ?: "Unknown"

            // ISO range
            val isoRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            val isoString = isoRange?.let { "${it.lower} - ${it.upper}" } ?: "Unknown"

            withContext(Dispatchers.Main) {
                tvBackCameraResolution.text = "Max Resolution: $resolution"
                tvBackCameraAperture.text = "Aperture: $apertureString"
                tvBackCameraFocal.text = "Focal Length: $focalString"
                tvBackCameraISO.text = "ISO Range: $isoString"
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                tvBackCameraResolution.text = "Max Resolution: Error checking"
                tvBackCameraAperture.text = "Aperture: Error checking"
                tvBackCameraFocal.text = "Focal Length: Error checking"
                tvBackCameraISO.text = "ISO Range: Error checking"
            }
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun checkFrontCameraDetails(characteristics: CameraCharacteristics) {
        try {
            // Resolution
            val streamConfigMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = streamConfigMap?.getOutputSizes(android.graphics.ImageFormat.JPEG)
            val maxSize = sizes?.maxByOrNull { it.width * it.height }
            val resolution = maxSize?.let { "${it.width} x ${it.height}" } ?: "Unknown"

            // Aperture
            val apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
            val apertureString = apertures?.joinToString(", ") { "f/${it}" } ?: "Unknown"

            // Focal length
            val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            val focalString = focalLengths?.joinToString(", ") { "${it}mm" } ?: "Unknown"

            // Special features
            val features = mutableListOf<String>()
            val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
            if (flashAvailable) features.add("Flash")
            
            val stabilization = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
            if (stabilization?.isNotEmpty() == true) features.add("OIS")

            val featuresString = if (features.isNotEmpty()) features.joinToString(", ") else "None"

            withContext(Dispatchers.Main) {
                tvFrontCameraResolution.text = "Max Resolution: $resolution"
                tvFrontCameraAperture.text = "Aperture: $apertureString"
                tvFrontCameraFocal.text = "Focal Length: $focalString"
                tvFrontCameraFeatures.text = "Special Features: $featuresString"
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                tvFrontCameraResolution.text = "Max Resolution: Error checking"
                tvFrontCameraAperture.text = "Aperture: Error checking"
                tvFrontCameraFocal.text = "Focal Length: Error checking"
                tvFrontCameraFeatures.text = "Special Features: Error checking"
            }
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun checkVideoCapabilities(characteristics: CameraCharacteristics) {
        try {
            val streamConfigMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            
            // Video resolution
            val videoSizes = streamConfigMap?.getOutputSizes(android.media.MediaRecorder::class.java)
            val maxVideoSize = videoSizes?.maxByOrNull { it.width * it.height }
            val videoResolution = maxVideoSize?.let { "${it.width} x ${it.height}" } ?: "Unknown"

            // Frame rates
            val frameRates = streamConfigMap?.getOutputMinFrameDuration(android.media.MediaRecorder::class.java, maxVideoSize ?: Size(1920, 1080))
            val fps = frameRates?.let { 1_000_000_000L / it } ?: 30L

            // Video stabilization
            val videoStabilizationModes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
            val hasVideoStabilization = videoStabilizationModes?.contains(CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON) ?: false

            // High speed video (slow motion)
            val highSpeedSizes = streamConfigMap?.highSpeedVideoSizes
            val hasSlowMotion = !highSpeedSizes.isNullOrEmpty()

            withContext(Dispatchers.Main) {
                tvVideoResolution.text = "Max Video Resolution: $videoResolution"
                tvVideoFrameRate.text = "Frame Rates: Up to ${fps} FPS"
                tvVideoStabilization.text = "Video Stabilization: ${if (hasVideoStabilization) "Available" else "Not Available"}"
                tvSlowMotion.text = "Slow Motion: ${if (hasSlowMotion) "Supported" else "Not Supported"}"
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                tvVideoResolution.text = "Max Video Resolution: Error checking"
                tvVideoFrameRate.text = "Frame Rates: Error checking"
                tvVideoStabilization.text = "Video Stabilization: Error checking"
                tvSlowMotion.text = "Slow Motion: Error checking"
            }
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun checkCameraFeatures(cameraManager: CameraManager, cameraIds: Array<String>) {
        try {
            var hasZoom = false
            var hasVideoRecording = false

            for (cameraId in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                
                // Check zoom
                val maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
                if (maxZoom != null && maxZoom > 1.0f) {
                    hasZoom = true
                }

                // Check video recording
                val streamConfigMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val videoSizes = streamConfigMap?.getOutputSizes(android.media.MediaRecorder::class.java)
                if (!videoSizes.isNullOrEmpty()) {
                    hasVideoRecording = true
                }
            }

            withContext(Dispatchers.Main) {
                tvZoomSupport.text = "Zoom Support: ${if (hasZoom) "Digital zoom available" else "No zoom support"}"
                tvVideoRecording.text = "Video Recording: ${if (hasVideoRecording) "Supported" else "Not Supported"}"
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                tvZoomSupport.text = "Zoom Support: Error checking"
                tvVideoRecording.text = "Video Recording: Error checking"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }
}
