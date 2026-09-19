package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticipacaoTest {

    @Test
    void deveSomarTresPontosQuandoEntregouAtividadeEParticipouDaAula() {
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(true, true);

        assertEquals(3, resultado);
    }

    @Test
    void deveSomarDoisPontosQuandoApenasEntregouAtividade() {
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(true, false);

        assertEquals(2, resultado);
    }

    @Test
    void deveSomarUmPontoQuandoApenasParticipouDaAula() {
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(false, true);

        assertEquals(1, resultado);
    }

    @Test
    void deveSomarZeroPontosQuandoNaoEntregouNemParticipou() {
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(false, false);

        assertEquals(0, resultado);
    }
}
