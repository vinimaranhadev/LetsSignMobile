package com.letssign.mobile.ui.admin.documents

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.letssign.mobile.data.local.entity.DocumentEventEntity
import com.letssign.mobile.data.local.entity.DocumentEventType
import com.letssign.mobile.data.local.entity.DocumentStatus
import com.letssign.mobile.data.local.entity.SignatureStatus
import com.letssign.mobile.di.AppContainer
import com.letssign.mobile.ui.admin.newdocument.classificationLabelPublic
import com.letssign.mobile.ui.common.AvisoErro
import com.letssign.mobile.ui.common.BarraDeAcoes
import com.letssign.mobile.ui.common.Carregando
import com.letssign.mobile.ui.common.DuasColunas
import com.letssign.mobile.ui.common.Fio
import com.letssign.mobile.ui.common.Iniciais
import com.letssign.mobile.ui.common.Manchete
import com.letssign.mobile.ui.common.Marcador
import com.letssign.mobile.ui.common.MarcadorStatus
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.Rubrica
import com.letssign.mobile.ui.common.SecondaryButton
import com.letssign.mobile.ui.common.StatusDocumento
import com.letssign.mobile.ui.common.blocoDuro
import com.letssign.mobile.ui.common.signatureStatusLabel
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaTexto
import com.letssign.mobile.ui.theme.Tinta
import com.letssign.mobile.ui.theme.VermelhoCarimbo
import com.letssign.mobile.ui.theme.movimentoReduzido
import com.letssign.mobile.util.DateFormat

@Composable
fun DocumentDetailScreen(
    container: AppContainer,
    documentId: Long,
    onBack: () -> Unit,
    onNewVersionCreated: (Long) -> Unit
) {
    val viewModel: DocumentDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                DocumentDetailViewModel(container.documentRepository, container.userRepository, documentId)
            }
        }
    )
    val state by viewModel.uiState.collectAsState()
    var retrying by remember { mutableStateOf(false) }
    var retryError by remember { mutableStateOf<String?>(null) }
    val document = state.document

    Scaffold(
        containerColor = MarcaTexto,
        topBar = {
            Manchete(
                titulo = document?.title ?: "Acompanhamento",
                edicao = "Acompanhamento",
                compacta = true,
                onBack = onBack,
                linhaFina = document?.let { "Versão ${it.version} · ${it.originalFileName}" }
            )
        },
        bottomBar = {
            if (document != null && document.status != DocumentStatus.DRAFT) {
                BarraDeAcoes {
                    SecondaryButton("Criar nova versão deste documento", onClick = { viewModel.createNewVersion(onNewVersionCreated) })
                }
            }
        }
    ) { padding ->
        if (document == null) {
            Carregando(Modifier.padding(padding))
            return@Scaffold
        }

        CompositionLocalProvider(LocalContentColor provides Caneta) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.widthIn(max = 720.dp).fillMaxWidth().padding(20.dp)) {
                    StatusDocumento(document.status)
                    Spacer(Modifier.height(14.dp))

                    // Datas em colunas de jornal
                    DuasColunas(
                        esquerda = {
                            Data("Criado em", DateFormat.format(document.createdAt))
                            Spacer(Modifier.height(10.dp))
                            Data("Enviado em", DateFormat.format(document.sentAt))
                        },
                        direita = {
                            Data("Concluído em", DateFormat.format(document.completedAt))
                            Spacer(Modifier.height(10.dp))
                            Data("Assinaturas", "${state.signers.count { it.documentSigner.status == SignatureStatus.SIGNED }} de ${state.signers.size}")
                        }
                    )

                    if (document.status == DocumentStatus.SENDING_FAILED) {
                        Spacer(Modifier.height(18.dp))
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(end = 4.dp, bottom = 4.dp)
                                .blocoDuro(MarcaTexto, borda = VermelhoCarimbo)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AvisoErro("O envio deste documento falhou.")
                            retryError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = VermelhoCarimbo) }
                            PrimaryButton(
                                text = "Tentar enviar novamente",
                                isLoading = retrying,
                                onClick = {
                                    retrying = true
                                    viewModel.retrySend { success ->
                                        retrying = false
                                        retryError = if (!success) "Falha ao reenviar. Verifique o documento." else null
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))
                    Rubrica("Signatários")
                    state.signers.forEach { row ->
                        val assinado = row.documentSigner.status == SignatureStatus.SIGNED
                        val nome = row.signer?.fullName ?: "Signatário #${row.documentSigner.signerId}"
                        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Iniciais(nome)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(nome, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    classificationLabelPublic(row.documentSigner.classification) +
                                        if (assinado) " · ${DateFormat.format(row.documentSigner.signedAt)}" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CanetaSuave
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MarcadorStatus(if (assinado) Marcador.CHEIO else Marcador.VAZIO, if (assinado) Tinta else Caneta)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    signatureStatusLabel(row.documentSigner.status).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (assinado) Tinta else Caneta
                                )
                            }
                        }
                        Fio()
                    }

                    Spacer(Modifier.height(22.dp))
                    Rubrica("Histórico de eventos")
                    Spacer(Modifier.height(10.dp))
                    LinhaDoTempo(state.events)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun Data(rotulo: String, valor: String) {
    Text(rotulo.uppercase(), style = MaterialTheme.typography.labelSmall, color = CanetaSuave)
    Text(valor, style = MaterialTheme.typography.titleMedium)
}

/**
 * Linha do tempo que se desenha: o trilho desce de evento em evento e cada marco
 * aparece quando o traço chega nele. Estática com animações desligadas no sistema.
 */
@Composable
private fun LinhaDoTempo(eventos: List<DocumentEventEntity>) {
    if (eventos.isEmpty()) {
        Text("Nenhum evento registrado.", style = MaterialTheme.typography.bodyMedium, color = CanetaSuave)
        return
    }
    val reduzido = movimentoReduzido()
    val progresso = remember { Animatable(if (reduzido) eventos.size.toFloat() else 0f) }
    LaunchedEffect(eventos.size, reduzido) {
        if (reduzido) progresso.snapTo(eventos.size.toFloat())
        else progresso.animateTo(eventos.size.toFloat(), tween(420 * eventos.size, easing = FastOutSlowInEasing))
    }
    Column {
        eventos.forEachIndexed { i, evento ->
            // fração deste trecho já desenhada (0..1)
            val trecho = (progresso.value - i).coerceIn(0f, 1f)
            val ultimo = i == eventos.lastIndex
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(Modifier.width(28.dp).fillMaxHeight()) {
                    val cor = if (evento.type == DocumentEventType.SEND_FAILED) VermelhoCarimbo else Caneta
                    Canvas(Modifier.fillMaxSize()) {
                        val x = 9.dp.toPx()
                        val cy = 11.dp.toPx()
                        val r = 6.dp.toPx()
                        if (!ultimo) {
                            val fim = cy + (size.height - cy) * trecho
                            drawLine(Caneta, Offset(x, cy), Offset(x, fim), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Square)
                        }
                        if (trecho > 0f) {
                            val escala = (trecho * 3f).coerceAtMost(1f)
                            if (evento.type == DocumentEventType.COMPLETED || evento.type == DocumentEventType.SIGNER_SIGNED) {
                                drawCircle(cor, r * escala, Offset(x, cy))
                            } else {
                                drawCircle(MarcaTexto, r * escala, Offset(x, cy))
                                drawCircle(cor, r * escala, Offset(x, cy), style = Stroke(2.dp.toPx()))
                            }
                        }
                    }
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(bottom = if (ultimo) 0.dp else 18.dp)
                ) {
                    Text(eventLabel(evento.type), style = MaterialTheme.typography.titleLarge,
                        color = if (evento.type == DocumentEventType.SEND_FAILED) VermelhoCarimbo else Caneta)
                    Text(evento.description, style = MaterialTheme.typography.bodyMedium)
                    Text(DateFormat.format(evento.timestamp), style = MaterialTheme.typography.bodySmall, color = CanetaSuave)
                }
            }
        }
    }
}

private fun eventLabel(type: DocumentEventType): String = when (type) {
    DocumentEventType.CREATED -> "Criação"
    DocumentEventType.SENT -> "Envio"
    DocumentEventType.SEND_FAILED -> "Falha no envio"
    DocumentEventType.SIGNER_SIGNED -> "Assinatura"
    DocumentEventType.COMPLETED -> "Conclusão"
    DocumentEventType.NEW_VERSION -> "Nova versão"
}
