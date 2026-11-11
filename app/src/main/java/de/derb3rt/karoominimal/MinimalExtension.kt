package de.derb3rt.karoominimal

import android.content.Context
import android.widget.RemoteViews
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Karoo owns the native map and turn guidance. This extension contributes ONE custom
 * graphical field to a map page, containing speed, cadence and heart rate.
 * No location permission, no network access, no direct sensor connections.
 */
class MinimalExtension : KarooExtension("karoo-minimal", "0.1.0") {
    private val system by lazy { KarooSystemService(applicationContext) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val types: List<DataTypeImpl> by lazy { listOf(MinimalTripleDataType(system, extension)) }

    override fun onCreate() {
        super.onCreate()
        system.connect { /* Connection status is handled by Karoo OS. */ }
    }

    override fun onDestroy() {
        scope.cancel()
        system.disconnect()
        super.onDestroy()
    }
}

private fun KarooSystemService.dataFlow(typeId: String): Flow<StreamState> = callbackFlow {
    val id = addConsumer(
        io.hammerhead.karooext.models.OnStreamState.StartStreaming(typeId),
    ) { event: io.hammerhead.karooext.models.OnStreamState -> trySend(event.state) }
    awaitClose { removeConsumer(id) }
}

private data class TripleReading(
    val speedMetersPerSecond: Double?,
    val cadenceRpm: Double?,
    val heartRateBpm: Double?,
) {
    companion object {
        fun from(speed: StreamState, cadence: StreamState, hr: StreamState) = TripleReading(
            speed.liveNumber(), cadence.liveNumber(), hr.liveNumber(),
        )
    }
}

private fun StreamState.liveNumber(): Double? =
    (this as? StreamState.Streaming)?.dataPoint?.singleValue
        ?.takeIf { it.isFinite() && it >= 0.0 }

private fun displayInteger(value: Double?): String =
    value?.let { String.format(Locale.GERMANY, "%.0f", it) } ?: "—"

private fun displaySpeed(metersPerSecond: Double?): String =
    metersPerSecond?.let { String.format(Locale.GERMANY, "%.1f", it * 3.6) } ?: "—"

private class MinimalTripleDataType(
    private val karoo: KarooSystemService,
    extension: String,
) : DataTypeImpl(extension, "minimal-triple") {

    /** Speed also backs a conventional numeric stream; the visible field is graphical. */
    override fun startStream(emitter: Emitter<StreamState>) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            karoo.dataFlow(DataType.Type.SPEED).collect { state ->
                when (state) {
                    is StreamState.Streaming -> {
                        val value = state.dataPoint.singleValue
                        if (value != null) {
                            emitter.onNext(state.copy(dataPoint = state.dataPoint.copy(
                                dataTypeId = dataTypeId,
                                values = mapOf(DataType.Field.SINGLE to value),
                            )))
                        } else emitter.onNext(StreamState.NotAvailable)
                    }
                    else -> emitter.onNext(state)
                }
            }
        }
        emitter.setCancellable { scope.cancel() }
    }

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        emitter.onNext(UpdateGraphicConfig(showHeader = false))
        scope.launch {
            val speedFlow = karoo.dataFlow(DataType.Type.SPEED)
                .onStart { emit(StreamState.NotAvailable) }
            val cadenceFlow = karoo.dataFlow(DataType.Type.CADENCE)
                .onStart { emit(StreamState.NotAvailable) }
            val hrFlow = karoo.dataFlow(DataType.Type.HEART_RATE)
                .onStart { emit(StreamState.NotAvailable) }

            combine(speedFlow, cadenceFlow, hrFlow, TripleReading::from)
                .distinctUntilChanged()
                .collect { reading ->
                    val views = RemoteViews(context.packageName, R.layout.field_dashboard)
                    views.setTextViewText(R.id.speed_value, displaySpeed(reading.speedMetersPerSecond))
                    views.setTextViewText(R.id.cadence_value, displayInteger(reading.cadenceRpm))
                    views.setTextViewText(R.id.hr_value, displayInteger(reading.heartRateBpm))
                    emitter.updateView(views)
                }
        }
        emitter.setCancellable { scope.cancel() }
    }
}
