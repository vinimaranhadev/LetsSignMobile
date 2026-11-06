package com.letssign.mobile.ui.signer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.letssign.mobile.data.local.entity.SignatureFieldEntity
import com.letssign.mobile.di.AppContainer
import com.letssign.mobile.ui.common.AvisoErro
import com.letssign.mobile.ui.common.BarraDeAcoes
import com.letssign.mobile.ui.common.CarimboAssinado
import com.letssign.mobile.ui.common.Carregando
import com.letssign.mobile.ui.common.Manchete
import com.letssign.mobile.ui.common.PdfPageView
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.blocoDuro
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaTexto
import com.letssign.mobile.ui.theme.VermelhoCarimbo

@Composable
fun SignDocumentScreen(
    container: AppContainer,
    documentSignerId: Long,
    onFinished: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: SignDocumentViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SignDocumentViewModel(container.documentRepository, documentSignerId) }
        }
    )
    val state by viewModel.uiState.collectAsState()
    var currentPage by remember { mutableStateOf(0) }
    var fieldBeingSigned by remember { mutableStateOf<SignatureFieldEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(state.completed) {
        if (state.completed) onFinished()
    }

    LaunchedEffect(state.fields) {
        if (state.fields.isNotEmpty()) currentPage = state.fields.minOf { it.page }
    }

    val document = state.document
    val assinados = state.signedImagesByFieldId.size
    val total = state.fields.size

    Scaffold(
        containerColor = MarcaTexto,
        topBar = {
            Manchete(
                titulo = document?.title ?: "Assinar documento",
                edicao = "Assinar documento",
                compacta = true,
                onBack = onBack,
                linhaFina = if (total > 0) "Campos assinados: $assinados de $total" else null
            )
        },
        bottomBar = {
            if (!state.isLoading) {
                BarraDeAcoes {
                    state.errorMessage?.let { AvisoErro(it) }
                    PrimaryButton(
                        text = when {
                            viewModel.allFieldsSigned() -> "Concluir assinatura"
                            total - assinados == 1 -> "Falta assinar 1 campo"
                            else -> "Faltam assinar ${total - assinados} campos"
                        },
                        isLoading = state.isSubmitting,
                        enabled = viewModel.allFieldsSigned(),
                        onClick = { viewModel.submit() }
                    )
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Carregando(Modifier.padding(padding))
            return@Scaffold
        }
        val pdfPath = document?.filePath
        val pageCount = document?.pageCount ?: 0

        CompositionLocalProvider(LocalContentColor provides Caneta) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.widthIn(max = 640.dp).fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Caneta, disabledContentColor = CanetaSuave)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Página anterior")
                        }
                        Text(
                            "Página ${currentPage + 1} de $pageCount",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { if (currentPage < pageCount - 1) currentPage++ },
                            enabled = currentPage < pageCount - 1,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Caneta, disabledContentColor = CanetaSuave)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próxima página")
                        }
                    }

                    Box(
                        Modifier
                            .padding(start = 16.dp, end = 20.dp, bottom = 16.dp)
                            .blocoDuro(MarcaTexto)
                            .padding(2.dp)
                    ) {
                        if (pdfPath != null) {
                            PdfPageView(
                                filePath = pdfPath,
                                page = currentPage,
                                modifier = Modifier.fillMaxWidth()
                            ) { pageWidthDp, pageHeightDp ->
                                state.fields.filter { it.page == currentPage }.forEach { field ->
                                    val isSigned = state.signedImagesByFieldId.containsKey(field.id)
                                    val widthDp = pageWidthDp * field.widthRatio
                                    val heightDp = pageHeightDp * field.heightRatio
                                    val offsetXDp = pageWidthDp * field.xRatio
                                    val offsetYDp = pageHeightDp * field.yRatio
                                    Box(
                                        modifier = Modifier
                                            .offset(x = offsetXDp, y = offsetYDp)
                                            .size(width = widthDp, height = heightDp)
                                            .then(
                                                if (isSigned) Modifier.border(1.dp, VermelhoCarimbo.copy(alpha = 0.5f), RectangleShape)
                                                else Modifier.background(MarcaTexto, RectangleShape).border(2.dp, Caneta, RectangleShape)
                                            )
                                            .then(
                                                if (!isSigned) Modifier.clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    role = Role.Button,
                                                    onClickLabel = "Assinar este campo"
                                                ) { fieldBeingSigned = field } else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSigned) {
                                            CarimboAssinado()
                                        } else {
                                            Text(
                                                "× Toque para assinar",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Caneta,
                                                maxLines = 2,
                                                textAlign = TextAlign.Center,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fieldBeingSigned?.let { field ->
        SignaturePadDialog(
            fieldLabel = "Página ${field.page + 1}",
            cacheDir = context.cacheDir,
            onDismiss = { fieldBeingSigned = null },
            onConfirm = { path ->
                viewModel.onFieldSigned(field.id, path)
                fieldBeingSigned = null
            }
        )
    }
}
