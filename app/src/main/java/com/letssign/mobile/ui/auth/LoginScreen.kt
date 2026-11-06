package com.letssign.mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.letssign.mobile.data.local.AppDatabase
import com.letssign.mobile.data.local.entity.UserRole
import com.letssign.mobile.di.AppContainer
import com.letssign.mobile.ui.common.AvisoErro
import com.letssign.mobile.ui.common.CampoTexto
import com.letssign.mobile.ui.common.Fio
import com.letssign.mobile.ui.common.FioDuplo
import com.letssign.mobile.ui.common.LinhaDeAssinatura
import com.letssign.mobile.ui.common.PrimaryButton
import com.letssign.mobile.ui.common.Rubrica
import com.letssign.mobile.ui.common.RubricaAnimada
import com.letssign.mobile.ui.common.blocoDuro
import com.letssign.mobile.ui.common.edicaoDoDia
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.Capa
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaSuave
import com.letssign.mobile.ui.theme.MarcaTexto

@Composable
fun LoginScreen(
    container: AppContainer,
    onLoggedIn: (UserRole) -> Unit
) {
    val viewModel: LoginViewModel = viewModel(
        factory = viewModelFactory {
            initializer { LoginViewModel(container.userRepository, container.sessionManager) }
        }
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.loggedInRole) {
        state.loggedInRole?.let { onLoggedIn(it) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MarcaTexto)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Capa marrom clarinho: nome do jornal + rubrica que se escreve sozinha
        CompositionLocalProvider(LocalContentColor provides Caneta) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Capa)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp, bottom = 16.dp)
            ) {
                Text(
                    edicaoDoDia("Assinatura eletrônica").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                FioDuplo()
                Text(
                    "LetsSign",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 60.sp, lineHeight = 64.sp),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Fio()
                Spacer(Modifier.height(6.dp))
                Text(
                    "é simples assinar!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                    color = MarcaSuave
                )
                Spacer(Modifier.height(6.dp))
                RubricaAnimada(
                    Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                )
                LinhaDeAssinatura("Assinatura do titular")
            }
        }

        // Corpo branco: o acesso
        CompositionLocalProvider(LocalContentColor provides Caneta) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.widthIn(max = 480.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Rubrica("Acesso")
                    CampoTexto(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "E-mail",
                        keyboardType = KeyboardType.Email
                    )
                    CampoTexto(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Senha",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    state.errorMessage?.let { AvisoErro(it) }
                    Spacer(Modifier.height(4.dp))
                    PrimaryButton(text = "Entrar", isLoading = state.isLoading, onClick = viewModel::login)
                    Spacer(Modifier.height(10.dp))

                    // Nota de rodapé com o acesso de demonstração
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .blocoDuro(fundo = MarcaTexto, sombra = 0.dp, borda = Caneta, espessura = 1.dp)
                            .padding(12.dp)
                    ) {
                        Text("*", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 8.dp))
                        Column {
                            Text("ADMIN DE DEMONSTRAÇÃO", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${AppDatabase.SEED_ADMIN_EMAIL} / ${AppDatabase.SEED_ADMIN_PASSWORD}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Signatários são cadastrados pelo admin dentro do app.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CanetaSuave
                            )
                        }
                    }
                }
            }
        }
        Box(Modifier.height(8.dp))
    }
}
