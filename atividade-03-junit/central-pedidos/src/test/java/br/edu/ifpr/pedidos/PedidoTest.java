package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private ItemPedido itemAtivo(String sku, long precoCentavos, int quantidade, int estoque, int pesoGramas, boolean fragil) {
        return new ItemPedido(sku, precoCentavos, quantidade, estoque, pesoGramas, fragil);
    }

    @Test
    void deveCriarPedidoValidoComUmItem() {
        Pedido pedido = new Pedido(List.of(itemAtivo("SKU-1", 1_000, 1, 1, 100, false)), "PR", false, null);

        assertEquals("PR", pedido.uf());
    }

    @Test
    void deveLancarExcecaoQuandoListaDeItensForNula() {
        assertThrows(IllegalArgumentException.class, () -> new Pedido(null, "PR", false, null));
    }

    @Test
    void deveLancarExcecaoQuandoListaTiverMaisDeCemLinhas() {
        List<ItemPedido> itens = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            itens.add(itemAtivo("SKU-" + i, 100, 0, 0, 1, false));
        }

        assertThrows(IllegalArgumentException.class, () -> new Pedido(itens, "PR", false, null));
    }

    @Test
    void deveAceitarListaComExatamenteCemLinhas() {
        List<ItemPedido> itens = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            itens.add(itemAtivo("SKU-" + i, 100, 0, 0, 1, false));
        }

        Pedido pedido = new Pedido(itens, "PR", false, null);

        assertEquals(100, pedido.itens().size());
    }

    @Test
    void deveLancarNullPointerExceptionQuandoElementoDaListaForNulo() {
        List<ItemPedido> itens = Arrays.asList(itemAtivo("SKU-1", 100, 1, 1, 1, false), null);

        assertThrows(NullPointerException.class, () -> new Pedido(itens, "PR", false, null));
    }

    @Test
    void deveCopiarListaDefensivamente() {
        List<ItemPedido> itens = new ArrayList<>();
        itens.add(itemAtivo("SKU-1", 100, 1, 1, 1, false));

        Pedido pedido = new Pedido(itens, "PR", false, null);
        itens.add(itemAtivo("SKU-2", 100, 1, 1, 1, false));

        assertEquals(1, pedido.itens().size());
    }

    @Test
    void deveLancarExcecaoQuandoUfForNula() {
        assertThrows(IllegalArgumentException.class,
            () -> new Pedido(List.of(itemAtivo("SKU-1", 100, 1, 1, 1, false)), null, false, null));
    }

    @Test
    void deveLancarExcecaoQuandoUfTiverUmaLetra() {
        assertThrows(IllegalArgumentException.class,
            () -> new Pedido(List.of(itemAtivo("SKU-1", 100, 1, 1, 1, false)), "P", false, null));
    }

    @Test
    void deveLancarExcecaoQuandoUfForMinuscula() {
        assertThrows(IllegalArgumentException.class,
            () -> new Pedido(List.of(itemAtivo("SKU-1", 100, 1, 1, 1, false)), "pr", false, null));
    }

    @Test
    void deveAceitarUfDesconhecidaDeDuasLetrasMaiusculas() {
        Pedido pedido = new Pedido(List.of(itemAtivo("SKU-1", 100, 1, 1, 1, false)), "XX", false, null);

        assertEquals("XX", pedido.uf());
    }

    @Test
    void deveCalcularSubtotalZeroParaListaVazia() {
        Pedido pedido = new Pedido(List.of(), "PR", false, null);

        assertEquals(0L, pedido.subtotalCentavos());
    }

    @Test
    void deveCalcularSubtotalSomandoApenasItensAtivos() {
        Pedido pedido = new Pedido(List.of(
            itemAtivo("SKU-1", 1_000, 2, 5, 100, false),
            itemAtivo("SKU-2", 500, 0, 5, 100, false)
        ), "PR", false, null);

        assertEquals(2_000L, pedido.subtotalCentavos());
    }

    @Test
    void devePesoGramasZeroParaListaVazia() {
        Pedido pedido = new Pedido(List.of(), "PR", false, null);

        assertEquals(0, pedido.pesoGramas());
    }

    @Test
    void deveSomarPesoGramasDeTodosOsItens() {
        Pedido pedido = new Pedido(List.of(
            itemAtivo("SKU-1", 100, 2, 5, 300, false),
            itemAtivo("SKU-2", 100, 1, 5, 200, false)
        ), "PR", false, null);

        assertEquals(800, pedido.pesoGramas());
    }

    @Test
    void naoDeveTerFragilQuandoListaForVazia() {
        Pedido pedido = new Pedido(List.of(), "PR", false, null);

        assertFalse(pedido.temFragil());
    }

    @Test
    void deveTerFragilQuandoItemAtivoForFragil() {
        Pedido pedido = new Pedido(List.of(itemAtivo("SKU-1", 100, 1, 1, 100, true)), "PR", false, null);

        assertTrue(pedido.temFragil());
    }

    @Test
    void naoDeveTerFragilQuandoItemFragilForInativo() {
        Pedido pedido = new Pedido(List.of(itemAtivo("SKU-1", 100, 0, 1, 100, true)), "PR", false, null);

        assertFalse(pedido.temFragil());
    }

    @Test
    void deveTerEstoqueSuficienteQuandoListaForVazia() {
        Pedido pedido = new Pedido(List.of(), "PR", false, null);

        assertTrue(pedido.estoqueSuficiente());
    }

    @Test
    void deveTerEstoqueSuficienteQuandoTodosOsItensEstiveremDisponiveis() {
        Pedido pedido = new Pedido(List.of(
            itemAtivo("SKU-1", 100, 2, 5, 100, false),
            itemAtivo("SKU-2", 100, 3, 3, 100, false)
        ), "PR", false, null);

        assertTrue(pedido.estoqueSuficiente());
    }

    @Test
    void naoDeveTerEstoqueSuficienteQuandoAlgumItemFaltar() {
        Pedido pedido = new Pedido(List.of(
            itemAtivo("SKU-1", 100, 2, 5, 100, false),
            itemAtivo("SKU-2", 100, 4, 3, 100, false)
        ), "PR", false, null);

        assertFalse(pedido.estoqueSuficiente());
    }
}
