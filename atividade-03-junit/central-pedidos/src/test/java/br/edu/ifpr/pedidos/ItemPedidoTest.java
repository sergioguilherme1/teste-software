package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    @Test
    void deveCriarItemValidoComDadosNoLimiteMinimo() {
        ItemPedido item = new ItemPedido("SKU-1", 1, 0, 0, 1, false);

        assertAll(
            () -> assertEquals("SKU-1", item.sku()),
            () -> assertEquals(1, item.precoCentavos()),
            () -> assertEquals(0, item.quantidade()),
            () -> assertEquals(0, item.estoque()),
            () -> assertEquals(1, item.pesoGramas())
        );
    }

    @Test
    void deveCriarItemValidoComDadosNoLimiteMaximo() {
        ItemPedido item = new ItemPedido("SKU-2", 1_000_000, 100, 100, 100_000, true);

        assertAll(
            () -> assertEquals(1_000_000L, item.precoCentavos()),
            () -> assertEquals(100, item.quantidade()),
            () -> assertEquals(100_000, item.pesoGramas())
        );
    }

    @Test
    void deveLancarExcecaoQuandoSkuForNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido(null, 100, 1, 1, 100, false));
    }

    @Test
    void deveLancarExcecaoQuandoSkuForVazioOuEmBranco() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("   ", 100, 1, 1, 100, false));
    }

    @Test
    void deveLancarExcecaoQuandoPrecoForZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 0, 1, 1, 100, false));
    }

    @Test
    void deveLancarExcecaoQuandoPrecoForNegativo() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", -10, 1, 1, 100, false));
    }

    @Test
    void deveLancarExcecaoQuandoPrecoAcimaDoLimite() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000_001, 1, 1, 100, false));
    }

    @Test
    void deveLancarExcecaoQuandoQuantidadeForNegativa() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 100, -1, 1, 100, false));
    }

    @Test
    void deveLancarExcecaoQuandoQuantidadeAcimaDoLimite() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 100, 101, 200, 100, false));
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueForNegativo() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 100, 1, -1, 100, false));
    }

    @Test
    void deveLancarExcecaoQuandoPesoForZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 100, 1, 1, 0, false));
    }

    @Test
    void deveLancarExcecaoQuandoPesoAcimaDoLimite() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 100, 1, 1, 100_001, false));
    }

    @Test
    void deveCalcularTotalCentavosMultiplicandoPrecoPelaQuantidade() {
        ItemPedido item = new ItemPedido("SKU-1", 1_500, 3, 10, 100, false);

        assertEquals(4_500L, item.totalCentavos());
    }

    @Test
    void deveCalcularTotalCentavosComQuantidadeZero() {
        ItemPedido item = new ItemPedido("SKU-1", 1_500, 0, 10, 100, false);

        assertEquals(0L, item.totalCentavos());
    }

    @Test
    void deveEstarDisponivelQuandoQuantidadeForMenorQueEstoque() {
        ItemPedido item = new ItemPedido("SKU-1", 100, 2, 5, 100, false);

        assertTrue(item.disponivel());
    }

    @Test
    void deveEstarDisponivelQuandoQuantidadeForIgualAoEstoque() {
        ItemPedido item = new ItemPedido("SKU-1", 100, 5, 5, 100, false);

        assertTrue(item.disponivel());
    }

    @Test
    void naoDeveEstarDisponivelQuandoQuantidadeForMaiorQueEstoque() {
        ItemPedido item = new ItemPedido("SKU-1", 100, 6, 5, 100, false);

        assertFalse(item.disponivel());
    }
}
