package com.letssign.mobile.ui.admin.newdocument

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.letssign.mobile.ui.common.AcoesWizard
import com.letssign.mobile.ui.common.AvisoErro
import com.letssign.mobile.ui.common.quebravel
import com.letssign.mobile.ui.common.BarraDeAcoes
import com.letssign.mobile.ui.common.CampoTexto
import com.letssign.mobile.ui.common.EtapasWizard
import com.letssign.mobile.ui.common.Manchete
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.Rubrica
import com.letssign.mobile.ui.common.SecondaryButton
import com.letssign.mobile.ui.common.blocoDuro
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaTexto
import com.letssign.mobile.ui.theme.Papel
import com.letssign.mobile.ui.theme.Tinta

@Composable
fun TitlePdfStep(
    viewModel: NewDocumentViewModel,
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importPdf(uri, uri.lastPathSegment ?: "documento.pdf")
        }
    }

    Scaffold(
        containerColor = MarcaTexto,
        topBar = {
            Manchete(
                titulo = "Novo documento",
                edicao = "Envio para assinatura",
                compacta = true,
                onBack = onCancel,
                rodape = { EtapasWizard(1) }
            )
        },
        bottomBar = {
            BarraDeAcoes(Modifier.imePadding()) {
                AcoesWizard("Cancelar", onCancel, "Continuar", { viewModel.confirmTitleAndPdf(onDone = onNext) }, state.isSavingStep)
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
                Column(
                    Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Rubrica("Título do documento")
                    CampoTexto(
                        value = state.title,
                        onValueChange = viewModel::onTitleChange,
                        label = "Ex.: Contrato de Prestação de Serviços"
                    )
                    Spacer(Modifier.height(8.dp))
                    Rubrica("Arquivo PDF")

                    val arquivo = state.pdfFileName
                    // Bloco do arquivo: vazio = convite a escolher; preenchido = "folha" com o nome
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp, bottom = 4.dp)
                            .blocoDuro(if (arquivo != null) Papel else MarcaTexto, borda = if (arquivo != null) Tinta else Caneta)
                            .clickable(role = Role.Button, onClickLabel = "Selecionar PDF no celular") {
                                launcher.launch(arrayOf("application/pdf"))
                            }
                            .heightIn(min = 88.dp)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ícone de folha com canto dobrado
                        Box(
                            Modifier.size(width = 40.dp, height = 52.dp).background(Caneta),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("PDF", style = MaterialTheme.typography.labelSmall, color = MarcaTexto)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            if (arquivo == null) {
                                Text("Selecionar PDF no celular", style = MaterialTheme.typography.titleMedium)
                                Text("Toque para abrir os arquivos.", style = MaterialTheme.typography.bodySmall, color = CanetaSuave)
                            } else {
                                Text(quebravel(arquivo), style = MaterialTheme.typography.titleMedium, color = Tinta, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${state.pageCount} página(s) · toque para trocar", style = MaterialTheme.typography.bodySmall, color = Caneta)
                            }
                        }
                        Icon(Icons.Filled.UploadFile, contentDescription = null, tint = if (arquivo != null) Tinta else Caneta)
                    }

                    if (state.isProcessingPdf) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(20.dp), color = Caneta, strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Validando arquivo...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    state.errorMessage?.let { AvisoErro(it) }
                }
            }
        }
    }
}
