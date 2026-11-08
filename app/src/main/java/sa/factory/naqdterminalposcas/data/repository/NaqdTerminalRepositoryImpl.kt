package sa.factory.naqdterminalposcas.data.repository

import sa.factory.naqdterminalposcas.domain.repository.NaqdTerminalRepository
import sa.factory.naqdterminalposcas.hardware.HardwareSensorDriver
import sa.factory.naqdterminalposcas.model.NaqdTerminalItem
import sa.factory.naqdterminalposcas.model.NaqdTerminalOrder
import sa.factory.naqdterminalposcas.model.NaqdTerminalTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NaqdTerminalRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : NaqdTerminalRepository {

    private val _catalog = MutableStateFlow<List<NaqdTerminalItem>>(listOf(
        NaqdTerminalItem("1", "إضافة منتج ذكي", "إدخال منتجات الأثاث عبر مسح الباركود مع توليد أوصاف تلقائية باستخدام الذكاء الاصطناعي", 75.0, "AI", "طاولات طعام"),
        NaqdTerminalItem("2", "طباعة فاتورة حرارية", "إصدار فواتير فورية عبر طابعة ESC/POS مع تفاصيل المنتج الكاملة", 45.0, "فوري", "طاولات قهوة"),
        NaqdTerminalItem("3", "تحليل مخزون الأثاث", "تقرير تلقائي عن المنتجات الأكثر مبيعاً مع توصيات تسعير", 199.0, "تحليلي", "طاولات دراسة")
    ))
    override val catalogFlow: Flow<List<NaqdTerminalItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<NaqdTerminalItem>>(emptyList())
    override val activeCart: StateFlow<List<NaqdTerminalItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<NaqdTerminalTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            NaqdTerminalTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 96,
                signalDbm = -54
            )
        }

    override fun addToCart(item: NaqdTerminalItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: NaqdTerminalItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): NaqdTerminalOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = NaqdTerminalOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}
