package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void deveCriarClienteComComprasAnterioresZero() {
        Cliente cliente = new Cliente(false, false, 0);

        assertEquals(0, cliente.comprasAnteriores());
    }

    @Test
    void deveCriarClienteComComprasAnterioresPositivo() {
        Cliente cliente = new Cliente(true, false, 5);

        assertAll(
            () -> assertTrue(cliente.vip()),
            () -> assertFalse(cliente.bloqueado()),
            () -> assertEquals(5, cliente.comprasAnteriores())
        );
    }

    @Test
    void deveLancarExcecaoQuandoComprasAnterioresForNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente(false, false, -1));
    }
}
