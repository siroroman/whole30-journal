package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.dayentry.presentation.ui.icons.DragHandleIcon
import kotlin.math.abs
import kotlin.math.sign

internal val EntryListSpacing: Dp = DSSpacing.space12

@Stable
internal class ReorderState(
    private val idsProvider: () -> List<String>,
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    private val spacingPx: Float,
) {
    var draggedId by mutableStateOf<String?>(null)
        private set
    var dragOffset by mutableFloatStateOf(0f)
        private set

    private val heights = HashMap<String, Float>()
    private var order = mutableListOf<String>()
    private var draggedIndex = -1

    fun onItemSized(id: String, height: Float) {
        heights[id] = height
    }

    fun onDragStart(id: String) {
        val snapshot = idsProvider().toMutableList()
        val index = snapshot.indexOf(id)
        if (index < 0) return
        order = snapshot
        draggedIndex = index
        draggedId = id
        dragOffset = 0f
    }

    fun onDrag(deltaY: Float) {
        if (draggedId == null) return
        dragOffset += deltaY
        var slot = neighborSlot()
        while (slot != null && abs(dragOffset) > slot / 2) {
            val direction = sign(dragOffset)
            val target = draggedIndex + direction.toInt()
            onMove(draggedIndex, target)
            order.add(target, order.removeAt(draggedIndex))
            dragOffset -= direction * slot
            draggedIndex = target
            slot = neighborSlot()
        }
    }

    fun onDragEnd() {
        draggedId = null
        dragOffset = 0f
        draggedIndex = -1
    }

    private fun neighborSlot(): Float? {
        val neighborIndex = draggedIndex + if (dragOffset > 0f) 1 else -1
        return order.getOrNull(neighborIndex)?.let { heights[it] }?.let { it + spacingPx }
    }
}

@Composable
internal fun rememberReorderState(ids: List<String>, onMove: (fromIndex: Int, toIndex: Int) -> Unit): ReorderState {
    val currentIds by rememberUpdatedState(ids)
    val currentOnMove by rememberUpdatedState(onMove)
    val spacingPx = with(LocalDensity.current) { EntryListSpacing.toPx() }
    return remember(spacingPx) {
        ReorderState(
            idsProvider = { currentIds },
            onMove = { from, to -> currentOnMove(from, to) },
            spacingPx = spacingPx,
        )
    }
}

internal fun Modifier.reorderableItem(state: ReorderState, id: String): Modifier = this
    .zIndex(if (state.draggedId == id) 1f else 0f)
    .graphicsLayer { translationY = if (state.draggedId == id) state.dragOffset else 0f }
    .onSizeChanged { state.onItemSized(id, it.height.toFloat()) }

@Composable
internal fun ReorderHandle(
    state: ReorderState,
    id: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
) {
    Box(
        modifier = modifier.pointerInput(state, id) {
            detectDragGestures(
                onDragStart = { state.onDragStart(id) },
                onDrag = { change, dragAmount ->
                    change.consume()
                    state.onDrag(dragAmount.y)
                },
                onDragEnd = state::onDragEnd,
                onDragCancel = state::onDragEnd,
            )
        },
        contentAlignment = Alignment.Center,
    ) {
        DragHandleIcon(
            tint = DSTheme.colors.textTertiary,
            modifier = Modifier.size(iconSize),
            contentDescription = contentDescription,
        )
    }
}
