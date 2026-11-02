package com.mrcomanda.possmart

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mrcomanda.possmart.network.AppPreferences
import com.mrcomanda.possmart.ui.theme.CobroDirectoScreen
import com.mrcomanda.possmart.ui.theme.MesasScreen
import com.mrcomanda.possmart.ui.theme.OrdenesDiaScreen

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

    val tituloBarra = when (pantallaActual) {
        "cuentas" -> "Cuentas abiertas"
        "cobro_libre" -> "Cobro Directo (Point)"
        "ordenes_dia" -> "Órdenes del Día"
        else -> "Configuración"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloBarra) },
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
                                text = { Text("CUENTAS ABIERTAS", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                onClick = { pantallaActual = "cuentas"; menuExpanded = false }
                            )
                            // Nueva opción de menú para cobro libre
                            DropdownMenuItem(
                                text = { Text("COBRO DIRECTO", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                onClick = { pantallaActual = "cobro_libre"; menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("CONFIGURACIONES", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                onClick = { pantallaActual = "config"; menuExpanded = false }
                            )
                            if (AppPreferences.oauth) {
                                DropdownMenuItem(
                                    text = { Text("ÓRDENES DEL DÍA", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                    onClick = { pantallaActual = "ordenes_dia"; menuExpanded = false }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (pantallaActual) {
                "cuentas" -> MesasScreen()
                "cobro_libre" -> CobroDirectoScreen()
                "ordenes_dia" -> OrdenesDiaScreen() // (Esta pantalla la crearemos a continuación)
                else -> ConfigScreen()
            }
        }
    }
}
//fun MainContainer() {
//    var pantallaActual by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("cuentas") }
//    var menuExpanded by remember { mutableStateOf(false) }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(if (pantallaActual == "cuentas") "Cuentas" else "Configuración")
//                },
//                navigationIcon = {
//                    Box {
//                        IconButton(onClick = { menuExpanded = true }) {
//                            Text("≡", fontSize = 28.sp, fontWeight = FontWeight.Bold)
//                        }
//                        DropdownMenu(
//                            expanded = menuExpanded,
//                            onDismissRequest = { menuExpanded = false }
//                        ) {
//                            DropdownMenuItem(
//                                text = {
//                                    Text(
//                                        "CUENTAS",
//                                        fontWeight = FontWeight.Bold,
//                                        fontSize = 16.sp
//                                    )
//                                },
//                                onClick = {
//                                    pantallaActual = "cuentas"
//                                    menuExpanded = false
//                                }
//                            )
//                            DropdownMenuItem(
//                                text = {
//                                    Text(
//                                        "CONFIG",
//                                        fontWeight = FontWeight.Bold,
//                                        fontSize = 16.sp
//                                    )
//                                },
//                                onClick = {
//                                    pantallaActual = "config"
//                                    menuExpanded = false
//                                }
//                            )
//                        }
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
//            if (pantallaActual == "cuentas") {
//                MesasScreen()
//            } else {
//                ConfigScreen()
//            }
//        }
//    }
//}
// ==========================================
// CONFIGURACIÓN Y BLUETOOTH AUTOMÁTICO
// ==========================================
@Suppress("MissingPermission")
fun obtenerDispositivosBluetoothVinculados(context: Context): Map<String, String> {
    val listaDispositivos = mutableMapOf<String, String>()

    // Obtenemos el adaptador usando el BluetoothManager moderno
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
    val bluetoothAdapter = bluetoothManager?.adapter

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
    var nombreSeleccionado by remember { mutableStateOf("Elija una impresora") }

    val launcherPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) {
            dispositivos = obtenerDispositivosBluetoothVinculados(context)
        }
    }

    LaunchedEffect(Unit) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            dispositivos = obtenerDispositivosBluetoothVinculados(context)
        } else {
            launcherPermiso.launch(Manifest.permission.BLUETOOTH_CONNECT)
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
            //modifier = Modifier.menuAnchor().fillMaxWidth()
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
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
    // Reutilizamos directamente la pantalla completa y prolija que diseñamos
    SettingsScreen(
        onBackClick = {}
    )
}
