package com.letssign.mobile.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.geometry.Offset
import android.provider.Settings
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.letssign.mobile.data.local.AppDatabase
import com.letssign.mobile.data.local.entity.DocumentEntity
import com.letssign.mobile.data.local.entity.DocumentEventEntity
import com.letssign.mobile.data.local.entity.DocumentEventType
import com.letssign.mobile.data.local.entity.DocumentSignerEntity
import com.letssign.mobile.data.local.entity.DocumentStatus
import com.letssign.mobile.data.local.entity.SignatureFieldEntity
import com.letssign.mobile.data.local.entity.SignatureStatus
import com.letssign.mobile.data.local.entity.SignerClassification
import com.letssign.mobile.data.local.entity.UserEntity
import com.letssign.mobile.data.local.entity.UserRole
import com.letssign.mobile.di.AppContainer
import com.letssign.mobile.session.SessionUser
import com.letssign.mobile.ui.admin.AdminHomeScreen
import com.letssign.mobile.ui.admin.documents.DocumentDetailScreen
import com.letssign.mobile.ui.admin.newdocument.FieldDraft
import com.letssign.mobile.ui.admin.newdocument.NewDocumentUiState
import com.letssign.mobile.ui.admin.newdocument.NewDocumentViewModel
import com.letssign.mobile.ui.admin.newdocument.PositioningScreen
import com.letssign.mobile.ui.admin.newdocument.ReviewSendScreen
import com.letssign.mobile.ui.admin.newdocument.SelectedSigner
import com.letssign.mobile.ui.admin.newdocument.SignerSelectStep
import com.letssign.mobile.ui.admin.newdocument.TitlePdfStep
import com.letssign.mobile.ui.admin.signers.SignerFormScreen
import com.letssign.mobile.ui.auth.LoginScreen
import com.letssign.mobile.ui.common.LocalPdfPagePreview
import com.letssign.mobile.ui.signer.SignDocumentScreen
import com.letssign.mobile.ui.signer.SignerHomeScreen
import com.letssign.mobile.ui.theme.LetsSignTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Gera PNGs reais das telas (Robolectric NATIVE graphics + Roborazzi) com dados fictícios
 * gravados no Room de verdade. Saída: design/screens/<fase>/<nome>.png na raiz do repo.
 * Rodar: ./gradlew testDebugUnitTest -Pfase=antes|depois
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xhdpi")
class TelasScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var container: AppContainer
    private val admin = SessionUser(ADMIN_ID, "Administrador LetsSign", "admin@letssign.com", UserRole.ADMIN)
    private val signers = mutableListOf<UserEntity>()
    private var docParcial = 0L
    private var dsPendenteMariana = 0L

    private fun out(nome: String): String {
        val root = System.getProperty("repoRoot") ?: "."
        val fase = System.getProperty("fase") ?: "depois"
        val dir = File(root, "design/screens/$fase").apply { mkdirs() }
        return File(dir, "$nome.png").also { it.parentFile?.mkdirs() }.absolutePath
    }

    @Before
    fun setUp() {
        // Banco limpo por teste (o singleton sobrevive entre testes no mesmo sandbox).
        val f = AppDatabase::class.java.getDeclaredField("INSTANCE").apply { isAccessible = true }
        (f.get(null) as? AppDatabase)?.close()
        f.set(null, null)
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        ctx.deleteDatabase("letssign.db")
        container = AppContainer(ctx)
        seed(ctx)
    }

    private fun seed(ctx: Context) = runBlocking {
        val db = AppDatabase.getInstance(ctx)
        val users = db.userDao()
        signers.clear()
        listOf(
            Triple("Mariana Teixeira Lopes", "mariana.lopes@vale-norte.com.br", "(11) 98213-4470"),
            Triple("Carlos Eduardo Nunes", "carlos.nunes@nunesadv.com.br", "(21) 99654-0912"),
            Triple("Juliana Prado Almeida", "juliana.prado@gmail.com", "(31) 98877-1203"),
            Triple("Rogério Batista", "rogerio@batistaengenharia.com", "(41) 3322-9087")
        ).forEach { (n, e, p) ->
            val u = UserEntity(fullName = n, email = e, phone = p, passwordHash = "x", role = UserRole.SIGNER, createdByAdminId = ADMIN_ID)
            signers += u.copy(id = users.insert(u))
        }
        val docs = db.documentDao()
        val ev = db.documentEventDao()
        val dsd = db.documentSignerDao()
        val fd = db.signatureFieldDao()
        val t0 = 1_793_000_000_000L // nov/2026
        fun d(title: String, status: DocumentStatus, v: Int, dt: Long) = DocumentEntity(
            title = title, originalFileName = title.lowercase().replace(" ", "-") + ".pdf", filePath = "/fake/$title.pdf",
            pageCount = 3, ownerAdminId = ADMIN_ID, inviteMessage = "Segue para assinatura.", status = status, version = v,
            createdAt = t0 + dt, sentAt = if (status != DocumentStatus.DRAFT) t0 + dt + 3_600_000 else null,
            completedAt = if (status == DocumentStatus.COMPLETED) t0 + dt + 86_400_000 else null
        )
        docs.insert(d("Proposta comercial 4º trimestre", DocumentStatus.DRAFT, 1, 400_000_000))
        docs.insert(d("Acordo de parceria Vale Norte", DocumentStatus.SENDING_FAILED, 1, 300_000_000))
        docParcial = docs.insert(d("Contrato de prestação de serviços", DocumentStatus.PARTIALLY_SIGNED, 2, 200_000_000))
        docs.insert(d("Termo de confidencialidade", DocumentStatus.COMPLETED, 1, 100_000_000))
        docs.insert(d("Aditivo contratual nº 2", DocumentStatus.SENT, 1, 0))

        val ds1 = dsd.insert(DocumentSignerEntity(documentId = docParcial, signerId = signers[1].id, classification = SignerClassification.CONTRATANTE, status = SignatureStatus.SIGNED, signedAt = t0 + 210_000_000))
        dsPendenteMariana = dsd.insert(DocumentSignerEntity(documentId = docParcial, signerId = signers[0].id, classification = SignerClassification.CONTRATADO))
        dsd.insert(DocumentSignerEntity(documentId = docParcial, signerId = signers[2].id, classification = SignerClassification.TESTEMUNHA))
        fd.insert(SignatureFieldEntity(documentId = docParcial, documentSignerId = dsPendenteMariana, page = 0, xRatio = 0.08f, yRatio = 0.72f, widthRatio = 0.42f, heightRatio = 0.08f))
        fd.insert(SignatureFieldEntity(documentId = docParcial, documentSignerId = ds1, page = 0, xRatio = 0.52f, yRatio = 0.84f, widthRatio = 0.42f, heightRatio = 0.08f))
        listOf(
            Triple(DocumentEventType.CREATED, "Documento criado como rascunho.", 200_000_000L),
            Triple(DocumentEventType.SENT, "Documento enviado para 3 signatário(s).", 203_600_000L),
            Triple(DocumentEventType.SIGNER_SIGNED, "Signatário concluiu a assinatura.", 210_000_000L)
        ).forEach { (t, desc, dt) -> ev.insert(DocumentEventEntity(documentId = docParcial, type = t, description = desc, timestamp = t0 + dt)) }

        // Para a Mariana: um pendente (acima) e um assinado
        val docOk = docs.findById(4)!!.id
        dsd.insert(DocumentSignerEntity(documentId = docOk, signerId = signers[0].id, classification = SignerClassification.TESTEMUNHA, status = SignatureStatus.SIGNED, signedAt = t0 + 150_000_000))
    }

    @Composable
    private fun App(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalPdfPagePreview provides ::fakePdfPage) {
            LetsSignTheme(darkTheme = false) { content() }
        }
    }

    private fun waitText(text: String, substring: Boolean = true) {
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(text, substring = substring, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitForIdle()
    }

    private fun shot(nome: String) {
        compose.mainClock.advanceTimeBy(4_000)
        compose.waitForIdle()
        compose.onRoot().captureRoboImage(out(nome))
    }

    @Test fun t01_login() {
        compose.setContent { App { LoginScreen(container) {} } }
        waitText("Senha")
        shot("01-login")
    }

    @Test @Config(qualifiers = "w411dp-h891dp-xxhdpi")
    fun t01b_login_grande() {
        compose.setContent { App { LoginScreen(container) {} } }
        waitText("Senha")
        shot("01b-login-tela-grande")
    }

    @Test fun t02_admin_documentos() {
        compose.setContent { App { AdminHomeScreen(container, admin, {}, {}, {}, {}, {}) } }
        waitText("Proposta comercial")
        shot("02-admin-documentos")
    }

    @Test fun t03_admin_signatarios() {
        compose.setContent { App { AdminHomeScreen(container, admin, {}, {}, {}, {}, {}) } }
        compose.waitForIdle()
        compose.onAllNodesWithText("Signatários", substring = true, ignoreCase = true)[0].performClick()
        waitText("Mariana")
        shot("03-admin-signatarios")
    }

    @Test fun t04_form_signatario() {
        compose.setContent { App { SignerFormScreen(container, ADMIN_ID, null) {} } }
        waitText("Telefone")
        shot("04-form-signatario")
    }

    @Test fun t05_detalhe_documento() {
        compose.setContent { App { DocumentDetailScreen(container, docParcial, {}, {}) } }
        waitText("Carlos")
        shot("05-detalhe-documento")
    }

    private fun wizardVm(): NewDocumentViewModel {
        val vm = NewDocumentViewModel(container.documentRepository, container.userRepository, ADMIN_ID)
        @Suppress("UNCHECKED_CAST")
        val flow = NewDocumentViewModel::class.java.getDeclaredField("_uiState").apply { isAccessible = true }
            .get(vm) as MutableStateFlow<NewDocumentUiState>
        flow.value = NewDocumentUiState(
            title = "Contrato de locação comercial",
            pdfFileName = "contrato-locacao-loja-12.pdf",
            pdfPath = "/fake/locacao.pdf",
            pageCount = 3,
            documentId = 99,
            selectedSigners = listOf(
                SelectedSigner(signers[0], SignerClassification.CONTRATANTE),
                SelectedSigner(signers[1], SignerClassification.CONTRATADO)
            ),
            fields = listOf(
                FieldDraft(key = "a", signerId = signers[0].id, page = 0, xRatio = 0.06f, yRatio = 0.70f),
                FieldDraft(key = "b", signerId = signers[1].id, page = 0, xRatio = 0.52f, yRatio = 0.84f)
            )
        )
        return vm
    }

    @Test fun t06_wizard_titulo() {
        val vm = wizardVm()
        compose.setContent { App { TitlePdfStep(vm, {}, {}) } }
        waitText("página(s)")
        shot("06-wizard-1-titulo-pdf")
    }

    @Test fun t07_wizard_signatarios() {
        val vm = wizardVm()
        compose.setContent { App { SignerSelectStep(vm, {}, {}) } }
        waitText("Juliana")
        shot("07-wizard-2-signatarios")
    }

    @Test fun t08_wizard_posicionamento() {
        val vm = wizardVm()
        compose.setContent { App { PositioningScreen(vm, {}, {}) } }
        compose.waitForIdle()
        shot("08-wizard-3-posicionamento")
    }

    @Test fun t09_wizard_revisao() {
        val vm = wizardVm()
        compose.setContent { App { ReviewSendScreen(vm, {}, {}) } }
        waitText("Mariana")
        shot("09-wizard-4-revisao")
    }

    @Test fun t10_signatario_home() {
        val mariana = SessionUser(signers[0].id, signers[0].fullName, signers[0].email, UserRole.SIGNER)
        compose.setContent { App { SignerHomeScreen(container, mariana, {}, {}) } }
        waitText("Contrato de prestação")
        shot("10-signatario-pendentes")
    }

    @Test fun t11_assinar_documento() {
        compose.setContent { App { SignDocumentScreen(container, dsPendenteMariana, {}, {}) } }
        waitText("Toque para assinar")
        shot("11-assinar-documento")
    }

    @Test fun t12_pad_assinatura() {
        compose.setContent { App { SignDocumentScreen(container, dsPendenteMariana, {}, {}) } }
        waitText("Toque para assinar")
        compose.onAllNodesWithText("Toque para assinar", substring = true)[0].performClick()
        compose.mainClock.advanceTimeBy(2_000)
        compose.waitForIdle()
        captureScreenRoboImage(out("12-pad-assinatura"))
    }

    /* ---------- telas longas: conteúdo abaixo da dobra (linha do tempo, campos) ---------- */

    @Test @Config(qualifiers = "w360dp-h1300dp-xhdpi")
    fun t05b_detalhe_longo() {
        compose.setContent { App { DocumentDetailScreen(container, docParcial, {}, {}) } }
        waitText("Histórico")
        shot("05b-detalhe-documento-longo")
    }

    @Test @Config(qualifiers = "w360dp-h1100dp-xhdpi")
    fun t08b_posicionamento_longo() {
        val vm = wizardVm()
        compose.setContent { App { PositioningScreen(vm, {}, {}) } }
        waitText("Mariana")
        shot("08b-wizard-3-posicionamento-longo")
    }

    /* ---------- peça animada: quadros reais no tempo ---------- */

    private fun quadros(prefixo: String, tempos: List<Long>) {
        var anterior = 0L
        tempos.forEach { t ->
            compose.mainClock.advanceTimeBy(t - anterior)
            anterior = t
            compose.onRoot().captureRoboImage(out("$prefixo-${t}ms"))
        }
    }

    @Test fun t13_rubrica_quadros() {
        compose.mainClock.autoAdvance = false
        compose.setContent { App { LoginScreen(container) {} } }
        quadros("anim/rubrica", listOf(16, 400, 900, 1400, 2000))
    }

    @Test @Config(qualifiers = "w360dp-h1300dp-xhdpi")
    fun t14_linha_do_tempo_quadros() {
        compose.mainClock.autoAdvance = false
        compose.setContent { App { DocumentDetailScreen(container, docParcial, {}, {}) } }
        // espera os dados do Room chegarem sem deixar a animação correr à toa
        compose.mainClock.advanceTimeBy(16)
        repeat(50) {
            if (compose.onAllNodesWithText("Histórico", substring = true, ignoreCase = true).fetchSemanticsNodes().isEmpty()) {
                Thread.sleep(50); compose.mainClock.advanceTimeBy(16)
            }
        }
        quadros("anim/linha-do-tempo", listOf(16, 300, 700, 1300))
    }

    /** Fluxo real: abrir campo, desenhar no pad, confirmar; o carimbo cai sobre o campo. */
    @Test @Config(qualifiers = "w360dp-h1000dp-xhdpi")
    fun t15_carimbo_apos_assinar() {
        compose.setContent { App { SignDocumentScreen(container, dsPendenteMariana, {}, {}) } }
        waitText("Toque para assinar")
        compose.onAllNodesWithText("Toque para assinar", substring = true)[0].performClick()
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Área para desenhar a assinatura").performTouchInput {
            swipe(Offset(width * 0.1f, height * 0.7f), Offset(width * 0.3f, height * 0.2f), 120)
            swipe(Offset(width * 0.3f, height * 0.6f), Offset(width * 0.8f, height * 0.5f), 120)
        }
        compose.waitForIdle()
        captureScreenRoboImage(out("12b-pad-com-tracos"))
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Confirmar assinatura").performClick()
        compose.mainClock.advanceTimeBy(16)
        quadros("anim/carimbo", listOf(32, 120, 240, 600))
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        shot("11b-assinar-documento-campo-assinado")
    }

    /** Animações desligadas no sistema: no primeiro quadro tudo já aparece pronto. */
    @Test fun t16_sem_animacao() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        Settings.Global.putFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        compose.mainClock.autoAdvance = false
        compose.setContent { App { LoginScreen(container) {} } }
        compose.mainClock.advanceTimeBy(16)
        compose.onRoot().captureRoboImage(out("anim/rubrica-sem-animacao-16ms"))
        Settings.Global.putFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }

    companion object {
        const val ADMIN_ID = 999L

        /** Página fictícia de contrato (o PdfRenderer nativo não existe no JVM). */
        fun fakePdfPage(path: String, page: Int, widthPx: Int): Bitmap {
            val h = (widthPx * 1.414f).toInt()
            val bmp = Bitmap.createBitmap(widthPx, h, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            c.drawColor(Color.WHITE)
            val p = Paint().apply { isAntiAlias = true; color = Color.rgb(40, 40, 40); textSize = widthPx / 26f; isFakeBoldText = true }
            val m = widthPx * 0.08f
            c.drawText("CONTRATO DE LOCAÇÃO COMERCIAL", m, h * 0.08f, p)
            val line = Paint().apply { color = Color.rgb(190, 190, 190); strokeWidth = widthPx / 140f }
            var y = h * 0.14f
            var i = 0
            while (y < h * 0.64f) {
                val end = if (i % 6 == 5) widthPx * 0.55f else widthPx - m
                c.drawLine(m, y, end, y, line)
                y += h * 0.028f; i++
                if (i % 6 == 0) y += h * 0.02f
            }
            p.isFakeBoldText = false; p.textSize = widthPx / 34f; p.color = Color.rgb(90, 90, 90)
            c.drawText("São Paulo, 5 de novembro de 2026.", m, h * 0.67f, p)
            c.drawText("Página ${page + 1}", widthPx - m - widthPx * 0.15f, h * 0.97f, p)
            return bmp
        }
    }
}
