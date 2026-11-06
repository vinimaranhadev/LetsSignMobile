package com.letssign.mobile.ui.admin.newdocument

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.letssign.mobile.data.local.entity.SignerClassification
import com.letssign.mobile.data.local.entity.UserEntity
import com.letssign.mobile.ui.common.AcoesWizard
import com.letssign.mobile.ui.common.AvisoErro
import com.letssign.mobile.ui.common.BarraDeAcoes
import com.letssign.mobile.ui.common.EmptyState
import com.letssign.mobile.ui.common.EtapasWizard
import com.letssign.mobile.ui.common.Fio
import com.letssign.mobile.ui.common.Manchete
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.SecondaryButton
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaTexto
import com.letssign.mobile.ui.theme.Tinta

private fun classificationLabel(classification: SignerClassification): String =
    classificationLabelPublic(classification)

@Composable
fun SignerSelectStep(
    viewModel: NewDocumentViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val availableSigners by viewModel.availableSigners.collectAsState()

    Scaffold(
        containerColor = MarcaTexto,
        topBar = {
            Manchete(
                titulo = "Quem assina",
                edicao = state.title.ifBlank { "Novo documento" },
                compacta = true,
                onBack = onBack,
                rodape = { EtapasWizard(2) }
            )
        },
        bottomBar = {
            BarraDeAcoes {
                state.errorMessage?.let { AvisoErro(it) }
                Text(
                    "Selecionados: ${state.selectedSigners.size} (mínimo 1 obrigatório)",
                    style = MaterialTheme.typography.bodySmall,
                    color = CanetaSuave
                )
                AcoesWizard("Voltar", onBack, "Continuar", { viewModel.confirmSigners(onNext) }, state.isSavingStep)
            }
        }
    ) { padding ->
        CompositionLocalProvider(LocalContentColor provides Caneta) {
            if (availableSigners.isEmpty()) {
                EmptyState(
                    "Nenhum signatário cadastrado.\nCadastre signatários antes de criar o documento.",
                    Modifier.padding(padding)
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(availableSigners, key = { it.id }) { signer ->
                        val selected = state.selectedSigners.firstOrNull { it.signer.id == signer.id }
                        SignerSelectRow(
                            signer = signer,
                            classification = selected?.classification,
                            onToggle = { checked -> viewModel.toggleSigner(signer, checked) },
                            onClassificationChange = { viewModel.changeClassification(signer.id, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignerSelectRow(
    signer: UserEntity,
    classification: SignerClassification?,
    onToggle: (Boolean) -> Unit,
    onClassificationChange: (SignerClassification) -> Unit
) {
    val isChecked = classification != null
    Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .toggleable(value = isChecked, role = Role.Checkbox, onValueChange = onToggle)
                .heightIn(min = 64.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // caixa de marcar quadrada, como num formulário em papel
            Box(
                Modifier
                    .size(26.dp)
                    .border(2.dp, if (isChecked) Tinta else Caneta, RectangleShape)
                    .background(if (isChecked) Caneta else MarcaTexto),
                contentAlignment = Alignment.Center
            ) {
                if (isChecked) Icon(Icons.Filled.Check, contentDescription = null, tint = MarcaTexto, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(signer.fullName, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(signer.email, style = MaterialTheme.typography.bodySmall, color = CanetaSuave, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (isChecked) {
            // Classificação como régua segmentada (substitui o dropdown, mesmas 3 opções)
            Text(
                "ASSINA COMO",
                style = MaterialTheme.typography.labelSmall,
                color = CanetaSuave,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp)
            )
            Row(
                Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 14.dp)
                    .fillMaxWidth()
                    .border(2.dp, Caneta, RectangleShape)
            ) {
                SignerClassification.values().forEachIndexed { i, option ->
                    val ativo = option == classification
                    if (i > 0) Box(Modifier.width(2.dp).height(48.dp).background(Caneta))
                    Box(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .background(if (ativo) Caneta else MarcaTexto)
                            .clickable(role = Role.RadioButton) { onClassificationChange(option) }
                            .semantics { selected = ativo },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            classificationLabel(option),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (ativo) MarcaTexto else Caneta,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
        Fio(Modifier.padding(horizontal = 20.dp))
    }
}
