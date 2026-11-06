package com.letssign.mobile.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.letssign.mobile.data.local.entity.DocumentStatus
import com.letssign.mobile.data.local.entity.SignatureStatus
import com.letssign.mobile.ui.theme.Caneta
import com.letssign.mobile.ui.theme.CanetaSuave
import com.letssign.mobile.ui.theme.MarcaSuave
import com.letssign.mobile.ui.theme.MarcaTexto
import com.letssign.mobile.ui.theme.Tinta
import com.letssign.mobile.ui.theme.VermelhoCarimbo
import com.letssign.mobile.ui.theme.movimentoReduzido
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ------------------------------------------------------------------ neobrutalista contido */

/** Sombra dura deslocada (sem blur) + borda grossa: o "bloco" do sistema. */
fun Modifier.blocoDuro(
    fundo: Color,
    sombra: Dp = 4.dp,
    borda: Color = Tinta,
    espessura: Dp = 2.dp
): Modifier = this
    .drawBehind {
        val s = sombra.toPx()
        if (s > 0f) drawRect(Tinta, topLeft = Offset(s, s), size = size)
    }
    .background(fundo, RectangleShape)
    .border(espessura, borda, RectangleShape)

/** Fio fino de diagramação. */
@Composable
fun Fio(modifier: Modifier = Modifier, cor: Color = LocalContentColor.current, espessura: Dp = 1.dp) {
    Box(modifier.fillMaxWidth().height(espessura).background(cor))
}

/** Fio duplo (grosso + fino), como o que separa a manchete do corpo no jornal. */
@Composable
fun FioDuplo(modifier: Modifier = Modifier, cor: Color = LocalContentColor.current) {
    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(3.dp).background(cor))
        Spacer(Modifier.height(2.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(cor))
    }
}

@Composable
fun FioVertical(modifier: Modifier = Modifier, cor: Color = LocalContentColor.current) {
    Box(modifier.fillMaxHeight().width(1.dp).background(cor))
}

/** Rótulo de seção em caixa-alta espaçada, com fio que corre até a margem. */
@Composable
fun Rubrica(texto: String, modifier: Modifier = Modifier, cor: Color = LocalContentColor.current) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(texto.uppercase(), style = MaterialTheme.typography.labelMedium, color = cor)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(1.dp).background(cor))
    }
}

private val dataCabecalho = SimpleDateFormat("d MMM yyyy", Locale("pt", "BR"))

/**
 * Cabeçalho de capa: faixa azul (Caneta) com a linha de edição, fio duplo e a manchete
 * em serifada. Ocupa a status bar (edge-to-edge).
 */
@Composable
fun Manchete(
    titulo: String,
    modifier: Modifier = Modifier,
    edicao: String = "LetsSign",
    linhaFina: String? = null,
    compacta: Boolean = false,
    paddingInferior: Dp? = null,
    onBack: (() -> Unit)? = null,
    acoes: @Composable RowScope.() -> Unit = {},
    rodape: @Composable ColumnScope.() -> Unit = {}
) {
    CompositionLocalProvider(LocalContentColor provides MarcaTexto) {
        Column(
            modifier
                .fillMaxWidth()
                .background(Caneta)
                .statusBarsPadding()
                .padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = paddingInferior ?: if (compacta) 10.dp else 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
                Text(
                    edicao.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).offset(x = if (onBack != null) (-12).dp else 0.dp)
                )
                acoes()
            }
            Column(Modifier.padding(end = 12.dp)) {
                FioDuplo()
                Spacer(Modifier.height(if (compacta) 6.dp else 10.dp))
                Text(
                    titulo,
                    style = if (compacta) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineLarge,
                    maxLines = if (compacta) 2 else 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (linhaFina != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(linhaFina, style = MaterialTheme.typography.bodyMedium, color = MarcaSuave)
                }
                rodape()
            }
        }
    }
}

/** Linha de edição do dia ("5 nov. 2026"). */
fun edicaoDoDia(prefixo: String): String = "$prefixo · ${dataCabecalho.format(Date())}"

/* ------------------------------------------------------------------ botões */

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    BotaoDuro(text, modifier.fillMaxWidth(), isLoading, enabled, cheio = true, onClick = onClick)
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    BotaoDuro(text, modifier.fillMaxWidth(), false, enabled, cheio = false, onClick = onClick)
}

/**
 * Botão-bloco: borda grossa e sombra dura. Ao pressionar, o bloco "afunda" sobre a
 * própria sombra (deslocamento animado), como um carimbo apertado no papel.
 */
@Composable
fun BotaoDuro(
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    cheio: Boolean = true,
    fundoCheio: Color = Caneta,
    textoCheio: Color = MarcaTexto,
    contornoClaro: Boolean = false,
    onClick: () -> Unit
) {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val ativo = enabled && !isLoading
    val sombraBase = if (cheio) 4.dp else 0.dp
    val deslocamento by animateDpAsState(if (pressionado && ativo) sombraBase else 0.dp, tween(70), label = "afundar")
    val fundo = if (cheio && ativo) fundoCheio else Color.Transparent
    val corContorno = if (contornoClaro) MarcaTexto else Caneta
    val claro = contornoClaro || fundoCheio == MarcaTexto
    val corTexto = when {
        !ativo -> if (claro) MarcaSuave else CanetaSuave
        cheio -> textoCheio
        else -> corContorno
    }
    Box(
        modifier
            .heightIn(min = 52.dp)
            .padding(end = sombraBase, bottom = sombraBase)
            .offset(x = deslocamento, y = deslocamento)
            .blocoDuro(
                fundo = fundo,
                sombra = if (ativo) sombraBase - deslocamento else 0.dp,
                borda = if (cheio && ativo) Tinta else if (ativo) corContorno else corTexto
            )
            .clickable(interactionSource = interacao, indication = null, enabled = ativo, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(18.dp), color = corTexto, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = corTexto, textAlign = TextAlign.Center)
        }
    }
}

/* ------------------------------------------------------------------ campos */

@Composable
fun CampoTexto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RectangleShape,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Tinta,
            unfocusedBorderColor = Caneta,
            focusedLabelColor = Tinta,
            unfocusedLabelColor = CanetaSuave,
            focusedTextColor = Tinta,
            unfocusedTextColor = Caneta,
            cursorColor = Tinta,
            focusedContainerColor = Color.White.copy(alpha = 0.35f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.22f),
            focusedTrailingIconColor = Tinta,
            unfocusedTrailingIconColor = Caneta
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/** Mensagem de erro com marca vermelha de carimbo à esquerda. */
@Composable
fun AvisoErro(texto: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .semantics { contentDescription = "Erro: $texto" }
    ) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(VermelhoCarimbo))
        Text(
            texto,
            color = VermelhoCarimbo,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 10.dp, top = 2.dp, bottom = 2.dp)
        )
    }
}

/* ------------------------------------------------------------------ estados */

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val linhas = message.split("\n")
            Text(
                linhas.first(),
                style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                color = Caneta,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Box(Modifier.width(48.dp).height(2.dp).background(Caneta))
            if (linhas.size > 1) {
                Spacer(Modifier.height(10.dp))
                Text(
                    linhas.drop(1).joinToString("\n"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CanetaSuave,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun Carregando(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Caneta, strokeWidth = 3.dp)
    }
}

/* ------------------------------------------------------------------ status */

fun documentStatusLabel(status: DocumentStatus): String = when (status) {
    DocumentStatus.DRAFT -> "Rascunho"
    DocumentStatus.SENDING_FAILED -> "Falha no envio"
    DocumentStatus.SENT -> "Enviado"
    DocumentStatus.PARTIALLY_SIGNED -> "Parcialmente assinado"
    DocumentStatus.COMPLETED -> "Concluído"
}

/** Cores com contraste AA sobre o fundo marca-texto. */
fun documentStatusColor(status: DocumentStatus): Color = when (status) {
    DocumentStatus.DRAFT -> CanetaSuave
    DocumentStatus.SENDING_FAILED -> VermelhoCarimbo
    DocumentStatus.SENT -> Caneta
    DocumentStatus.PARTIALLY_SIGNED -> Caneta
    DocumentStatus.COMPLETED -> Tinta
}

fun signatureStatusLabel(status: SignatureStatus): String = when (status) {
    SignatureStatus.PENDING -> "Pendente"
    SignatureStatus.SIGNED -> "Assinado"
}

enum class Marcador { VAZIO, METADE, CHEIO, FALHA }

fun marcadorDe(status: DocumentStatus): Marcador = when (status) {
    DocumentStatus.DRAFT, DocumentStatus.SENT -> Marcador.VAZIO
    DocumentStatus.PARTIALLY_SIGNED -> Marcador.METADE
    DocumentStatus.COMPLETED -> Marcador.CHEIO
    DocumentStatus.SENDING_FAILED -> Marcador.FALHA
}

/** Marcador desenhado (círculo vazio / meio / cheio / X): o status não depende só da cor. */
@Composable
fun MarcadorStatus(marcador: Marcador, cor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(12.dp)) {
        val r = size.minDimension / 2f
        val stroke = Stroke(width = 1.6.dp.toPx())
        when (marcador) {
            Marcador.VAZIO -> drawCircle(cor, r - stroke.width / 2, style = stroke)
            Marcador.CHEIO -> drawCircle(cor, r)
            Marcador.METADE -> {
                drawCircle(cor, r - stroke.width / 2, style = stroke)
                drawArc(cor, 90f, 180f, true)
            }
            Marcador.FALHA -> {
                drawLine(cor, Offset(1f, 1f), Offset(size.width - 1f, size.height - 1f), stroke.width)
                drawLine(cor, Offset(size.width - 1f, 1f), Offset(1f, size.height - 1f), stroke.width)
            }
        }
    }
}

@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier, marcador: Marcador? = null) {
    Row(
        modifier
            .border(BorderStroke(1.5.dp, color), RectangleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (marcador != null) {
            MarcadorStatus(marcador, color)
            Spacer(Modifier.width(6.dp))
        }
        Text(text.uppercase(), color = color, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
fun StatusDocumento(status: DocumentStatus, modifier: Modifier = Modifier) {
    StatusChip(documentStatusLabel(status), documentStatusColor(status), modifier, marcadorDe(status))
}

/* ------------------------------------------------------------------ peça autoral: rubrica */

/**
 * Rubrica desenhada em coordenadas 0..300 x 0..110. Dois traços: o nome corrido e o
 * sublinhado de fecho. Usada na capa do login.
 */
private fun tracoNome(): Path = Path().apply {
    // laço alto inicial (maiúscula), letras corridas e um "S" grande no meio
    moveTo(18f, 72f)
    cubicTo(30f, 22f, 62f, 2f, 52f, 30f)
    cubicTo(44f, 52f, 22f, 96f, 40f, 92f)
    cubicTo(58f, 88f, 64f, 62f, 74f, 60f)
    cubicTo(84f, 58f, 78f, 80f, 90f, 78f)
    cubicTo(100f, 76f, 104f, 50f, 110f, 40f)
    cubicTo(114f, 32f, 108f, 78f, 122f, 76f)
    cubicTo(132f, 74f, 136f, 60f, 142f, 62f)
    cubicTo(150f, 64f, 138f, 80f, 150f, 79f)
    cubicTo(162f, 78f, 170f, 40f, 180f, 22f)
    cubicTo(186f, 12f, 170f, 20f, 172f, 40f)
    cubicTo(174f, 60f, 196f, 62f, 186f, 80f)
    cubicTo(178f, 94f, 160f, 84f, 176f, 76f)
    cubicTo(190f, 70f, 200f, 68f, 206f, 72f)
    cubicTo(212f, 76f, 214f, 64f, 220f, 64f)
    cubicTo(228f, 64f, 224f, 80f, 234f, 76f)
    cubicTo(244f, 72f, 240f, 104f, 232f, 100f)
    cubicTo(224f, 96f, 250f, 70f, 262f, 66f)
    cubicTo(272f, 62f, 270f, 78f, 280f, 74f)
    cubicTo(288f, 71f, 292f, 62f, 296f, 58f)
}

private fun tracoFecho(): Path = Path().apply {
    moveTo(56f, 106f)
    cubicTo(120f, 98f, 210f, 104f, 292f, 90f)
}

/**
 * Peça animada: a rubrica se desenha com a ponta da caneta visível; o sublinhado fecha
 * no fim. Tocar reescreve. Com animações desligadas no sistema, aparece pronta.
 */
@Composable
fun RubricaAnimada(
    modifier: Modifier = Modifier,
    cor: Color = MarcaTexto,
    duracaoMs: Int = 1800,
    progressoFixo: Float? = null
) {
    val reduzido = movimentoReduzido()
    val progresso = remember { Animatable(if (reduzido) 1f else 0f) }
    var vez by remember { mutableIntStateOf(0) }
    LaunchedEffect(vez, reduzido) {
        if (reduzido) {
            progresso.snapTo(1f)
        } else {
            progresso.snapTo(0f)
            progresso.animateTo(1f, tween(duracaoMs, easing = FastOutSlowInEasing))
        }
    }
    val nome = remember { tracoNome() }
    val fecho = remember { tracoFecho() }
    val medidaNome = remember { PathMeasure().apply { setPath(nome, false) } }
    val medidaFecho = remember { PathMeasure().apply { setPath(fecho, false) } }
    val parcial = remember { Path() }
    Canvas(
        modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "Reescrever a rubrica"
            ) { vez++ }
            .clearAndSetSemantics { }
    ) {
        val escala = minOf(size.width / 300f, size.height / 110f)
        val dx = (size.width - 300f * escala) / 2f
        val p = progressoFixo ?: progresso.value
        // 82% do tempo para o nome, 18% para o sublinhado
        val pNome = (p / 0.82f).coerceIn(0f, 1f)
        val pFecho = ((p - 0.82f) / 0.18f).coerceIn(0f, 1f)
        val tracado = Stroke(width = 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        withTransform({
            translate(dx, 0f)
            scale(escala, escala, Offset.Zero)
        }) {
        parcial.reset()
        medidaNome.getSegment(0f, medidaNome.length * pNome, parcial, true)
        drawPath(parcial, cor, style = tracado)
        if (pFecho > 0f) {
            parcial.reset()
            medidaFecho.getSegment(0f, medidaFecho.length * pFecho, parcial, true)
            drawPath(parcial, cor, style = Stroke(width = 2.4f, cap = StrokeCap.Round))
        }
        // ponta da caneta enquanto escreve
        if (p in 0.001f..0.999f) {
            val ponta = if (pFecho > 0f) medidaFecho.getPosition(medidaFecho.length * pFecho)
            else medidaNome.getPosition(medidaNome.length * pNome)
            drawCircle(cor, radius = 5f, center = ponta)
        }
        }
    }
}

/** Linha de assinatura com o "X" de onde assinar, sob a rubrica. */
@Composable
fun LinhaDeAssinatura(legenda: String, modifier: Modifier = Modifier, cor: Color = MarcaTexto) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("×", style = MaterialTheme.typography.titleLarge, color = cor)
            Spacer(Modifier.width(6.dp))
            Box(Modifier.weight(1f).padding(bottom = 6.dp).height(1.dp).background(cor))
        }
        Text(legenda.uppercase(), style = MaterialTheme.typography.labelSmall, color = MarcaSuave, modifier = Modifier.padding(start = 22.dp, top = 4.dp))
    }
}

/* ------------------------------------------------------------------ peça: carimbo */

/**
 * Carimbo "ASSINADO" que cai sobre o campo: entra grande e girado, assenta com um leve
 * tranco. Estático quando as animações do sistema estão desligadas.
 */
@Composable
fun CarimboAssinado(modifier: Modifier = Modifier, texto: String = "ASSINADO") {
    val reduzido = movimentoReduzido()
    val t = remember { Animatable(if (reduzido) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reduzido) t.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
    }
    val v = t.value
    val escala = 1.9f - 0.9f * v + if (v > 0.85f) (1f - v) * 0.4f else 0f
    Box(
        modifier
            .graphicsLayer {
                scaleX = escala; scaleY = escala
                rotationZ = -10f - 8f * (1f - v)
                alpha = v.coerceIn(0f, 1f)
            }
            .border(2.dp, VermelhoCarimbo, RoundedCornerShape(3.dp))
            .padding(2.dp)
            .border(1.dp, VermelhoCarimbo, RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp)
            .semantics { contentDescription = "Campo assinado" }
    ) {
        Text(texto, color = VermelhoCarimbo, style = MaterialTheme.typography.labelSmall)
    }
}

/* ------------------------------------------------------------------ utilitários de layout */

/** Coluna de jornal: duas colunas com fio vertical entre elas. */
@Composable
fun DuasColunas(
    modifier: Modifier = Modifier,
    esquerda: @Composable ColumnScope.() -> Unit,
    direita: @Composable ColumnScope.() -> Unit
) {
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(Modifier.weight(1f).padding(end = 12.dp), content = esquerda)
        FioVertical(cor = Caneta)
        Column(Modifier.weight(1f).padding(start = 12.dp), content = direita)
    }
}

/** Rodapé fixo de ações com fio grosso em cima. */
@Composable
fun BarraDeAcoes(modifier: Modifier = Modifier, conteudo: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .background(MarcaTexto)
            .drawBehind { drawRect(Caneta, size = Size(size.width, 2.dp.toPx())) }
            .navigationBarsPadding()
            .padding(PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = conteudo
    )
}

/** Número de ordem com dois dígitos, para índices em estilo de sumário. */
fun ordem(i: Int): String = (i + 1).toString().padStart(2, '0')

@Composable
fun Iniciais(nome: String, modifier: Modifier = Modifier, fundo: Color = Caneta, texto: Color = MarcaTexto) {
    val ini = nome.trim().split(" ").filter { it.isNotBlank() }.let {
        ((it.firstOrNull()?.take(1) ?: "") + (if (it.size > 1) it.last().take(1) else "")).uppercase()
    }
    Box(modifier.size(40.dp).background(fundo, CircleShape), contentAlignment = Alignment.Center) {
        Text(ini, color = texto, style = MaterialTheme.typography.titleSmall)
    }
}

val TextoMiudo: TextStyle @Composable get() = MaterialTheme.typography.bodySmall

/* ------------------------------------------------------------------ wizard */

private val nomesEtapas = listOf("Arquivo", "Quem", "Onde", "Envio")

/** Régua de etapas do wizard, dentro da capa azul: 4 blocos, os feitos preenchidos. */
@Composable
fun EtapasWizard(atual: Int, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .semantics { contentDescription = "Etapa $atual de 4: ${nomesEtapas[atual - 1]}" },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        nomesEtapas.forEachIndexed { i, nome ->
            val n = i + 1
            Column(Modifier.weight(1f)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .then(
                            if (n <= atual) Modifier.background(MarcaTexto)
                            else Modifier.border(1.dp, MarcaSuave, RectangleShape)
                        )
                )
                Text(
                    "$n · $nome",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (n == atual) MarcaTexto else MarcaSuave,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/** Permite quebrar nome de arquivo em "-", "_" e "." em vez de no meio da extensão. */
fun quebravel(nome: String): String = nome.replace("-", "-\u200B").replace("_", "_\u200B").replace(".", "\u200B.")

/** Rodapé do wizard: Voltar/Cancelar e Continuar lado a lado (economiza altura em 640dp). */
@Composable
fun AcoesWizard(
    voltar: String,
    onVoltar: () -> Unit,
    avancar: String,
    onAvancar: () -> Unit,
    isLoading: Boolean = false,
    voltarHabilitado: Boolean = true
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SecondaryButton(voltar, Modifier.weight(1f), enabled = voltarHabilitado, onClick = onVoltar)
        PrimaryButton(avancar, Modifier.weight(1.5f), isLoading = isLoading, onClick = onAvancar)
    }
}
