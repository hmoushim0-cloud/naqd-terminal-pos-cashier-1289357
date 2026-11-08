package sa.factory.naqdterminalposcas.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver {
    private val peripheral = "ESC/POS Thermal Receipt Printer + Barcode Scanner"

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "Handshake initializing..."))
        delay(300)
        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "ESC/POS: 150mm Paper | Barcode Scan: 200-456-789 | Print Head Temp: 42°C"))

        while (true) {
            delay(1500)
            val jitter = Random.nextInt(1, 10)
            emit(HardwareTelemetryFrame(peripheral, "ACTIVE_STREAMING", "ESC/POS: 150mm Paper | Barcode Scan: 200-456-789 | Print Head Temp: 42°C | Frame #${System.currentTimeMillis() % 10000} (Jitter: +${jitter}ms)"))
        }
    }
}
