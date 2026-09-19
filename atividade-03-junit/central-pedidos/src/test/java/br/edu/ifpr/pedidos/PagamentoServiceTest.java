package br.edu.ifpr.pedidos;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PagamentoServiceTest {

    @Test
    void deveLancarExcecaoQuandoTotalForZeroOuNegativo() {
        PagamentoService service = new PagamentoService(total -> true);

        assertThrows(IllegalArgumentException.class, () -> service.pagar(0, 3));
        assertThrows(IllegalArgumentException.class, () -> service.pagar(-10, 3));
    }

    @Test
    void deveLancarExcecaoQuandoMaxTentativasForMenorQueUm() {
        PagamentoService service = new PagamentoService(total -> true);

        assertThrows(IllegalArgumentException.class, () -> service.pagar(1_000, 0));
    }

    @Test
    void deveLancarExcecaoQuandoMaxTentativasForMaiorQueTres() {
        PagamentoService service = new PagamentoService(total -> true);

        assertThrows(IllegalArgumentException.class, () -> service.pagar(1_000, 4));
    }

    @Test
    void deveAprovarNaPrimeiraTentativaSemRepetir() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.incrementAndGet();
            return true;
        });

        assertTrue(service.pagar(1_000, 3));
        assertEquals(1, chamadas.get());
    }

    @Test
    void deveRecusarDefinitivamenteSemRepetirQuandoProcessadorRetornaFalse() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.incrementAndGet();
            return false;
        });

        assertFalse(service.pagar(1_000, 3));
        assertEquals(1, chamadas.get());
    }

    @Test
    void deveRepetirAteSucessoQuandoIndisponibilidadeTemporaria() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            if (chamadas.incrementAndGet() == 1) {
                throw new IllegalStateException("indisponível");
            }
            return true;
        });

        assertTrue(service.pagar(1_000, 3));
        assertEquals(2, chamadas.get());
    }

    @Test
    void deveRetornarFalseQuandoEsgotarTentativasComIndisponibilidade() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.incrementAndGet();
            throw new IllegalStateException("indisponível");
        });

        assertFalse(service.pagar(1_000, 2));
        assertEquals(2, chamadas.get());
    }

    @Test
    void deveAceitarLimiteMinimoDeUmaTentativa() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.incrementAndGet();
            throw new IllegalStateException("indisponível");
        });

        assertFalse(service.pagar(1_000, 1));
        assertEquals(1, chamadas.get());
    }

    @Test
    void deveAceitarLimiteMaximoDeTresTentativas() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            if (chamadas.incrementAndGet() < 3) {
                throw new IllegalStateException("indisponível");
            }
            return true;
        });

        assertTrue(service.pagar(1_000, 3));
        assertEquals(3, chamadas.get());
    }

    @Test
    void devePropagarExcecoesQueNaoSaoIndisponibilidadeTemporaria() {
        AtomicInteger chamadas = new AtomicInteger();
        PagamentoService service = new PagamentoService(total -> {
            chamadas.incrementAndGet();
            throw new RuntimeException("erro inesperado");
        });

        assertThrows(RuntimeException.class, () -> service.pagar(1_000, 3));
        assertEquals(1, chamadas.get());
    }
}
