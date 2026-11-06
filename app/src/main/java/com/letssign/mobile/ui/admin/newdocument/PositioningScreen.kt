package com.letssign.mobile.ui.admin.newdocument

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.letssign.mobile.ui.common.AcoesWizard
import com.letssign.mobile.ui.common.AvisoErro
import com.letssign.mobile.ui.common.BarraDeAcoes
import com.letssign.mobile.ui.common.EtapasWizard
import com.letssign.mobile.ui.common.Manchete
import com.letssign.mobile.ui.common.PdfPageView
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.SecondaryButton
import com.letssign.mobile.ui.common.blocoDuro
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaTexto
import com.letssign.mobile.ui.theme.Tinta
import com.letssign.mobile.ui.theme.VermelhoCarimbo

@Composable
fun PositioningScreen(
    viewModel: NewDocumentViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var currentPage by remember { mutableStateOf(0) }
    var activeSignerId by remember(state.selectedSigners) {
        mutableStateOf(state.selectedSigners.firstOrNull()?.signer?.id)
    }

    val pdfPath = state.pdfPath
    val nomeDe = { id: Long -> state.selectedSigners.firstOrNull { it.signer.id == id }?.signer?.fullName ?: "?" }

    Scaffold(
        containerColor = MarcaTexto,
        topBar = {
            Manchete(
                titulo = "Onde assinar",
                edicao = state.title.ifBlank { "Novo documento" },
                compacta = true,
                onBack = onBack,
                rodape = { EtapasWizard(3) }
            )
        },
        bottomBar = {
            BarraDeAcoes {
                state.errorMessage?.let { AvisoErro(it) }
                AcoesWizard("Voltar", onBack, "Continuar", { viewModel.confirmFields(onNext) }, state.isSavingStep)
            }
        }
    ) { padding ->
        CompositionLocalProvider(LocalContentColor provides Caneta) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.widthIn(max = 640.dp).fillMaxWidth()) {
                    Text(
                        "Escolha quem assina e toque na página onde o campo deve ficar. Arraste para ajustar.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                    ) {
                        items(state.selectedSigners) { selected ->
                            val ativo = activeSignerId == selected.signer.id
                            Box(
                                Modifier
                                    .heightIn(min = 48.dp)
                                    .padding(end = 3.dp, bottom = 3.dp)
                                    .blocoDuro(if (ativo) Caneta else MarcaTexto, sombra = if (ativo) 3.dp else 0.dp, borda = if (ativo) Tinta else Caneta)
                                    .clickable(role = Role.RadioButton) { activeSignerId = selected.signer.id }
                                    .semantics { this.selected = ativo }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    selected.signer.fullName.substringBefore(" "),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (ativo) MarcaTexto else Caneta
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        IconButton(onClick = { if (currentPage > 0) currentPage-- }, enabled = currentPage > 0,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Caneta, disabledContentColor = CanetaSuave)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Página anterior")
                        }
                        Text(
                            "Página ${currentPage + 1} de ${state.pageCount}",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { if (currentPage < state.pageCount - 1) currentPage++ },
                            enabled = currentPage < state.pageCount - 1,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Caneta, disabledContentColor = CanetaSuave)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próxima página")
                        }
                    }

                    // A folha: borda grossa + sombra dura, a página do PDF por cima
                    Box(
                        Modifier
                            .padding(start = 16.dp, end = 20.dp, bottom = 4.dp)
                            .blocoDuro(MarcaTexto)
                            .padding(2.dp)
                    ) {
                        if (pdfPath != null) {
                            PdfPageView(
                                filePath = pdfPath,
                                page = currentPage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(currentPage, activeSignerId) {
                                        detectTapGestures { offset ->
                                            val signerId = activeSignerId ?: return@detectTapGestures
                                            val xRatio = offset.x / size.width
                                            val yRatio = offset.y / size.height
                                            viewModel.addField(signerId, currentPage, xRatio, yRatio)
                                        }
                                    }
                            ) { pageWidthDp, pageHeightDp ->
                                state.fields.filter { it.page == currentPage }.forEach { field ->
                                    FieldOverlay(
                                        field = field,
                                        signerName = nomeDe(field.signerId).substringBefore(" "),
                                        pageWidthDp = pageWidthDp,
                                        pageHeightDp = pageHeightDp,
                                        onMove = { nx, ny -> viewModel.moveField(field.key, nx, ny) },
                                        onDelete = { viewModel.deleteField(field.key) }
                                    )
                                }
                            }
                        }
                    }

                    // Índice dos campos (alvo de toque de 48dp para remover)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "CAMPOS POSICIONADOS · ${state.fields.size}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    state.fields.forEachIndexed { i, field ->
                        Row(
                            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${i + 1}. ${nomeDe(field.signerId)} · página ${field.page + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(onClick = { viewModel.deleteField(field.key) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remover campo ${i + 1}", tint = VermelhoCarimbo)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun FieldOverlay(
    field: FieldDraft,
    signerName: String,
    pageWidthDp: Dp,
    pageHeightDp: Dp,
    onMove: (Float, Float) -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val widthDp = pageWidthDp * field.widthRatio
    val heightDp = pageHeightDp * field.heightRatio
    val offsetXDp = pageWidthDp * field.xRatio
    val offsetYDp = pageHeightDp * field.yRatio

    Box(
        modifier = Modifier
            .offset(x = offsetXDp, y = offsetYDp)
            .size(width = widthDp, height = heightDp)
            .background(MarcaTexto.copy(alpha = 0.85f), RectangleShape)
            .border(2.dp, Caneta, RectangleShape)
            .pointerInput(field.key) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val pageWidthPx = with(density) { pageWidthDp.toPx() }
                    val pageHeightPx = with(density) { pageHeightDp.toPx() }
                    val newXRatio = field.xRatio + dragAmount.x / pageWidthPx
                    val newYRatio = field.yRatio + dragAmount.y / pageHeightPx
                    onMove(newXRatio, newYRatio)
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "× $signerName",
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = Caneta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                Modifier
                    .size(24.dp)
                    .clickable(onClickLabel = "Remover campo", onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Remover campo", tint = VermelhoCarimbo, modifier = Modifier.size(16.dp))
            }
        }
    }
}
