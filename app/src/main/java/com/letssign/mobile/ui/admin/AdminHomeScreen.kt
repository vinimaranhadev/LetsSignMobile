package com.letssign.mobile.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.letssign.mobile.di.AppContainer
import com.letssign.mobile.session.SessionUser
import com.letssign.mobile.ui.admin.documents.DocumentsListScreen
import com.letssign.mobile.ui.admin.signers.SignersListScreen
import com.letssign.mobile.ui.common.Manchete
import com.letssign.mobile.ui.common.edicaoDoDia
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.MarcaTexto

@Composable
fun AdminHomeScreen(
    container: AppContainer,
    admin: SessionUser,
    onNewDocument: () -> Unit,
    onOpenDocument: (Long) -> Unit,
    onAddSigner: () -> Unit,
    onEditSigner: (Long) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MarcaTexto,
        topBar = {
            Manchete(
                titulo = if (selectedTab == 0) "Documentos" else "Signatários",
                edicao = edicaoDoDia("Painel do admin"),
                paddingInferior = 0.dp,
                acoes = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair")
                    }
                },
                rodape = {
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Aba("Documentos", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                        Aba("Signatários", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> DocumentsListScreen(
                    container = container,
                    adminId = admin.id,
                    onNewDocument = onNewDocument,
                    onOpenDocument = onOpenDocument
                )
                else -> SignersListScreen(
                    container = container,
                    adminId = admin.id,
                    onAddSigner = onAddSigner,
                    onEditSigner = onEditSigner
                )
            }
        }
    }
}

/** Aba de capa: a selecionada vira bloco amarelo encaixado no fundo azul. */
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
