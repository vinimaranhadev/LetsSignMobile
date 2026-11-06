package com.letssign.mobile.ui.admin.newdocument

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.letssign.mobile.ui.common.AcoesWizard
import com.letssign.mobile.ui.common.AvisoErro
import com.letssign.mobile.ui.common.quebravel
import com.letssign.mobile.ui.common.BarraDeAcoes
import com.letssign.mobile.ui.common.CampoTexto
import com.letssign.mobile.ui.common.DuasColunas
import com.letssign.mobile.ui.common.EtapasWizard
import com.letssign.mobile.ui.common.Fio
import com.letssign.mobile.ui.common.Iniciais
import com.letssign.mobile.ui.common.Manchete
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.Rubrica
import com.letssign.mobile.ui.common.SecondaryButton
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaTexto

@Composable
fun ReviewSendScreen(
    viewModel: NewDocumentViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.sendSuccess) {
        if (state.sendSuccess == true) {
            onFinished()
        }
    }

    Scaffold(
        containerColor = MarcaTexto,
        topBar = {
            Manchete(
                titulo = state.title.ifBlank { "Revisão" },
                edicao = "Revisão e envio",
                compacta = true,
                onBack = onBack,
                rodape = { EtapasWizard(4) }
            )
        },
        bottomBar = {
            BarraDeAcoes(Modifier.imePadding()) {
                AcoesWizard(
                    "Voltar", onBack,
                    if (state.sendSuccess == false) "Tentar novamente" else "Confirmar e enviar", { viewModel.send() },
                    isLoading = state.isSending,
                    voltarHabilitado = !state.isSending
                )
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
                Column(Modifier.widthIn(max = 720.dp).fillMaxWidth().padding(20.dp)) {
                    Rubrica("Documento")
                    Spacer(Modifier.height(10.dp))
                    DuasColunas(
                        esquerda = {
                            Rotulo("Arquivo")
                            Text(quebravel(state.pdfFileName ?: "-"), style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        },
                        direita = {
                            Row {
                                Column(Modifier.weight(1f)) {
                                    Rotulo("Páginas")
                                    Text("${state.pageCount}", style = MaterialTheme.typography.headlineMedium)
                                }
                                Column(Modifier.weight(1f)) {
                                    Rotulo("Campos")
                                    Text("${state.fields.size}", style = MaterialTheme.typography.headlineMedium)
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(22.dp))
                    Rubrica("Signatários · ${state.selectedSigners.size}")
                    state.selectedSigners.forEach { sel ->
                        val campos = state.fields.filter { it.signerId == sel.signer.id }
                        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Iniciais(sel.signer.fullName)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(sel.signer.fullName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(sel.signer.email, style = MaterialTheme.typography.bodySmall, color = CanetaSuave, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${classificationLabelPublic(sel.classification)} · ${campos.size} campo(s)" +
                                        if (campos.isNotEmpty()) " na pág. ${campos.map { it.page + 1 }.distinct().sorted().joinToString(", ")}" else "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Fio()
                    }

                    Spacer(Modifier.height(22.dp))
                    Rubrica("Mensagem do convite")
                    Spacer(Modifier.height(10.dp))
                    CampoTexto(
                        value = state.inviteMessage,
                        onValueChange = viewModel::onInviteMessageChange,
                        label = "Texto enviado aos signatários",
                        singleLine = false,
                        minLines = 3
                    )

                    if (state.sendSuccess == false) {
                        Spacer(Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AvisoErro("Falha ao enviar. ${state.errorMessage ?: "Tente novamente."}")
                            Text(
                                "Você pode tentar novamente sem duplicar a solicitação.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 14.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun Rotulo(texto: String) {
    Text(texto.uppercase(), style = MaterialTheme.typography.labelSmall, color = CanetaSuave)
}
