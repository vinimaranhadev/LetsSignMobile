package com.letssign.mobile.ui.admin.signers

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
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
import com.letssign.mobile.data.local.entity.UserEntity
import com.letssign.mobile.di.AppContainer
import com.letssign.mobile.ui.common.BarraDeAcoes
import com.letssign.mobile.ui.common.EmptyState
import com.letssign.mobile.ui.common.Fio
import com.letssign.mobile.ui.common.Iniciais
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.Rubrica
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave

@Composable
fun SignersListScreen(
    container: AppContainer,
    adminId: Long,
    onAddSigner: () -> Unit,
    onEditSigner: (Long) -> Unit
) {
    val viewModel: SignersViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SignersViewModel(container.userRepository, adminId) }
        }
    )
    val signers by viewModel.signers.collectAsState()

    CompositionLocalProvider(LocalContentColor provides Caneta) {
        Column(Modifier.fillMaxSize()) {
            if (signers.isEmpty()) {
                EmptyState("Nenhum signatário cadastrado ainda.\nCadastre quem vai assinar os documentos.", Modifier.weight(1f))
            } else {
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    item {
                        Rubrica(
                            "Cadastro · ${signers.size} ${if (signers.size == 1) "pessoa" else "pessoas"}",
                            Modifier.widthIn(max = 720.dp).padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 4.dp)
                        )
                    }
                    items(signers, key = { it.id }) { signer ->
                        SignerRow(signer, onClick = { onEditSigner(signer.id) })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
            BarraDeAcoes {
                PrimaryButton(text = "+  Cadastrar signatário", onClick = onAddSigner)
            }
        }
    }
}

@Composable
private fun SignerRow(signer: UserEntity, onClick: () -> Unit) {
    Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clickable(onClickLabel = "Editar ${signer.fullName}", onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Iniciais(signer.fullName)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(signer.fullName, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(signer.email, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(signer.phone, style = MaterialTheme.typography.bodySmall, color = CanetaSuave)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.padding(12.dp))
        }
        Fio(Modifier.padding(horizontal = 20.dp))
    }
}
