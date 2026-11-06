package com.letssign.mobile.ui.admin.signers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.letssign.mobile.di.AppContainer
import com.letssign.mobile.ui.common.AvisoErro
import com.letssign.mobile.ui.common.BarraDeAcoes
import com.letssign.mobile.ui.common.CampoTexto
import com.letssign.mobile.ui.common.Manchete
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.Rubrica
import com.letssign.mobile.ui.common.SecondaryButton
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaTexto

@Composable
fun SignerFormScreen(
    container: AppContainer,
    adminId: Long,
    signerId: Long?,
    onBack: () -> Unit
) {
    val viewModel: SignersViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SignersViewModel(container.userRepository, adminId) }
        }
    )
    val signers by viewModel.signers.collectAsState()
    val formState by viewModel.formState.collectAsState()
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(signerId, signers) {
        if (initialized && (signerId == null || signerId == -1L)) return@LaunchedEffect
        if (signerId != null && signerId != -1L) {
            signers.firstOrNull { it.id == signerId }?.let {
                viewModel.loadForEdit(it)
                initialized = true
            }
        } else {
            viewModel.startNew()
            initialized = true
        }
    }

    Scaffold(
        containerColor = MarcaTexto,
        topBar = {
            Manchete(
                titulo = if (formState.isEditing) "Editar signatário" else "Cadastrar signatário",
                edicao = "Signatários · ficha",
                compacta = true,
                onBack = onBack
            )
        },
        bottomBar = {
            BarraDeAcoes(Modifier.imePadding()) {
                PrimaryButton(
                    text = if (formState.isEditing) "Salvar alterações" else "Cadastrar signatário",
                    isLoading = formState.isSaving,
                    onClick = { viewModel.save(onSuccess = onBack) }
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
                Column(
                    Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Rubrica("Identificação")
                    CampoTexto(formState.fullName, viewModel::onFullNameChange, "Nome completo *")
                    Spacer(Modifier.height(6.dp))
                    Rubrica("Contato e acesso")
                    CampoTexto(formState.email, viewModel::onEmailChange, "E-mail (login) *", keyboardType = KeyboardType.Email)
                    CampoTexto(formState.phone, viewModel::onPhoneChange, "Telefone *", keyboardType = KeyboardType.Phone)
                    CampoTexto(
                        formState.password,
                        viewModel::onPasswordChange,
                        if (formState.isEditing) "Nova senha (opcional)" else "Senha *",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Text(
                        "* obrigatório. A senha precisa de ao menos 6 caracteres.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CanetaSuave
                    )
                    formState.errorMessage?.let { AvisoErro(it) }
                }
            }
        }
    }
}
