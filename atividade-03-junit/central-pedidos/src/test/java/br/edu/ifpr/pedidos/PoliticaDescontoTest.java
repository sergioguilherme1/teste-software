package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoliticaDescontoTest {

    private final PoliticaDesconto politica = new PoliticaDesconto();

    @Test
    void deveLancarExcecaoQuandoSubtotalForNegativo() {
        Cliente cliente = new Cliente(false, false, 1);

        assertThrows(IllegalArgumentException.class, () -> politica.calcular(cliente, -1, null));
    }

    @Test
    void deveAplicarDescontoVipDeDezPorCento() {
        Cliente cliente = new Cliente(true, false, 1);

        assertEquals(10_000L, politica.calcular(cliente, 100_000, null));
    }

    @Test
    void deveAplicarDescontoDeCincoPorCentoParaComumComSubtotalAcimaDoLimite() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(2_500L, politica.calcular(cliente, 50_000, null));
    }

    @Test
    void deveNaoAplicarDescontoParaComumComSubtotalAbaixoDoLimite() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(0L, politica.calcular(cliente, 49_999, null));
    }

    @Test
    void deveIgnorarCupomEmBranco() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(0L, politica.calcular(cliente, 10_000, "   "));
    }

    @Test
    void deveAplicarCupomBemVindoQuandoElegivel() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals(2_000L, politica.calcular(cliente, 10_000, "bemvindo"));
    }

    @Test
    void deveIgnorarCupomBemVindoQuandoClienteJaComprouAntes() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(0L, politica.calcular(cliente, 10_000, "BEMVINDO"));
    }

    @Test
    void deveIgnorarCupomBemVindoQuandoSubtotalForMenorQueLimite() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals(0L, politica.calcular(cliente, 9_999, "BEMVINDO"));
    }

    @Test
    void deveAplicarCupomExtra10QuandoElegivel() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(2_000L, politica.calcular(cliente, 20_000, "EXTRA10"));
    }

    @Test
    void deveIgnorarCupomExtra10QuandoSubtotalForMenorQueLimite() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(0L, politica.calcular(cliente, 19_999, "EXTRA10"));
    }

    @Test
    void deveLancarExcecaoParaCupomDesconhecido() {
        Cliente cliente = new Cliente(false, false, 1);

        assertThrows(IllegalArgumentException.class, () -> politica.calcular(cliente, 10_000, "NAOEXISTE"));
    }

    @Test
    void deveLimitarDescontoAoTetoDeVintePorcento() {
        Cliente cliente = new Cliente(true, false, 0);

        assertEquals(2_000L, politica.calcular(cliente, 10_000, "BEMVINDO"));
    }
}
