package com.letssign.mobile.ui.admin.documents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.letssign.mobile.data.local.entity.DocumentEntity
import com.letssign.mobile.data.local.entity.DocumentStatus
import com.letssign.mobile.di.AppContainer
import com.letssign.mobile.ui.common.BarraDeAcoes
import com.letssign.mobile.ui.common.EmptyState
import com.letssign.mobile.ui.common.Fio
import com.letssign.mobile.ui.common.FioVertical
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.StatusDocumento
import com.letssign.mobile.ui.common.ordem
import com.letssign.mobile.util.DateFormat
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave

@Composable
fun DocumentsListScreen(
    container: AppContainer,
    adminId: Long,
    onNewDocument: () -> Unit,
    onOpenDocument: (Long) -> Unit
) {
    val viewModel: DocumentsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DocumentsViewModel(container.documentRepository, adminId) }
        }
    )
    val documents by viewModel.documents.collectAsState()

    CompositionLocalProvider(LocalContentColor provides Caneta) {
        Column(Modifier.fillMaxSize()) {
            if (documents.isEmpty()) {
                EmptyState(
                    "Nenhum documento cadastrado.\nUse \"Novo documento\" para enviar um PDF para assinatura.",
                    Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item { Placar(documents) }
                    itemsIndexed(documents, key = { _, d -> d.id }) { i, document ->
                        DocumentRow(i, document, onClick = { onOpenDocument(document.id) })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
            BarraDeAcoes {
                PrimaryButton(text = "+  Novo documento", onClick = onNewDocument)
            }
        }
    }
}

/** Placar em três colunas de jornal: quanto está em andamento, concluído e parado. */
@Composable
private fun Placar(documents: List<DocumentEntity>) {
    val andamento = documents.count { it.status == DocumentStatus.SENT || it.status == DocumentStatus.PARTIALLY_SIGNED }
    val concluidos = documents.count { it.status == DocumentStatus.COMPLETED }
    val parados = documents.count { it.status == DocumentStatus.DRAFT || it.status == DocumentStatus.SENDING_FAILED }
    Column(Modifier.widthIn(max = 720.dp).fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp)) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Numero(andamento, "em andamento", Modifier.weight(1f))
            FioVertical(Modifier.padding(horizontal = 10.dp))
            Numero(concluidos, "concluídos", Modifier.weight(1f))
            FioVertical(Modifier.padding(horizontal = 10.dp))
            Numero(parados, "rascunho ou falha", Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Fio(espessura = 2.dp)
    }
}

@Composable
private fun Numero(n: Int, legenda: String, modifier: Modifier) {
    Column(modifier) {
        Text(n.toString(), style = MaterialTheme.typography.displaySmall)
        Text(legenda, style = MaterialTheme.typography.bodySmall, color = CanetaSuave, maxLines = 2)
    }
}

@Composable
private fun DocumentRow(index: Int, document: DocumentEntity, onClick: () -> Unit) {
    Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clickable(onClickLabel = "Abrir acompanhamento", onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                ordem(index),
                style = MaterialTheme.typography.titleSmall,
                color = CanetaSuave,
                modifier = Modifier.width(30.dp).padding(top = 5.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "v${document.version} · criado em ${DateFormat.format(document.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CanetaSuave
                )
                Spacer(Modifier.height(2.dp))
                StatusDocumento(document.status)
            }
        }
        Fio(Modifier.padding(horizontal = 20.dp))
    }
}
