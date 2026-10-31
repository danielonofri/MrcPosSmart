package com.mrcomanda.possmart

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mrcomanda.possmart.network.AppPreferences
import com.mrcomanda.possmart.ui.theme.MesasScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.init(applicationContext)
        enableEdgeToEdge()

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContainer()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer() {
    var pantallaActual by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("cuentas") }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (pantallaActual == "cuentas") "Cuentas" else "Configuración")
                },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Text("≡", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "CUENTAS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                },
                                onClick = {
                                    pantallaActual = "cuentas"
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "CONFIG",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                },
                                onClick = {
                                    pantallaActual = "config"
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (pantallaActual == "cuentas") {
                MesasScreen()
            } else {
                ConfigScreen()
            }
        }
    }
}
// ==========================================
// CONFIGURACIÓN Y BLUETOOTH AUTOMÁTICO
// ==========================================

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
            } else {
                launcherPermiso.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
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
            label = { Text("Impresora Bluetooth Externa") },
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

@Composable
fun ConfigScreen() {
    var urlTexto by remember { mutableStateOf(AppPreferences.baseUrl) }
    var keyTexto by remember { mutableStateOf(AppPreferences.apiKey) }
    var macTexto by remember { mutableStateOf(AppPreferences.macImpresora) }
    var smartPosCheck by remember { mutableStateOf(AppPreferences.esSmartPos) }
    var mensajeGuardado by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = urlTexto,
            onValueChange = { urlTexto = it },
            label = { Text("Base URL (ej: https://192.168.1.50:5006/)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = keyTexto,
            onValueChange = { keyTexto = it },
            label = { Text("X-API-Key") },
            modifier = Modifier.fillMaxWidth()
        )

        SelectorImpresoraBluetooth(
            context = context,
            macSeleccionadaActual = macTexto,
            onMacGuardada = { nuevaMac ->
                macTexto = nuevaMac
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = smartPosCheck,
                onCheckedChange = { smartPosCheck = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("¿Ejecutando en Smart POS físico?")
        }

        Button(
            onClick = {
                AppPreferences.baseUrl = urlTexto.trim()
                AppPreferences.apiKey = keyTexto.trim()
                AppPreferences.macImpresora = macTexto.trim()
                AppPreferences.esSmartPos = smartPosCheck
                mensajeGuardado = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Cambios")
        }

        if (mensajeGuardado) {
            Text(
                text = "¡Configuración guardada con éxito!",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}