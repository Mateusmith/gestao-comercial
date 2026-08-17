package br.com.commercecore;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.DirtiesContext;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DatabaseInvariantIntegrationTest {

    private final JdbcClient banco;

    @Autowired
    DatabaseInvariantIntegrationTest(JdbcClient banco) {
        this.banco = banco;
    }

    @Test
    void auditoriaDeveSerImutavelNoProprioBanco() {
        UUID empresaId = UUID.randomUUID();
        UUID auditoriaId = UUID.randomUUID();
        banco.sql("""
                insert into empresa (
                    id, razao_social, nome_fantasia, cnpj, ativa, versao, criado_em, atualizado_em
                ) values (:id, 'Empresa de Teste', 'Teste', '11222333000181', true, 0, now(), now())
                """)
                .param("id", empresaId)
                .update();
        banco.sql("""
                insert into auditoria_operacao (
                    id, empresa_id, ator_id, metodo_http, caminho, status_http, endereco_ip,
                    correlacao, ocorrido_em, versao, criado_em, atualizado_em
                ) values (
                    :id, :empresaId, 'usuario-teste', 'POST', '/api/v1/teste', 201, '127.0.0.1',
                    'correlacao-teste', now(), 0, now(), now()
                )
                """)
                .param("id", auditoriaId)
                .param("empresaId", empresaId)
                .update();

        assertThatThrownBy(() -> banco.sql("""
                update auditoria_operacao set caminho = '/alterado' where id = :id
                """)
                .param("id", auditoriaId)
                .update())
                .isInstanceOf(DataAccessException.class);
    }
}
