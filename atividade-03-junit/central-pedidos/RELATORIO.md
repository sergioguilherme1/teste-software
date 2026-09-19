# Relatório do grupo

Integrantes: Sérgio

## Grafo de relacionamento entre classes

```mermaid
classDiagram
    class PedidoService {
        -PoliticaDesconto descontos
        -CalculadoraFrete fretes
        -AnaliseRisco risco
        -PagamentoService pagamentos
        +fechar(Pedido, Cliente) ResultadoPedido
    }
    class PoliticaDesconto {
        +calcular(Cliente, long, String) long
    }
    class CalculadoraFrete {
        +calcular(Pedido, Cliente, long) long
    }
    class AnaliseRisco {
        +avaliar(Cliente, long, boolean) String
    }
    class PagamentoService {
        -ProcessadorPagamento processador
        +pagar(long, int) boolean
    }
    class ProcessadorPagamento {
        <<interface>>
        +autorizar(long) boolean
    }
    class Cliente
    class ItemPedido
    class Pedido
    class ResultadoPedido

    PedidoService --> PoliticaDesconto
    PedidoService --> CalculadoraFrete
    PedidoService --> AnaliseRisco
    PedidoService --> PagamentoService
    PagamentoService --> ProcessadorPagamento
    PedidoService ..> ResultadoPedido : cria
    Pedido "1" --> "*" ItemPedido : itens
    PedidoService ..> Pedido
    PedidoService ..> Cliente
```

## Grafo de chamadas de `fechar`

```mermaid
flowchart TD
    A[PedidoService.fechar] --> B[cliente.bloqueado?]
    B -- sim --> Z1[return BLOQUEADO]
    B -- não --> C[Pedido.subtotalCentavos]
    C --> D[Pedido.estoqueSuficiente?]
    D -- não --> Z2[return SEM_ESTOQUE]
    D -- sim --> E[PoliticaDesconto.calcular]
    E --> F[CalculadoraFrete.calcular]
    F --> G[Pedido.temFragil / pesoGramas]
    F --> H[AnaliseRisco.avaliar]
    H -- não aprovado --> Z3[return REVISAO/RECUSADO]
    H -- aprovado --> I[PagamentoService.pagar]
    I --> J[ProcessadorPagamento.autorizar]
    I --> Z4[return PAGO/PAGAMENTO_RECUSADO]
```

Curto-circuito: nas condições com `&&`/`\|\|` (ex.: `AnaliseRisco`, `CalculadoraFrete`, `PoliticaDesconto`) cada operando foi modelado como um **nó de decisão separado**, pois o segundo operando só é avaliado se o primeiro não decidir o resultado — isso afeta diretamente a contagem de nós/arestas do CFG.

Exceções (`IllegalArgumentException`, `NullPointerException`, cupom desconhecido propagado) foram tratadas como **nós terminais que convergem para um nó de saída único** (`End`), permitindo aplicar `V(G) = E − N + 2` num grafo com entrada e saída unificadas.

---

## Grafos e complexidade

| Método | Nós (N) | Arestas (E) | V(G) = E−N+2 | V(G) = decisões+1 | Caminhos independentes propostos | Restrições de viabilidade |
| --- | --- | --- | --- | --- | --- | --- |
| `PoliticaDesconto.calcular` | 24 | 34 | 12 | 12 | 12 | Nenhuma restrição — todos os caminhos são alcançáveis via combinação de `vip`, `subtotal`, `comprasAnteriores` e `cupom`. |
| `CalculadoraFrete.calcular` | 21 | 29 | 10 | 10 | 10 | Nenhuma restrição — laço de peso exige valores de `pesoGramas` específicos (0, 1 e 3 iterações testados). |
| `AnaliseRisco.avaliar` | 14 | 20 | 8 | 8 | 8 | Todas as 8 combinações são alcançáveis **na unidade**. Na colaboração via `PedidoService.fechar`, o ramo `RECUSADO` (cliente bloqueado) é **inviável**, pois `fechar` já intercepta clientes bloqueados antes de chamar `avaliar` (ver análise crítica). |
| `PagamentoService.pagar` | 14 | 18 | 6 | 6 | 6 | Nenhuma restrição — o `do/while` permite 1 a 3 tentativas por contrato. |
| `PedidoService.fechar` | 23 | 30 | 9 | 9 | 9 | O caminho de exceção do cupom desconhecido (nó 14→15) depende de `PoliticaDesconto` propagar `IllegalArgumentException`; nenhuma restrição de inviabilidade. |

### CFG — `PoliticaDesconto.calcular` (V(G) = 12)

```mermaid
flowchart TD
    n1[Início] --> n2{subtotal < 0?}
    n2 -- sim --> n3[throw SubtotalNegativo]
    n2 -- não --> n4{cliente.vip?}
    n4 -- sim --> n5[desconto = 10%]
    n4 -- não --> n6{subtotal >= 50000?}
    n6 -- sim --> n7[desconto = 5%]
    n6 -- não --> n8[desconto = 0]
    n5 --> n9{cupom == null?}
    n7 --> n9
    n8 --> n9
    n9 -- sim --> n10[return desconto]
    n9 -- não --> n11{cupom.isBlank?}
    n11 -- sim --> n12[return desconto]
    n11 -- não --> n13{switch cupom}
    n13 -- BEMVINDO --> n15{comprasAnteriores==0?}
    n13 -- EXTRA10 --> n18{subtotal>=20000?}
    n13 -- default --> n14[throw CupomDesconhecido]
    n15 -- sim --> n16{subtotal>=10000?}
    n15 -- não --> n20[calcula teto]
    n16 -- sim --> n17[desconto += 2000]
    n16 -- não --> n20
    n17 --> n20
    n18 -- sim --> n19[desconto += 10%]
    n18 -- não --> n20
    n19 --> n20
    n20 --> n21{desconto > teto?}
    n21 -- sim --> n22[return teto]
    n21 -- não --> n23[return desconto]
    n3 --> n24[Fim]
    n10 --> n24
    n12 --> n24
    n14 --> n24
    n22 --> n24
    n23 --> n24
```

**Base de caminhos independentes → testes em `PoliticaDescontoTest`:**

| # | Caminho (resumo) | Teste JUnit |
| --- | --- | --- |
| 1 | subtotal < 0 → exceção | `deveLancarExcecaoQuandoSubtotalForNegativo` |
| 2 | vip, cupom nulo | `deveAplicarDescontoVipDeDezPorCento` |
| 3 | comum, subtotal ≥ 50000, cupom nulo | `deveAplicarDescontoDeCincoPorCentoParaComumComSubtotalAcimaDoLimite` |
| 4 | comum, subtotal < 50000, cupom nulo | `deveNaoAplicarDescontoParaComumComSubtotalAbaixoDoLimite` |
| 5 | cupom em branco | `deveIgnorarCupomEmBranco` |
| 6 | BEMVINDO elegível | `deveAplicarCupomBemVindoQuandoElegivel` |
| 7 | BEMVINDO ignorado (já comprou antes) | `deveIgnorarCupomBemVindoQuandoClienteJaComprouAntes` |
| 8 | BEMVINDO ignorado (subtotal < 10000) | `deveIgnorarCupomBemVindoQuandoSubtotalForMenorQueLimite` |
| 9 | EXTRA10 elegível | `deveAplicarCupomExtra10QuandoElegivel` |
| 10 | EXTRA10 ignorado | `deveIgnorarCupomExtra10QuandoSubtotalForMenorQueLimite` |
| 11 | cupom desconhecido → exceção | `deveLancarExcecaoParaCupomDesconhecido` |
| 12 | desconto combinado > teto → capado | `deveLimitarDescontoAoTetoDeVintePorcento` |

### CFG — `CalculadoraFrete.calcular` (V(G) = 10)

```mermaid
flowchart TD
    a1[Início] --> a2{liquido < 0?}
    a2 -- sim --> a3[throw ValorNegativo]
    a2 -- não --> a4{switch uf}
    a4 -- PR --> a5[frete=1200]
    a4 -- SP/RJ --> a6[frete=2000]
    a4 -- default --> a7[frete=3000]
    a5 --> a8[excedente = peso-2000]
    a6 --> a8
    a7 --> a8
    a8 --> a9{excedente > 0?}
    a9 -- sim --> a10[frete+=300; excedente-=1000]
    a10 --> a9
    a9 -- não --> a11{liquido>=30000?}
    a11 -- sim --> a12{!expresso?}
    a11 -- não --> a14{vip?}
    a12 -- sim --> a13[frete=0]
    a12 -- não --> a14
    a13 --> a14
    a14 -- sim --> a15[frete/=2]
    a14 -- não --> a16{expresso?}
    a15 --> a16
    a16 -- sim --> a17[frete+=1500]
    a16 -- não --> a18{temFragil?}
    a17 --> a18
    a18 -- sim --> a19[frete+=500]
    a18 -- não --> a20[return frete]
    a19 --> a20
    a3 --> a21[Fim]
    a20 --> a21
```

**Base de caminhos → testes em `CalculadoraFreteTest`:** liquido negativo (`deveLancarExcecaoQuandoLiquidoForNegativo`), UF PR/SP-RJ/default (`deveCobrarFreteBaseParaParana`, `deveCobrarFreteBaseParaSaoPauloERio`, `deveCobrarFreteBaseParaOutrasUfs`), 1 e 3 iterações do laço de peso (`deveAdicionarTaxaUnicaParaPesoExcedenteComFracao`, `deveAdicionarTaxaParaVariasIteracoesDePesoExcedente`), zerar frete (`deveZerarFreteQuandoLiquidoAtingirLimiteENaoForExpresso`), meia tarifa VIP (`deveAplicarMeiaTarifaParaClienteVip`), taxa expressa (`deveAdicionarTaxaExpressa`), taxa frágil (`deveAdicionarTaxaParaItemFragil`) e não zerar quando expresso (`naoDeveZerarFreteQuandoForExpressoMesmoComLiquidoAlto`).

### CFG — `AnaliseRisco.avaliar` (V(G) = 8)

```mermaid
flowchart TD
    r1[Início] --> r2{total < 0?}
    r2 -- sim --> r3[throw TotalNegativo]
    r2 -- não --> r4{bloqueado?}
    r4 -- sim --> r5[return RECUSADO]
    r4 -- não --> r6{comprasAnteriores==0?}
    r6 -- sim --> r7{total>100000?}
    r6 -- não --> r10{total>500000?}
    r7 -- sim --> r9[return REVISAO]
    r7 -- não --> r8{expresso?}
    r8 -- sim --> r9
    r8 -- não --> r13[return APROVADO]
    r10 -- sim --> r11{!vip?}
    r10 -- não --> r13
    r11 -- sim --> r12[return REVISAO]
    r11 -- não --> r13
    r3 --> r14[Fim]
    r5 --> r14
    r9 --> r14
    r12 --> r14
    r13 --> r14
```

**Base de caminhos → testes em `AnaliseRiscoTest`:** total negativo, bloqueado, novo cliente com total alto, novo cliente expresso, novo cliente aprovado, cliente antigo não-vip com total alto, cliente antigo vip com total alto, cliente antigo aprovado — os 8 testes já escritos cobrem 1:1 os 8 caminhos.

### CFG — `PagamentoService.pagar` (V(G) = 6)

```mermaid
flowchart TD
    p1[Início] --> p2{total<=0?}
    p2 -- sim --> p3[throw TotalInvalido]
    p2 -- não --> p4{maxTentativas<1?}
    p4 -- sim --> p6[throw TentativasInvalidas]
    p4 -- não --> p5{maxTentativas>3?}
    p5 -- sim --> p6
    p5 -- não --> p7[tentativa=0]
    p7 --> p8[tentativa++]
    p8 --> p9{autorizar lança IllegalStateException?}
    p9 -- não --> p10[return autorizar]
    p9 -- sim --> p11[catch: continua]
    p11 --> p12{tentativa < maxTentativas?}
    p12 -- sim --> p8
    p12 -- não --> p13[return false]
    p3 --> p14[Fim]
    p6 --> p14
    p10 --> p14
    p13 --> p14
```

**Base de caminhos → testes em `PagamentoServiceTest`:** total inválido, tentativas < 1, tentativas > 3, aprovação na 1ª tentativa (sem repetir), recusa definitiva sem repetir, sucesso após indisponibilidade temporária, esgotar tentativas com indisponibilidade — mais os testes extra de limite mínimo/máximo de tentativas e propagação de exceção não relacionada.

### CFG — `PedidoService.fechar` (V(G) = 9)

```mermaid
flowchart TD
    f1[Início] --> f2{pedido==null?}
    f2 -- sim --> f3[throw NPE]
    f2 -- não --> f4{cliente==null?}
    f4 -- sim --> f5[throw NPE]
    f4 -- não --> f6{bloqueado?}
    f6 -- sim --> f7[return BLOQUEADO]
    f6 -- não --> f8[subtotal]
    f8 --> f9{subtotal==0?}
    f9 -- sim --> f10[throw SemItens]
    f9 -- não --> f11{!estoqueSuficiente?}
    f11 -- sim --> f12[return SEM_ESTOQUE]
    f11 -- não --> f13[desconto = PoliticaDesconto.calcular]
    f13 --> f14{cupom desconhecido lançado?}
    f14 -- sim --> f15[propaga exceção]
    f14 -- não --> f16[frete, total, analise]
    f16 --> f17{analise != APROVADO?}
    f17 -- sim --> f18[return analise]
    f17 -- não --> f19{pagamentos.pagar?}
    f19 -- sim --> f20[status=PAGO]
    f19 -- não --> f21[status=PAGAMENTO_RECUSADO]
    f20 --> f22[return status]
    f21 --> f22
    f3 --> f23[Fim]
    f5 --> f23
    f7 --> f23
    f10 --> f23
    f12 --> f23
    f15 --> f23
    f18 --> f23
    f22 --> f23
```

**Base de caminhos → testes em `PedidoServiceTest`:** pedido nulo, cliente nulo, bloqueado sem cobrança, subtotal zero, sem estoque (com cupom inválido ignorado), cupom desconhecido propagado, revisão sem cobrança, pago, pagamento recusado.

---

## Matriz de testes

| ID / método JUnit | Unidade | Entrada e estado do stub | Resultado esperado | Caminho / aresta | Critério atendido |
| --- | --- | --- | --- | --- | --- |
| `PoliticaDescontoTest.deveLimitarDescontoAoTetoDeVintePorcento` | `PoliticaDesconto` | vip, comprasAnteriores=0, subtotal=10000, cupom=BEMVINDO | 2000 (capado) | n21 (desconto>teto) = verdadeiro | Valor-limite (teto de 20%) |
| `CalculadoraFreteTest.deveAdicionarTaxaParaVariasIteracoesDePesoExcedente` | `CalculadoraFrete` | peso=4500g | 2100 (3 iterações) | a9→a10 (3x) | Laço com várias iterações |
| `CalculadoraFreteTest.naoDeveZerarFreteQuandoForExpressoMesmoComLiquidoAlto` | `CalculadoraFrete` | liquido=30000, expresso=true | 3200 (não zera) | a11(sim)→a12(não, pois expresso) | Curto-circuito `&&` — 2º operando decide |
| `AnaliseRiscoTest.deveColocarEmRevisaoNovoClienteComTotalBaixoMasExpresso` | `AnaliseRisco` | comprasAnteriores=0, total=1000, expresso=true | REVISAO | r7(não)→r8(sim) | Curto-circuito `\|\|` — 2º operando decide |
| `PagamentoServiceTest.deveRepetirAteSucessoQuandoIndisponibilidadeTemporaria` | `PagamentoService` | stub lança `IllegalStateException` na 1ª chamada, aprova na 2ª | `true`, 2 chamadas | p9(sim)→p11→p12(sim)→p8→p9(não)→p10 | Exceção controlada (não contada como branch pelo JaCoCo) |
| `PedidoServiceTest.deveRetornarSemEstoqueAntesDeValidarCupomDesconhecido` | `PedidoService` (colaboração) | estoque insuficiente + cupom inválido | `SEM_ESTOQUE`, sem exceção | f11(sim)→f12 | Ordem do contrato (retorno antecipado antes de avaliar cupom) |
| `PedidoServiceTest.devePropagarExcecaoDeCupomDesconhecidoQuandoEstoqueForSuficiente` | `PedidoService` (colaboração) | estoque ok + cupom inválido | `IllegalArgumentException` propagada | f14(sim)→f15 | Propagação de exceção entre camadas |

*(Tabela ilustrativa — os 90 testes cobrem coletivamente todos os caminhos listados nas seções de CFG acima.)*

---

## Evolução da cobertura

| Etapa | Testes executados | Linhas | Branches | Métodos | Classes | Lacunas e justificativas |
| --- | --- | --- | --- | --- | --- | --- |
| Inicial | 1 (exemplo fornecido) | ~5% | ~5% | ~11% | 9/9 (import) | Só um caminho de `PedidoService.fechar` exercitado; demais classes sem nenhum teste. |
| Fase 1 — entidades | 41 | — | — | — | — | `Cliente`, `ItemPedido`, `Pedido` cobrindo validações de construtor e métodos auxiliares. |
| Fase 2 — regras de negócio | 72 | — | — | — | — | `PoliticaDesconto`, `CalculadoraFrete`, `AnaliseRisco` → 100% linhas/branches nas 3 classes. |
| Fase 3 — pagamento e orquestração | **90** | **100%** | **100%** | **100%** | **9/9 (100%)** | `PagamentoService` e `PedidoService` completados; nenhuma lacuna de linha/branch alcançável restante. |

---

## Análise crítica

- **Quais combinações faltavam mesmo com os ramos cobertos?** O JaCoCo reporta 100% de branches, mas isso não comprova que toda **combinação multi-decisão** foi exercitada — por exemplo, em `CalculadoraFrete`, as flags `vip`, `expresso` e `temFragil` são decisões independentes (3 flags = 8 combinações), e testamos apenas combinações isoladas (uma flag ativa por vez), não todas as 8 combinações simultâneas.

- **Quais condições não foram avaliadas devido ao curto-circuito?** Em `AnaliseRisco.avaliar`, na expressão `total > 100_000 || expresso`, quando `total > 100_000` é verdadeiro, o operando `expresso` **nunca é avaliado** (o teste `deveColocarEmRevisaoNovoClienteComTotalAlto` usa `expresso=false` sem necessidade). Da mesma forma, em `CalculadoraFrete`, quando `liquido>=30000` é falso, `!pedido.expresso()` nunca é avaliado.

- **Quais caminhos são inviáveis no serviço, mas viáveis na unidade?** O ramo `RECUSADO` de `AnaliseRisco.avaliar` (quando `cliente.bloqueado()` é verdadeiro) é **viável isoladamente** (testado em `AnaliseRiscoTest.deveRecusarClienteBloqueadoIndependenteDoTotal`), mas **inviável via `PedidoService.fechar`**, porque `fechar` já retorna `"BLOQUEADO"` antes de chamar `risco.avaliar(...)` para qualquer cliente bloqueado — ou seja, `risco.avaliar` nunca recebe um `cliente.bloqueado()==true` quando chamado a partir do serviço.

- **Como foram testadas exceções e quantidades de iterações?** Exceções de validação (`IllegalArgumentException`, `NullPointerException`) foram testadas com `assertThrows` em cada classe. A exceção controlada de `PagamentoService` (`IllegalStateException`) foi testada com um stub baseado em `AtomicInteger` que conta chamadas e alterna entre lançar a exceção e retornar um resultado, cobrindo 0, 1 e 2 repetições. O laço de peso de `CalculadoraFrete` foi testado com 0 iterações (peso ≤ 2kg), 1 iteração com fração (peso 2,5kg) e 3 iterações (peso 4,5kg).

- **Qual alteração proposital foi detectada por qual teste? A alteração foi desfeita?** Alterei temporariamente o desconto VIP de `PoliticaDesconto.calcular` de 10% para 11% (`subtotal * 11 / 100`). O teste `PoliticaDescontoTest.deveAplicarDescontoVipDeDezPorCento` falhou imediatamente (`expected: <10000> but was: <11000>`), confirmando que a suíte detecta regressões nessa regra. A alteração foi **desfeita** e a suíte voltou a passar (90/90) com 100% de cobertura.
