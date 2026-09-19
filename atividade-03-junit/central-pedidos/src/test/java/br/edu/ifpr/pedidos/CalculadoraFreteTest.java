package br.edu.ifpr.pedidos;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraFreteTest {

    private final CalculadoraFrete calculadora = new CalculadoraFrete();

    private Pedido pedido(String uf, boolean expresso, int pesoGramas, boolean fragil) {
        ItemPedido item = new ItemPedido("SKU-1", 100, 1, 1, pesoGramas, fragil);
        return new Pedido(List.of(item), uf, expresso, null);
    }

    @Test
    void deveLancarExcecaoQuandoLiquidoForNegativo() {
        Pedido pedido = pedido("PR", false, 1_000, false);
        Cliente cliente = new Cliente(false, false, 1);

        assertThrows(IllegalArgumentException.class, () -> calculadora.calcular(pedido, cliente, -1));
    }

    @Test
    void deveCobrarFreteBaseParaParana() {
        Pedido pedido = pedido("PR", false, 1_000, false);
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(1_200L, calculadora.calcular(pedido, cliente, 5_000));
    }

    @Test
    void deveCobrarFreteBaseParaSaoPauloERio() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(2_000L, calculadora.calcular(pedido("SP", false, 1_000, false), cliente, 5_000));
        assertEquals(2_000L, calculadora.calcular(pedido("RJ", false, 1_000, false), cliente, 5_000));
    }

    @Test
    void deveCobrarFreteBaseParaOutrasUfs() {
        Pedido pedido = pedido("BA", false, 1_000, false);
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(3_000L, calculadora.calcular(pedido, cliente, 5_000));
    }

    @Test
    void deveAdicionarTaxaUnicaParaPesoExcedenteComFracao() {
        Pedido pedido = pedido("PR", false, 2_500, false);
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(1_500L, calculadora.calcular(pedido, cliente, 5_000));
    }

    @Test
    void deveAdicionarTaxaParaVariasIteracoesDePesoExcedente() {
        Pedido pedido = pedido("PR", false, 4_500, false);
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(2_100L, calculadora.calcular(pedido, cliente, 5_000));
    }

    @Test
    void deveZerarFreteQuandoLiquidoAtingirLimiteENaoForExpresso() {
        Pedido pedido = pedido("PR", false, 1_000, true);
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(500L, calculadora.calcular(pedido, cliente, 30_000));
    }

    @Test
    void deveAplicarMeiaTarifaParaClienteVip() {
        Pedido pedido = pedido("PR", false, 1_000, false);
        Cliente cliente = new Cliente(true, false, 1);

        assertEquals(600L, calculadora.calcular(pedido, cliente, 5_000));
    }

    @Test
    void deveAdicionarTaxaExpressa() {
        Pedido pedido = pedido("PR", true, 1_000, false);
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(2_700L, calculadora.calcular(pedido, cliente, 5_000));
    }

    @Test
    void deveAdicionarTaxaParaItemFragil() {
        Pedido pedido = pedido("PR", false, 1_000, true);
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(1_700L, calculadora.calcular(pedido, cliente, 5_000));
    }

    @Test
    void naoDeveZerarFreteQuandoForExpressoMesmoComLiquidoAlto() {
        Pedido pedido = pedido("PR", true, 1_000, true);
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals(3_200L, calculadora.calcular(pedido, cliente, 30_000));
    }
}
