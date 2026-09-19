package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnaliseRiscoTest {

    private final AnaliseRisco risco = new AnaliseRisco();

    @Test
    void deveLancarExcecaoQuandoTotalForNegativo() {
        Cliente cliente = new Cliente(false, false, 1);

        assertThrows(IllegalArgumentException.class, () -> risco.avaliar(cliente, -1, false));
    }

    @Test
    void deveRecusarClienteBloqueadoIndependenteDoTotal() {
        Cliente cliente = new Cliente(false, true, 1);

        assertEquals("RECUSADO", risco.avaliar(cliente, 100, false));
    }

    @Test
    void deveColocarEmRevisaoNovoClienteComTotalAlto() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals("REVISAO", risco.avaliar(cliente, 100_001, false));
    }

    @Test
    void deveColocarEmRevisaoNovoClienteComTotalBaixoMasExpresso() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals("REVISAO", risco.avaliar(cliente, 1_000, true));
    }

    @Test
    void deveAprovarNovoClienteComTotalBaixoENaoExpresso() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals("APROVADO", risco.avaliar(cliente, 1_000, false));
    }

    @Test
    void deveColocarEmRevisaoClienteAntigoNaoVipComTotalAlto() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals("REVISAO", risco.avaliar(cliente, 500_001, false));
    }

    @Test
    void deveAprovarClienteAntigoVipComTotalAlto() {
        Cliente cliente = new Cliente(true, false, 1);

        assertEquals("APROVADO", risco.avaliar(cliente, 500_001, false));
    }

    @Test
    void deveAprovarClienteAntigoComTotalBaixo() {
        Cliente cliente = new Cliente(false, false, 1);

        assertEquals("APROVADO", risco.avaliar(cliente, 1_000, false));
    }
}
