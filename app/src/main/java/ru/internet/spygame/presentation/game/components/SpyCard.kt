package ru.internet.spygame.presentation.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.internet.spygame.domain.model.GameCard
import ru.internet.spygame.presentation.game.CardUiState
import ru.internet.spygame.presentation.theme.SpyGameTheme
import kotlin.math.abs

/**
 * Игровая карточка с полной анимацией.
 *
 * Машина состояний анимации:
 * ```
 * STACKED ──(tap)──► [scale↑, elevation↑, flip 0→90°, swap, -90°→0°]──► REVEALED
 * REVEALED ──(tap/swipe/timer)──► [translationX ±screenW, alpha→0 за 300ms]──► DISMISSED
 * ```
 *
 * @param card          Доменная модель (слово, категория, isSpy).
 * @param cardUiState   Текущее состояние из GameViewModel.
 * @param isTopCard     true только для верхней карточки стопки.
 * @param timerProgress 1.0 → 0.0 за 5 секунд. Передаётся в [CardBack].
 * @param onTap         Тап по карточке → ViewModel.onCardTap().
 * @param onDismissed   Завершённый свайп за порог → ViewModel.onCardSwiped().
 */
@Composable
fun SpyCard(
    card: GameCard,
    cardUiState: CardUiState,
    isTopCard: Boolean,
    timerProgress: Float,
    onTap: () -> Unit,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val screenWidthPx = LocalWindowInfo.current.containerSize.width.toFloat()
    val swipeThresholdPx = with(density) { 80.dp.toPx() }
    val coroutineScope = rememberCoroutineScope()

    // ─── Scale: 1.0 → 1.05 при reveal (animateFloatAsState по спецификации) ──
    val scale by animateFloatAsState(
        targetValue = if (cardUiState == CardUiState.REVEALED) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "CardScale"
    )

    // ─── Elevation: 2dp → 16dp при reveal (animateDpAsState по спецификации) ─
    val shadowElevation by animateDpAsState(
        targetValue = when (cardUiState) {
            CardUiState.STACKED   -> 2.dp
            CardUiState.REVEALED  -> 16.dp
            CardUiState.DISMISSED -> 0.dp
        },
        animationSpec = tween(durationMillis = 300),
        label = "CardElevation"
    )

    // ─── 3D-флип (Animatable — нужны последовательные фазы) ──────────────────
    val flipRotation = remember { Animatable(0f) }
    // Когда true — показываем CardBack (слово), иначе — CardFront (рубашку)
    var showBackFace by remember { mutableStateOf(false) }

    // ─── Анимация dismiss ────────────────────────────────────────────────────
    val dismissOffsetX = remember { Animatable(0f) }
    val dismissAlpha   = remember { Animatable(1f) }

    // ─── Живое смещение свайпа от пальца ─────────────────────────────────────
    var swipeOffsetX by remember { mutableFloatStateOf(0f) }
    // +1 = вправо, -1 = влево; по умолчанию +1 (dismiss по таймеру или тапу)
    var dismissDirection by remember { mutableIntStateOf(1) }

    // ─── Главный LaunchedEffect ───────────────────────────────────────────────
    LaunchedEffect(cardUiState) {
        when (cardUiState) {

            // Сброс при старте новой игры
            CardUiState.STACKED -> {
                showBackFace = false
                flipRotation.snapTo(0f)
                dismissOffsetX.snapTo(0f)
                dismissAlpha.snapTo(1f)
                swipeOffsetX = 0f
                dismissDirection = 1
            }

            // REVEAL: «парение» затем 3D-флип (по спецификации)
            CardUiState.REVEALED -> {
                // Даём animateFloatAsState/animateDpAsState (scale + elevation) ~150мс
                delay(150L)

                // Фаза 1: рубашка уходит к ребру (rotationY 0° → 90°)
                flipRotation.animateTo(
                    targetValue = 90f,
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                )
                // На 90° карточка ребром → меняем контент
                showBackFace = true
                // Прыжок на противоположный край без анимации
                flipRotation.snapTo(-90f)
                // Фаза 2: оборот появляется из ребра (rotationY −90° → 0°)
                flipRotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                )
            }

            // DISMISS: slide-out + fade (translationX ±screenWidth + alpha за 300мс)
            CardUiState.DISMISSED -> {
                // Стартуем с позиции пальца — плавное продолжение свайпа
                dismissOffsetX.snapTo(swipeOffsetX)
                swipeOffsetX = 0f

                launch {
                    dismissOffsetX.animateTo(
                        targetValue    = screenWidthPx * dismissDirection * 1.5f,
                        animationSpec  = tween(
                            durationMillis = 300,
                            easing         = FastOutLinearInEasing
                        )
                    )
                }
                dismissAlpha.animateTo(
                    targetValue   = 0f,
                    animationSpec = tween(durationMillis = 300)
                )
            }
        }
    }

    // Верхняя карточка реагирует на тап (reveal).
    // REVEALED-карточка — всегда интерактивна (dismiss по тапу).
    val isInteractive = isTopCard || cardUiState == CardUiState.REVEALED

    // ─── Итоговый Modifier ────────────────────────────────────────────────────
    val animatedModifier = modifier
        // Shadow следует за animated elevation
        .shadow(elevation = shadowElevation, shape = RoundedCornerShape(16.dp))
        // Все трансформы в одном graphicsLayer — один RenderNode, без лишних перерисовок
        .graphicsLayer {
            rotationY      = flipRotation.value
            cameraDistance = 12f * density.density          // Предотвращает перспективное искажение
            scaleX         = scale
            scaleY         = scale
            translationX   = dismissOffsetX.value + swipeOffsetX
            alpha          = dismissAlpha.value
        }
        // Тап
        .pointerInput(isInteractive, cardUiState) {
            if (!isInteractive) return@pointerInput
            detectTapGestures(onTap = { onTap() })
        }
        // Горизонтальный свайп — только у REVEALED-карточки
        .pointerInput(cardUiState) {
            if (cardUiState != CardUiState.REVEALED) return@pointerInput
            detectHorizontalDragGestures(
                onDragStart  = { swipeOffsetX = 0f },
                onDragCancel = {
                    coroutineScope.launch {
                        val anim = Animatable(swipeOffsetX)
                        anim.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) {
                            swipeOffsetX = value
                        }
                    }
                },
                onDragEnd = {
                    if (abs(swipeOffsetX) > swipeThresholdPx) {
                        // Порог пройден → dismiss в направлении свайпа
                        dismissDirection = if (swipeOffsetX > 0) 1 else -1
                        onDismissed()
                    } else {
                        // Не добрал до порога → возврат в центр
                        coroutineScope.launch {
                            val anim = Animatable(swipeOffsetX)
                            anim.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) {
                                swipeOffsetX = value
                            }
                        }
                    }
                },
                onHorizontalDrag = { _, dragAmount ->
                    swipeOffsetX += dragAmount
                }
            )
        }

    // ─── Рендер ───────────────────────────────────────────────────────────────
    if (showBackFace) {
        CardBack(
            card          = card,
            timerProgress = timerProgress,
            modifier      = animatedModifier
        )
    } else {
        CardFront(
            categoryName = card.categoryName,
            cardNumber   = card.index,
            modifier     = animatedModifier
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "SpyCard — STACKED (верхняя)")
@Composable
private fun SpyCardStackedPreview() {
    SpyGameTheme {
        SpyCard(
            card          = GameCard(1, false, "Пилот", "Аэропорт"),
            cardUiState   = CardUiState.STACKED,
            isTopCard     = true,
            timerProgress = 1f,
            onTap         = {},
            onDismissed   = {},
            modifier      = Modifier.size(220.dp, 308.dp)
        )
    }
}

@Preview(showBackground = true, name = "SpyCard — REVEALED (шпион)")
@Composable
private fun SpyCardSpyRevealedPreview() {
    SpyGameTheme {
        SpyCard(
            card          = GameCard(3, true, GameCard.SPY_WORD_PLACEHOLDER, "Аэропорт"),
            cardUiState   = CardUiState.REVEALED,
            isTopCard     = false,
            timerProgress = 0.4f,
            onTap         = {},
            onDismissed   = {},
            modifier      = Modifier.size(220.dp, 308.dp)
        )
    }
}
