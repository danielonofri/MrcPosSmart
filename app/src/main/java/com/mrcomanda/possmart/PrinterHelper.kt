package com.mrcomanda.possmart

import android.content.Context
import com.mrcomanda.possmart.model.ItemFacturaDto
import com.mrcomanda.possmart.network.AppPreferences
import com.mrcomanda.possmart.model.Mesa
object PrinterHelper {

    // Configuración general de entorno
    private const val ESTOY_EN_SMART_POS = false
    private const val MAC_IMPRESORA_EXTERNA = "02:18:99:0C:00:39" //[cite: 5]

    fun generarTextoPrecuenta(items: List<ItemFacturaDto>, total: Double, numeroMesa: Int): String {
        val sb = StringBuilder()
        sb.append("--------------------------------\n")
        sb.append("         MR.COMANDA POS         \n")
        sb.append("--------------------------------\n")
        sb.append("Mesa: $numeroMesa\n")
        sb.append("Fecha: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n")
        sb.append("--------------------------------\n")
        sb.append(String.format("%-20s %3s %7s\n", "DESCRIPCION", "CAN", "PRECIO"))
        sb.append("--------------------------------\n")

        for (item in items) {
            val desc = if (item.descrip.length > 20) item.descrip.substring(0, 20) else item.descrip
            val cant = item.cantidad.toInt().toString()
            val precio = item.precio.toInt().toString()
            sb.append(String.format("%-20s %3s %7s\n", desc, cant, precio))
        }

        sb.append("--------------------------------\n")
        sb.append(String.format("TOTAL A PAGAR: $%16.2f\n", total))
        sb.append("--------------------------------\n")
        sb.append("    ¡ Gracias por su visita !     \n\n\n")

        return sb.toString()
    }

    fun imprimirPrecuenta(
        context: Context,
        items: List<ItemFacturaDto>,
        total: Double,
        numeroMesa: Int
    ) {
        val textoTicket = generarTextoPrecuenta(items, total, numeroMesa)

        // Log para ver el estado del selector en consola
        android.util.Log.d("PrinterHelper", "esSmartPos actual: ${AppPreferences.esSmartPos}")
        android.util.Log.d("PrinterHelper", "MAC configurada: ${AppPreferences.macImpresora}")

        if (AppPreferences.esSmartPos) {
            android.util.Log.d("PrinterHelper", "Ruta elegida: Smart POS NATIVO")
            try {
                // Invocación del SDK nativo para impresión integrada
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
//            android.util.Log.d("PrinterHelper", "Ruta elegida: Bluetooth Externo")
//            enviarAEscPosExterna(textoTicket, AppPreferences.macImpresora)

            android.util.Log.d("PrinterHelper", "Ruta elegida: Bluetooth por Búsqueda Dinámica")
            enviarAEscPosExternaSinMac(textoTicket)
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    fun enviarAEscPosExterna(texto: String, macAddress: String) {
        Thread {
            try {
                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return@Thread

                val device = bluetoothAdapter.getRemoteDevice(macAddress)
                val uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                val socket = device.createInsecureRfcommSocketToServiceRecord(uuid)

                // ELIMINAR O COMENTAR ESTA LÍNEA QUE PIDE EL PERMISO DE ESCANEO:
                // bluetoothAdapter.cancelDiscovery()

                socket.connect()

                val outputStream = socket.outputStream
                outputStream.write(texto.toByteArray(charset("CP850")))
                outputStream.flush()
                outputStream.close()
                socket.close()
                android.util.Log.d("PrinterHelper", "¡Ticket impreso con éxito!")
            } catch (e: Exception) {
                android.util.Log.e("PrinterHelper", "Error: ${e.message}", e)
            }
        }.start()
    }

    @android.annotation.SuppressLint("MissingPermission")
    fun enviarAEscPosExternaSinMac(texto: String) {
        Thread {
            try {
                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                    android.util.Log.e("PrinterHelper", "Bluetooth apagado o no disponible")
                    return@Thread
                }

                val dispositivosEmparejados: Set<android.bluetooth.BluetoothDevice>? = bluetoothAdapter.bondedDevices
                if (dispositivosEmparejados.isNullOrEmpty()) {
                    android.util.Log.e("PrinterHelper", "No hay dispositivos Bluetooth emparejados en el celular")
                    return@Thread
                }

                // Búsqueda dinámica por nombre igual que en el proyecto de prueba
                var impresora: android.bluetooth.BluetoothDevice? = null
                for (device in dispositivosEmparejados) {
                    //android.util.Log.e("PrinterHelperx", device.name)
                    if (device.name.contains("POS", ignoreCase = true) ||
                        device.name.contains("Printer", ignoreCase = true) ||
                        device.name.contains("Prin", ignoreCase = true) ||
                        device.name.contains("FK", ignoreCase = true)) {
                        impresora = device
                        android.util.Log.e("PrinterHelperx", impresora.name)
                        break
                    }
                }

                // Si no encuentra por nombre, toma el primer dispositivo disponible de la lista[cite: 5]
                if (impresora == null) {
                    impresora = dispositivosEmparejados.first()
                }

                android.util.Log.d("PrinterHelper", "Dispositivo seleccionado por búsqueda: ${impresora.name}")

                val uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                val socket = impresora.createInsecureRfcommSocketToServiceRecord(uuid)

                socket.connect()

                val outputStream = socket.outputStream
                outputStream.write(texto.toByteArray(charset("CP850")))
                outputStream.flush()
                outputStream.close()
                socket.close()
                android.util.Log.d("PrinterHelper", "¡Ticket impreso por búsqueda dinámica con éxito!")
            } catch (e: Exception) {
                android.util.Log.e("PrinterHelper", "Error en búsqueda y conexión: ${e.message}", e)
            }
        }.start()
    }
    fun imprimirPrecuentaPrinterIntegrada(
        context: Context,
        items: List<ItemFacturaDto>,
        total: Double,
        numeroMesa: Int,
        leyendaExtra: String? = null
    ) {
        try {
            val bitmap = crearBitmapPrecuentaPrinterIntegrada(
                numeroMesa = numeroMesa,
                items = items,
                total = total,
                leyendaExtra = leyendaExtra
            )
            enviarBitmapPrinterIntegrada(bitmap, "precuenta mesa $numeroMesa")
        } catch (e: Exception) {
            android.util.Log.e("MrComandaLog", "Error al imprimir pre-cuenta en PrinterIntegrada", e)
        }
    }

    fun imprimirMesasAbiertasPrinterIntegrada(
        context: Context,
        mesas: List<Mesa>
    ) {
        try {
            val bitmap = crearBitmapMesasAbiertasPrinterIntegrada(mesas)
            enviarBitmapPrinterIntegrada(bitmap, "listado mesas abiertas (${mesas.size})")
        } catch (e: Exception) {
            android.util.Log.e("MrComandaLog", "Error al imprimir mesas abiertas en PrinterIntegrada", e)
        }
    }

    private fun enviarBitmapPrinterIntegrada(bitmap: android.graphics.Bitmap, descripcion: String) {
        android.util.Log.d("MrComandaLog", "Bitmap PrinterIntegrada listo [$descripcion]: ${bitmap.width}x${bitmap.height}")
        // Ajustar con el método exacto decompilado de BitmapPrinter (Ctrl+N):
        // MPManager.bitmapPrinter.print(bitmap) { response ->
        //     android.util.Log.d("MrComandaLog", "<- Respuesta BitmapPrinter [$descripcion]: $response")
        // }
    }

    private fun crearBitmapPrecuentaPrinterIntegrada(
        numeroMesa: Int,
        items: List<ItemFacturaDto>,
        total: Double,
        leyendaExtra: String?
    ): android.graphics.Bitmap {
        val paintTitulo = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }
        val paintTexto = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
        }

        val alto = (180 + items.size * 44 + if (leyendaExtra != null) 36 else 0).coerceAtLeast(240)
        val bitmap = android.graphics.Bitmap.createBitmap(384, alto, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap).apply { drawColor(android.graphics.Color.WHITE) }

        var y = 32f
        canvas.drawText("MR. COMANDA - PRE-CUENTA", 12f, y, paintTitulo)
        y += 32f
        canvas.drawText("Mesa: $numeroMesa", 12f, y, paintTexto)
        y += 28f
        canvas.drawText("--------------------------------", 12f, y, paintTexto)
        y += 28f

        for (item in items) {
            canvas.drawText("${item.cantidad}x ${item.descrip}".take(28), 12f, y, paintTexto)
            y += 22f
            canvas.drawText("   $${item.precio}", 12f, y, paintTexto)
            y += 24f
        }

        canvas.drawText("--------------------------------", 12f, y, paintTexto)
        y += 30f
        canvas.drawText("TOTAL: $${total.toInt()}", 12f, y, paintTitulo)

        if (!leyendaExtra.isNullOrBlank()) {
            y += 32f
            canvas.drawText(leyendaExtra.take(30), 12f, y, paintTexto)
        }

        return bitmap
    }

    private fun crearBitmapMesasAbiertasPrinterIntegrada(mesas: List<Mesa>): android.graphics.Bitmap {
        val paintTitulo = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 22f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }
        val paintTexto = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 17f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
        }

        val totalGeneral = mesas.sumOf { it.total }
        val alto = (170 + mesas.size * 28).coerceAtLeast(220)
        val bitmap = android.graphics.Bitmap.createBitmap(384, alto, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap).apply { drawColor(android.graphics.Color.WHITE) }

        var y = 32f
        canvas.drawText("MESAS ABIERTAS", 12f, y, paintTitulo)
        y += 30f
        canvas.drawText("--------------------------------", 12f, y, paintTexto)
        y += 26f

        for (mesa in mesas) {
            val marca = if (mesa.cerrando) "[COBRADA]" else "[ABIERTA]"
            val linea = "M${mesa.mesa} $marca $${mesa.total.toInt()}"
            canvas.drawText(linea.take(32), 12f, y, paintTexto)
            y += 26f
        }

        canvas.drawText("--------------------------------", 12f, y, paintTexto)
        y += 30f
        canvas.drawText("CANT: ${mesas.size} | TOT: $${totalGeneral.toInt()}", 12f, y, paintTitulo)

        return bitmap
    }
}
