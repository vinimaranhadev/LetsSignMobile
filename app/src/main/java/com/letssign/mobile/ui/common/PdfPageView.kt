package com.letssign.mobile.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.unit.dp
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.Papel
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.letssign.mobile.data.storage.PdfPageRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Ponto de injeção usado só por previews/testes de screenshot (JVM/Robolectric não tem
 * o PdfRenderer nativo). Em produção é null e a página vem do [PdfPageRenderer].
 */
val LocalPdfPagePreview = staticCompositionLocalOf<((path: String, page: Int, widthPx: Int) -> android.graphics.Bitmap)?> { null }

/**
 * Renderiza uma página do PDF ocupando a largura disponível, mantendo a
 * proporção original da página, e expõe o tamanho renderizado (em Dp) via
 * [content] para que overlays (campos de assinatura) possam ser posicionados
 * com base nos mesmos ratios (0..1) salvos no banco de dados.
 */
@Composable
fun PdfPageView(
    filePath: String,
    page: Int,
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.(pageWidthDp: Dp, pageHeightDp: Dp) -> Unit = { _, _ -> }
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val targetWidthPx = with(density) { maxWidth.toPx() }.toInt().coerceAtLeast(1)

        val preview = LocalPdfPagePreview.current
        val rendered = produceState<PdfPageRenderer.RenderedPage?>(initialValue = null, filePath, page, targetWidthPx) {
            if (preview != null) {
                // preview/teste: desenha na própria thread (Robolectric não aceita outra)
                val bmp = preview(filePath, page, targetWidthPx)
                value = PdfPageRenderer.RenderedPage(bmp, bmp.width, bmp.height)
                return@produceState
            }
            value = withContext(Dispatchers.IO) {
                try {
                    PdfPageRenderer(File(filePath)).use { it.renderPage(page, targetWidthPx) }
                } catch (e: Exception) {
                    null
                }
            }
        }.value

        if (rendered == null) {
            // Folha em branco na proporção A4 enquanto a página renderiza
            androidx.compose.foundation.layout.Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f / 1.414f)
                    .background(Papel),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Caneta, strokeWidth = 3.dp)
            }
        } else {
            val pageHeightDp = with(density) { rendered.bitmap.height.toFloat().toDp() }
            Image(
                bitmap = rendered.bitmap.asImageBitmap(),
                contentDescription = "Página ${page + 1} do documento",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            content(maxWidth, pageHeightDp)
        }
    }
}
