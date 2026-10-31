package com.comanda.possmart // Reemplazá con tu paquete real

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mrcomanda.possmart.network.AppPreferences

@Suppress("MissingPermission")
fun obtenerDispositivosBluetoothVinculados(): Map<String, String> {
    val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
    val listaDispositivos = mutableMapOf<String, String>()

    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
        val emparejados = bluetoothAdapter.bondedDevices
        emparejados?.forEach { device ->
            val nombre = device.name ?: "Desconocido"
            val mac = device.address
            listaDispositivos[nombre] = mac
        }
    }
    return listaDispositivos
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var macActual by remember { mutableStateOf(AppPreferences.macImpresora) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configuración de Impresora",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Seleccione la impresora térmica vinculada al dispositivo:",
                style = MaterialTheme.typography.bodyMedium
            )

            // Componente selector de impresora Bluetooth
            SelectorImpresoraBluetooth(
                context = context,
                macSeleccionadaActual = macActual,
                onMacGuardada = { nuevaMac ->
                    AppPreferences.macImpresora = nuevaMac
                    macActual = nuevaMac
                }
            )

            Text(
                text = "MAC configurada actual:\n$macActual",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorImpresoraBluetooth(
    context: Context,
    macSeleccionadaActual: String,
    onMacGuardada: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var dispositivos by remember { mutableStateOf(mapOf<String, String>()) }
    var nombreSeleccionado by remember { mutableStateOf("Seleccione una impresora") }

    val launcherPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) {
            dispositivos = obtenerDispositivosBluetoothVinculados()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                dispositivos = obtenerDispositivosBluetoothVinculados()
            } /*else {
                launcherPermiso.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }*/
        } else {
            dispositivos = obtenerDispositivosBluetoothVinculados()
        }

        val encontrado = dispositivos.entries.find { it.value == macSeleccionadaActual }
        if (encontrado != null) {
            nombreSeleccionado = encontrado.key
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = nombreSeleccionado,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (dispositivos.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay dispositivos vinculados") },
                    onClick = { expanded = false }
                )
            } else {
                dispositivos.forEach { (nombre, mac) ->
                    DropdownMenuItem(
                        text = { Text(nombre) },
                        onClick = {
                            nombreSeleccionado = nombre
                            onMacGuardada(mac)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}