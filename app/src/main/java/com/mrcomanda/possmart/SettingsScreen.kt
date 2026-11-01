package com.mrcomanda.possmart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mrcomanda.possmart.network.AppPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados locales sincronizados con AppPreferences
    var baseUrl by remember { mutableStateOf(AppPreferences.baseUrl) }
    var apiKey by remember { mutableStateOf(AppPreferences.apiKey) }
    var macActual by remember { mutableStateOf(AppPreferences.macImpresora) }
    var esSmartPos by remember { mutableStateOf(AppPreferences.esSmartPos) }

    var oauthActivado by remember { mutableStateOf(AppPreferences.oauth) }
    var integracion by remember { mutableStateOf(AppPreferences.integracion) }
    var licencia by remember { mutableStateOf(AppPreferences.licencia) }
    var cuit by remember { mutableStateOf(AppPreferences.cuit) }
    var tokenMrc by remember { mutableStateOf(AppPreferences.tokenMrc) }
    var urlTokenMp by remember { mutableStateOf(AppPreferences.urlTokenMp) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configuración del sistema",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // SECCIÓN 1: CONEXIÓN Y GENERALES
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Conexión y servidor",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { valor ->
                            baseUrl = valor
                            AppPreferences.baseUrl = valor
                        },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { valor ->
                            apiKey = valor
                            AppPreferences.apiKey = valor
                        },
                        label = { Text("X-API-Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿Ejecutando en Smart POS físico?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Checkbox(
                            checked = esSmartPos,
                            onCheckedChange = { checked ->
                                esSmartPos = checked
                                AppPreferences.esSmartPos = checked
                            }
                        )
                    }
                }
            }

            // SECCIÓN 2: IMPRESORA BLUETOOTH
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Impresora Térmica",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Elija la impresora vinculada al dispositivo:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    SelectorImpresoraBluetooth(
                        context = context,
                        macSeleccionadaActual = macActual,
                        onMacGuardada = { mac: String ->
                            AppPreferences.macImpresora = mac
                            macActual = mac
                        }
                    )

                    Text(
                        text = "MAC configurada:\n$macActual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // SECCIÓN 3: INTEGRACIÓN OAUTH / MERCADO PAGO
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Integración Mercado Pago / OAuth",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Habilitar OAuth",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = oauthActivado,
                            onCheckedChange = { nuevoEstado ->
                                oauthActivado = nuevoEstado
                                AppPreferences.oauth = nuevoEstado
                            }
                        )
                    }

                    if (oauthActivado) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        OutlinedTextField(
                            value = integracion,
                            onValueChange = { valor ->
                                integracion = valor
                                AppPreferences.integracion = valor
                            },
                            label = { Text("Integración") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = licencia,
                            onValueChange = { valor ->
                                licencia = valor
                                AppPreferences.licencia = valor
                            },
                            label = { Text("Licencia") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = cuit,
                            onValueChange = { valor ->
                                cuit = valor
                                AppPreferences.cuit = valor
                            },
                            label = { Text("CUIT") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Token MRC: Siempre enmascarado como contraseña, editable y sin requerir íconos
                        OutlinedTextField(
                            value = tokenMrc,
                            onValueChange = { valor ->
                                tokenMrc = valor
                                AppPreferences.tokenMrc = valor
                            },
                            label = { Text("Token MRC") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        OutlinedTextField(
                            value = urlTokenMp,
                            onValueChange = { valor ->
                                urlTokenMp = valor
                                AppPreferences.urlTokenMp = valor
                            },
                            label = { Text("URL Token MP") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}