# Relatório — UI criativa do LetsSignMobile

Branch `ui-criativa`. Pipeline criativo do prospect-local (feito para landing HTML) adaptado para Jetpack Compose.

## 1. Situação de partida (main)

A `main` **não compilava**. `./gradlew assembleDebug` falhava em `:app:compileDebugKotlin` (log completo em `design/build-main.log`):

```
e: PositioningScreen.kt:19:43 Cannot access 'weight': it is internal in 'androidx.compose.foundation.layout'
e: SignerSelectStep.kt:11:43 Cannot access 'weight': it is internal ...
e: TitlePdfStep.kt:13:43 Cannot access 'weight': it is internal ...
e: PdfPageView.kt:31:26 Unresolved reference: BoxWithConstraints
e: SignDocumentScreen.kt:16:43 Cannot access 'weight': it is internal ...
e: SignDocumentScreen.kt:185:33 Unresolved reference: clickable
BUILD FAILED in 4m 43s
```

Correção mínima em commit separado (`build: corrige compilação da main`): remover os imports de `weight` (vem do escopo Row/Column), trocar o tipo do receiver para `BoxWithConstraintsScope` e importar `clickable`. Depois disso a main compila; os screenshots "antes" foram tirados desse estado, sem mexer no visual.

## 2. Sorteio

`python design/sorteio.py <prospect-local>/scripts/cardapio-design.py` (as listas são importadas do próprio script do prospect-local). Seed **608168**:

| item | sorteado |
|---|---|
| cor | **duotom**: duas cores fortes que se revezam como fundo, neutros quase ausentes |
| composição | **colunas de jornal**: seções em 2-3 colunas com fios finos, cabeçalho tipo manchete de capa |
| ornamento | **blocos com sombra dura deslocada e borda grossa** (neobrutalista contido) |
| display | **Instrument Serif** |
| corpo | **Source Sans 3** |

Nada trocado. Adaptação ao celular: colunas reais só onde cabem em 360dp (placar da lista, datas do acompanhamento, resumo da revisão); o "jornal" aparece no cabeçalho de capa de todas as telas e nas listas em formato de índice (01, 02, fio fino).

Duotom escolhido: **Caneta #1A2AB8** (azul de esferográfica) + **Marca-texto #F4E04D** (amarelo de grifar documento). Contraste 7,0:1 entre as duas; todos os pares de texto usados medem ≥ 5,2:1 (tabela em `design.json`).

## 3. Telas alteradas

Login · Admin (abas Documentos/Signatários) · Lista de documentos · Lista de signatários · Formulário de signatário · Acompanhamento do documento · Wizard 1 (título/PDF) · Wizard 2 (signatários + classificação) · Wizard 3 (posicionar campos) · Wizard 4 (revisão/envio) · Home do signatário (pendentes/assinados) · Assinar documento · Pad de assinatura · Tema, cores, fontes, ícone do launcher.

Não alterados: ViewModels, `data/`, `di/`, `session/`, `navigation/`, `util/`. A matemática de toque/arraste da tela de posicionamento foi copiada sem mudança (`git diff -w` não mostra linha alterada com `Ratio`, `detect*`, `addField`, `moveField` ou `toPx`). O PNG da assinatura sai igual ao de antes (mesma tinta, espessura e tamanho).

## 4. Peça autoral animada

- **Principal: a rubrica que se escreve** no login. Canvas + `PathMeasure.getSegment`; a ponta da caneta percorre o traço por 1,8 s e o sublinhado fecha no final, sobre a linha "× Assinatura do titular". Tocar reescreve.
- **Carimbo ASSINADO**: cai girado sobre o campo quando o signatário confirma o desenho.
- **Linha do tempo de eventos**: o trilho desce de marco em marco no acompanhamento.
- Animações desligadas no sistema (`ANIMATOR_DURATION_SCALE == 0`) → as três aparecem já no estado final. Verificado em teste (`anim/rubrica-sem-animacao-16ms.png`: rubrica completa no 1º quadro).

Quadros reais (relógio de teste controlado) em `design/screens/depois/anim/`.

## 5. Crítica (adaptação do gauntlet)

Screenshots reais: Robolectric (gráficos nativos) + Roborazzi, `./gradlew testDebugUnitTest -Pfase=<fase>`, com dados fictícios gravados no Room de verdade e uma página de contrato desenhada no lugar do `PdfRenderer` (que não existe no JVM). Tela padrão 360×640dp; também 411×891dp e telas altas para conteúdo abaixo da dobra. **19 testes, 0 falhas.**

### Rodada 1: `defeitos` (sobre `design/screens/rodada1/`)

1. **Login cortado em 360×640**: capa ocupava ~55% da altura e o botão Entrar ficava abaixo da dobra; a linha de edição com a data por extenso estourava.
2. **Rubrica não lia como assinatura**: parecia uma sequência de ondas iguais (cara de "squiggle" genérico).
3. **"Toque para assinar" truncado** no campo sobre o PDF (labelSmall em caixa-alta espaçada não cabia na largura do campo).
4. **Botão desabilitado ilegível**: "Concluir assinatura" desabilitado usava fundo amarelo-acinzentado com texto escuro de baixo contraste e parecia ativo.
5. **Régua de etapas do wizard truncada** ("2 · Signatár…").
6. **Segmentos de classificação apertados** com recuo de 60dp: "Testemunha" quase cortado.

Correções: capa do login reduzida (wordmark 60sp, rubrica 84dp, linha de edição curta "5 nov. 2026" com reticências); rubrica redesenhada com laço inicial, "S" alto e fecho deslocado; texto do campo em 2 linhas centralizado; desabilitado perde o preenchimento e vira só contorno em cor suave, com texto "Falta assinar 1 campo"; etapas renomeadas Arquivo/Quem/Onde/Envio; segmentos sem recuo e com fonte de corpo.

### Rodada 2: `defeitos` de novo (rodada2, não versionada; mesma bateria + quadros da animação)

1. **Nome do arquivo quebrava no meio da extensão** (".p / df").
2. **Wizard 2 e 4: dois botões empilhados + aviso comiam ~200dp** do rodapé em 640dp, sobrando pouco para a lista.
3. **Home do signatário**: número de ordem centralizado verticalmente com título de 2 linhas (desalinhado do título).
4. **Linha do tempo**: quadros capturados fora da área visível (problema do teste, não da tela).

Correções: quebra permitida só em `-`, `_` e `.` (espaço de largura zero); Voltar/Continuar lado a lado (`AcoesWizard`) nos 4 passos; alinhamento ao topo nas linhas; teste da linha do tempo em tela alta. Resultado final em `design/screens/depois/`.

### Rodada 3: `melhor`/`cego` (antes × depois)

`design/screens/comparativo-antes-depois.png`: em cima a main, embaixo a branch, nas mesmas 5 telas (login, documentos, acompanhamento, posicionamento, assinar). Veredito, olhando os PNGs:

- **Antes**: tem cara de template Material3 sem tema. Cartões lilás de canto arredondado, botão pílula rosa, Roboto, muito espaço vazio. A lista de documentos em 360dp é **quebrada de verdade**: o StatusChip ocupa uma coluna estreita e o texto "Falha no envio" fica em pé, uma letra por linha, esticando os cartões. Acompanhamento e posicionamento renderizam quase vazios na primeira dobra.
- **Depois**: identidade própria e reconhecível (duotom, capa de jornal, serifada, blocos com sombra dura). O status aparece por forma e por cor, os botões principais ficam sempre visíveis no rodapé e a página do PDF é destacada como folha.
- **Melhor: depois**, com folga. Teste cego (qual parece template de IA/padrão): o "antes" seria apontado. O "depois" não tem os vícios típicos (gradiente, glassmorphism, cartões iguais, pílulas, roxo M3).
- Limitação honesta: a crítica foi feita pelo próprio construtor olhando os PNGs, não por um crítico de contexto limpo, e não houve lineup com apps reais de referência como no gauntlet original.

## 6. Pendências e o que não foi verificado

- **Não rodei em aparelho nem emulador.** O APK compila, e as telas foram renderizadas pelo Robolectric com o Compose real. Mas gesto real de arrastar campo, seletor de PDF do sistema (SAF), `PdfRenderer` nativo e IME/teclado não foram exercitados em device.
- O "×" de remover **dentro** do campo sobre o PDF tem 24dp (o campo tem cerca de 36dp de altura). Para compensar, há uma lista de campos com botão de remover de 48dp logo abaixo da página.
- O modo escuro do sistema é ignorado de propósito (o duotom é a identidade). Se o professor exigir dark mode, é preciso criar uma variante.
- A data na capa usa `Date()` do aparelho e o locale pt-BR fixo.
- Os screenshots dependem da fonte `.ttf` empacotada. O Robolectric renderiza o texto com a engine nativa do host (Windows), então pode haver pequenas diferenças de antialiasing em relação ao aparelho.
