package com.conelab.basictestingapp

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.conelab.basictestingapp.ui.theme.BasicTestingAppTheme
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.conelab.basictestingapp.georeference.Georeference
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BasicTestingAppTheme {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun MainScreen() {
        //Estado para autenticacion
        var isAuthInProgress = remember { mutableStateOf(false) }


        var showDialog = remember { mutableStateOf(false) }
        // Estado para la ubicación
        var location = remember { mutableStateOf<Location?>(null) }
        val georeference = remember { Georeference(context = this) }

        // Pedir permisos de ubicación
        val locationPermissionState = rememberMultiplePermissionsState(
            permissions = listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )


        LaunchedEffect(Unit) {
            locationPermissionState.launchMultiplePermissionRequest()
        }


        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (locationPermissionState.allPermissionsGranted) {
                            georeference.requestLocation()
                            location = georeference.location
                            showDialog.value = true
                        } else {
                            openAppSettings()
                        }},
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Georeferencia")
                }

                Button(onClick = {openAppSettings()},
                    modifier = Modifier.padding(bottom = 16.dp)
                ){
                    Text(text = "Pedir permiso")
                }
                Button(onClick = {
                    isAuthInProgress.value = true
                    checkFingerprintAuthentication { isAuthInProgress.value = false }
                }) {
                    Text(text = "Lector de huellas")
                }
            }

            if (showDialog.value) {
                showAlertDialog(location.value){
                    showDialog.value = false
                }
//                showDialog.value = false
            }
        }
    }

    @Composable
    fun showAlertDialog(location: Location?, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Ubicación") },
            text = {
                Text(
                    text = location?.let {
                        "Latitud: ${it.latitude}\nLongitud: ${it.longitude}"
                    } ?: "Ubicación no disponible"
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { // Llama a onDismiss para cerrar el cuadro de diálogo
                    Text("OK")
                }
            }
        )
    }

    //Llamado a la pantalla de ajustes
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package",
            packageName, null)
        intent.data = uri
        startActivity(intent)

    }


    //Huella dactilar


    //Revisar Hardware
    private fun checkFingerprintAuthentication(onComplete: () -> Unit) {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showFingerprintPrompt(onComplete)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.w("Fingerprint", "No fingerprint sensor available")
                Toast.makeText(this, "No fingerprint sensor available", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.w("Fingerprint", "Fingerprint sensor unavailable")
                Toast.makeText(this, "Fingerprint sensor unavailable", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            else -> {
                Log.w("Fingerprint", "Fingerprint authentication not supported")
                Toast.makeText(this, "Fingerprint authentication not supported", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        }
    }


    //Autenticacion
    private fun showFingerprintPrompt(onComplete: () -> Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Lector de huellas")
            .setDescription("LECTOR")
            .setNegativeButtonText("CANCEL")
            .build()

        val biometricPrompt = BiometricPrompt(this as FragmentActivity, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.i("Fingerprint", "Authentication successful")
                    Toast.makeText(this@MainActivity, "Authentication successful", Toast.LENGTH_SHORT).show()
                    onComplete()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e("Fingerprint", "Authentication error: $errString")
                    Toast.makeText(this@MainActivity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    onComplete()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w("Fingerprint", "Authentication failed")
                    onComplete()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }



}


