package com.letssign.mobile.ui.signer

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.letssign.mobile.ui.common.FioDuplo
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.SecondaryButton
import com.letssign.mobile.ui.common.blocoDuro
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaSuave
import com.letssign.mobile.ui.theme.MarcaTexto
import com.letssign.mobile.ui.theme.Papel
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/** Cor da tinta gravada no PNG (inalterada em relação à versão original). */
private val TintaAssinatura = Color(0xFF1C1B3A)

/**
 * Tela de captura de assinatura manuscrita (desenho livre com o dedo),
 * usada pelo Signatário para assinar cada campo posicionado no documento.
 * Visual: folha branca sobre bloco azul, linha de base pontilhada e "×" de onde assinar.
 */
@Composable
fun SignaturePadDialog(
    fieldLabel: String,
    cacheDir: File,
    onDismiss: () -> Unit,
    onConfirm: (imagePath: String) -> Unit
) {
    val strokes = remember { mutableStateOf(listOf<List<Offset>>()) }
    val currentStroke = remember { mutableStateOf(listOf<Offset>()) }
    var canvasSizePx by remember { mutableStateOf(IntSize.Zero) }
    val temTraco = strokes.value.isNotEmpty()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides MarcaTexto) {
                Column(
                    Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .padding(end = 6.dp, bottom = 6.dp)
                        .blocoDuro(Caneta, sombra = 6.dp)
                        .padding(18.dp)
                ) {
                    Text("ASSINAR · ${fieldLabel.uppercase()}", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(6.dp))
                    FioDuplo()
                    Spacer(Modifier.height(8.dp))
                    Text("Sua assinatura", style = MaterialTheme.typography.headlineMedium)
                    Text("Desenhe com o dedo dentro da folha.", style = MaterialTheme.typography.bodyMedium, color = MarcaSuave)
                    Spacer(Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .background(Papel)
                            .semantics { contentDescription = "Área para desenhar a assinatura" }
                            .onSizeChanged { canvasSizePx = it }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset -> currentStroke.value = listOf(offset) },
                                    onDragEnd = {
                                        strokes.value = strokes.value + listOf(currentStroke.value)
                                        currentStroke.value = emptyList()
                                    },
                                    onDrag = { change, _ ->
                                        currentStroke.value = currentStroke.value + change.position
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // linha de base e marca de onde assinar (só guia visual, não vai para o PNG)
                            val base = size.height * 0.74f
                            drawLine(
                                CanetaSuave, Offset(24f, base), Offset(size.width - 24f, base), strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                            )
                            val x0 = 28f; val s = 16f
                            drawLine(Caneta, Offset(x0, base - 18f - s), Offset(x0 + s, base - 18f), 3f)
                            drawLine(Caneta, Offset(x0 + s, base - 18f - s), Offset(x0, base - 18f), 3f)

                            val allStrokes = strokes.value + listOf(currentStroke.value)
                            allStrokes.forEach { points ->
                                if (points.size > 1) {
                                    val path = Path()
                                    path.moveTo(points.first().x, points.first().y)
                                    points.drop(1).forEach { path.lineTo(it.x, it.y) }
                                    drawPath(path, color = TintaAssinatura, style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = {
                                strokes.value = emptyList()
                                currentStroke.value = emptyList()
                            },
                            enabled = temTraco
                        ) {
                            Text("Limpar", style = MaterialTheme.typography.labelLarge, color = if (temTraco) MarcaTexto else MarcaSuave)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BotaoClaro("Cancelar", Modifier.weight(1f), cheio = false, enabled = true, onClick = onDismiss)
                        BotaoClaro(
                            "Confirmar assinatura",
                            Modifier.weight(1.6f),
                            cheio = true,
                            enabled = temTraco,
                            onClick = {
                                val allStrokes = strokes.value
                                if (allStrokes.isNotEmpty() && canvasSizePx.width > 0) {
                                    val path = buildSignatureBitmap(allStrokes, canvasSizePx.width, canvasSizePx.height)
                                    val file = File(cacheDir, "sig_${UUID.randomUUID()}.png")
                                    FileOutputStream(file).use { out -> path.compress(Bitmap.CompressFormat.PNG, 100, out) }
                                    onConfirm(file.absolutePath)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Botão invertido para o fundo azul do diálogo (amarelo cheio / contorno amarelo). */
@Composable
private fun BotaoClaro(texto: String, modifier: Modifier, cheio: Boolean, enabled: Boolean, onClick: () -> Unit) {
    com.letssign.mobile.ui.common.BotaoDuro(
        text = texto,
        modifier = modifier,
        enabled = enabled,
        cheio = cheio,
        fundoCheio = MarcaTexto,
        textoCheio = Caneta,
        contornoClaro = !cheio,
        onClick = onClick
    )
}

private fun buildSignatureBitmap(strokes: List<List<Offset>>, widthPx: Int, heightPx: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(AndroidColor.TRANSPARENT)
    val paint = Paint().apply {
        color = AndroidColor.rgb(28, 27, 58)
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    strokes.forEach { points ->
        if (points.size > 1) {
            val path = AndroidPath()
            path.moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { path.lineTo(it.x, it.y) }
            canvas.drawPath(path, paint)
        }
    }
    return bitmap
}
