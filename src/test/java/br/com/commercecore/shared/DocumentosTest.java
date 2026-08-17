package br.com.commercecore.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DocumentosTest {

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "16899535009"})
    void deveAceitarCpfValido(String cpf) {
        assertThat(Documentos.cpfValido(cpf)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"111.111.111-11", "12345678900", "123"})
    void deveRejeitarCpfInvalido(String cpf) {
        assertThat(Documentos.cpfValido(cpf)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.222.333/0001-81", "45723174000110"})
    void deveAceitarCnpjValido(String cnpj) {
        assertThat(Documentos.cnpjValido(cnpj)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.111.111/1111-11", "12345678000100", "123"})
    void deveRejeitarCnpjInvalido(String cnpj) {
        assertThat(Documentos.cnpjValido(cnpj)).isFalse();
    }
}
