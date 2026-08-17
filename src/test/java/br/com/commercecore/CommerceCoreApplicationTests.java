package br.com.commercecore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
class CommerceCoreApplicationTests {

	private final JdbcClient banco;
	private final EventPublicationRegistry publicacoes;
	private final MockMvc requisicoes;

	@Autowired
	CommerceCoreApplicationTests(JdbcClient banco, EventPublicationRegistry publicacoes, MockMvc requisicoes) {
		this.banco = banco;
		this.publicacoes = publicacoes;
		this.requisicoes = requisicoes;
	}

	@Test
	void deveSubirOContextoEAplicarTodasAsMigracoes() {
		Integer migracoes = banco.sql("select count(*) from flyway_schema_history where success = true")
				.query(Integer.class)
				.single();

		assertThat(migracoes).isEqualTo(6);
		assertThat(publicacoes.findIncompletePublications()).isEmpty();
	}

	@Test
	void deveProtegerAsMetricasComCredencialTecnica() throws Exception {
		requisicoes.perform(get("/actuator/prometheus"))
				.andExpect(status().isUnauthorized());

		requisicoes.perform(get("/actuator/prometheus")
					.with(httpBasic("prometheus", "Prometheus@123")))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("jvm_info")));
	}

	@Test
	void credencialDeMetricasNaoDeveAcessarApisDeNegocio() throws Exception {
		requisicoes.perform(get("/api/v1/empresas")
					.with(httpBasic("prometheus", "Prometheus@123")))
				.andExpect(status().isUnauthorized());
	}
}
