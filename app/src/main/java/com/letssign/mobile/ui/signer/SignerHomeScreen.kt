package com.letssign.mobile.ui.signer

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.letssign.mobile.data.local.entity.SignatureStatus
import com.letssign.mobile.di.AppContainer
import com.letssign.mobile.session.SessionUser
import com.letssign.mobile.ui.admin.newdocument.classificationLabelPublic
import com.letssign.mobile.ui.common.EmptyState
import com.letssign.mobile.ui.common.Fio
import com.letssign.mobile.ui.common.Manchete
import com.letssign.mobile.ui.common.StatusDocumento
import com.letssign.mobile.ui.common.edicaoDoDia
import com.letssign.mobile.ui.common.ordem
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaTexto
import com.letssign.mobile.util.DateFormat

@Composable
fun SignerHomeScreen(
    container: AppContainer,
    signer: SessionUser,
    onOpenDocument: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: SignerDocumentsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SignerDocumentsViewModel(container.documentRepository, signer.id) }
        }
    )
    val items by viewModel.items.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    val pending = items.filter { it.documentSigner.status == SignatureStatus.PENDING }
    val signed = items.filter { it.documentSigner.status == SignatureStatus.SIGNED }

    Scaffold(
        containerColor = MarcaTexto,
        topBar = {
            Manchete(
                titulo = "Olá, ${signer.fullName.substringBefore(" ")}",
                edicao = edicaoDoDia("Suas assinaturas"),
                linhaFina = when (pending.size) {
                    0 -> "Nada pendente para assinar."
                    1 -> "1 documento aguarda a sua assinatura."
                    else -> "${pending.size} documentos aguardam a sua assinatura."
                },
                paddingInferior = 0.dp,
                acoes = {
                    IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair") }
                },
                rodape = {
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Aba("Pendentes (${pending.size})", tab == 0, Modifier.weight(1f)) { tab = 0 }
                        Aba("Assinados (${signed.size})", tab == 1, Modifier.weight(1f)) { tab = 1 }
                    }
                }
            )
        }
    ) { padding ->
        val list = if (tab == 0) pending else signed
        CompositionLocalProvider(LocalContentColor provides Caneta) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (list.isEmpty()) {
                    EmptyState(if (tab == 0) "Nenhum documento pendente de assinatura." else "Nenhum documento assinado ainda.")
                } else {
                    LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        itemsIndexed(list, key = { _, it -> it.documentSigner.id }) { i, item ->
                            val document = item.document ?: return@itemsIndexed
                            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 72.dp)
                                        .clickable(onClickLabel = if (tab == 0) "Assinar" else "Abrir") { onOpenDocument(item.documentSigner.id) }
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(ordem(i), style = MaterialTheme.typography.titleSmall, color = CanetaSuave, modifier = Modifier.width(30.dp).padding(top = 5.dp))
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(document.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            "Você assina como ${classificationLabelPublic(item.documentSigner.classification).lowercase()}" +
                                                (item.documentSigner.signedAt?.let { " · assinado em ${DateFormat.format(it)}" } ?: ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CanetaSuave
                                        )
                                        StatusDocumento(document.status)
                                    }
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 8.dp, top = 6.dp))
                                }
                                Fio(Modifier.padding(horizontal = 20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Aba(texto: String, selecionada: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .heightIn(min = 48.dp)
            .background(if (selecionada) MarcaTexto else Caneta)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { selected = selecionada },
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = if (selecionada) Caneta else MarcaTexto,
            modifier = Modifier.padding(vertical = 14.dp)
        )
    }
}
