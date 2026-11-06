package com.letssign.mobile.ui.theme

import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.letssign.mobile.R

/*
 * Identidade "duotom" (sorteio do modo criativo, seed 608168 — ver design/design.json).
 * Duas cores fortes se revezam como fundo: Caneta (azul de caneta esferográfica) e
 * Marca-texto (amarelo de grifar documento). Tinta é o tom profundo da própria caneta,
 * usado só em sombra dura e texto miúdo. Contrastes medidos: Caneta x Marca-texto 7,0:1,
 * Tinta x Marca-texto 13,6:1, Vermelho-carimbo x Marca-texto 7,0:1.
 */
val Caneta = Color(0xFF1A2AB8)
val MarcaTexto = Color(0xFFF4E04D)
val Tinta = Color(0xFF0B0F3B)
val VermelhoCarimbo = Color(0xFF8C1414)
/** Caneta a 80% sobre marca-texto (texto secundário, 5,4:1). */
val CanetaSuave = Color(0xFF464EA3)
/** Marca-texto a 80% sobre caneta (texto secundário em fundo azul, 5,2:1). */
val MarcaSuave = Color(0xFFC8BC62)
/** Papel do PDF e do bloco de assinatura (o documento real é branco). */
val Papel = Color(0xFFFFFFFF)

// Nomes antigos mantidos para não quebrar referências fora da UI.
val LetsSignPink = Caneta
val LetsSignNavy = Tinta

val InstrumentSerif = FontFamily(
    Font(R.font.instrument_serif_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic)
)
val SourceSans = FontFamily(
    Font(R.font.source_sans_3_regular, FontWeight.Normal),
    Font(R.font.source_sans_3_semibold, FontWeight.SemiBold),
    Font(R.font.source_sans_3_bold, FontWeight.Bold)
)

private val Cores = lightColorScheme(
    primary = Caneta,
    onPrimary = MarcaTexto,
    primaryContainer = Caneta,
    onPrimaryContainer = MarcaTexto,
    secondary = Tinta,
    onSecondary = MarcaTexto,
    background = MarcaTexto,
    onBackground = Caneta,
    surface = MarcaTexto,
    onSurface = Caneta,
    surfaceVariant = MarcaTexto,
    onSurfaceVariant = CanetaSuave,
    outline = Caneta,
    error = VermelhoCarimbo,
    onError = Papel
)

private fun serif(size: Int, line: Int, spacing: Float = 0f) =
    TextStyle(fontFamily = InstrumentSerif, fontSize = size.sp, lineHeight = line.sp, letterSpacing = spacing.sp)

private fun sans(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, spacing: Float = 0f) =
    TextStyle(fontFamily = SourceSans, fontSize = size.sp, lineHeight = line.sp, fontWeight = weight, letterSpacing = spacing.sp)

val Tipos = Typography(
    displayLarge = serif(64, 64, -1.5f),
    displayMedium = serif(52, 54, -1f),
    displaySmall = serif(40, 42, -0.5f),
    headlineLarge = serif(36, 38, -0.5f),
    headlineMedium = serif(30, 32, -0.3f),
    headlineSmall = serif(26, 28),
    titleLarge = serif(24, 28),
    titleMedium = sans(17, 22, FontWeight.SemiBold),
    titleSmall = sans(15, 20, FontWeight.SemiBold),
    bodyLarge = sans(17, 24),
    bodyMedium = sans(15, 21),
    bodySmall = sans(13, 18),
    labelLarge = sans(15, 20, FontWeight.Bold, 0.2f),
    labelMedium = sans(12, 16, FontWeight.Bold, 1.4f),
    labelSmall = sans(11, 14, FontWeight.Bold, 1.2f)
)

@Composable
fun LetsSignTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    // O duotom é a identidade: mesmo esquema no modo escuro (fundo azul já domina metade das telas).
    MaterialTheme(colorScheme = Cores, typography = Tipos, content = content)
}

/**
 * true quando o sistema pediu para remover animações (Opções do desenvolvedor →
 * "Escala de duração do Animator" = 0, ou Acessibilidade → "Remover animações").
 * As peças animadas usam isso para mostrar direto o estado final.
 */
@Composable
fun movimentoReduzido(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        } catch (e: Exception) {
            false
        }
    }
}
