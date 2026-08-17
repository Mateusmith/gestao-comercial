package br.com.commercecore.platform.internal;

import br.com.commercecore.platform.AuditRecordedEvent;
import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auditoria_operacao")
public class AuditEntity extends AbstractEntity {
    @Column(name = "empresa_id")
    private UUID empresaId;
    @Column(name = "ator_id", nullable = false, length = 120)
    private String atorId;
    @Column(name = "metodo_http", nullable = false, length = 10)
    private String metodoHttp;
    @Column(name = "caminho", nullable = false, length = 300)
    private String caminho;
    @Column(name = "parametros", length = 1000)
    private String parametros;
    @Column(name = "status_http", nullable = false)
    private int statusHttp;
    @Column(name = "endereco_ip", nullable = false, length = 64)
    private String enderecoIp;
    @Column(name = "correlacao", nullable = false, length = 100)
    private String correlacao;
    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected AuditEntity() {
    }

    public AuditEntity(AuditRecordedEvent evento) {
        empresaId = evento.empresaId();
        atorId = evento.atorId();
        metodoHttp = evento.metodoHttp();
        caminho = evento.caminho();
        parametros = evento.parametros();
        statusHttp = evento.statusHttp();
        enderecoIp = evento.enderecoIp();
        correlacao = evento.correlacao();
        ocorridoEm = evento.ocorridoEm();
    }

    public UUID getEmpresaId() { return empresaId; }
    public String getAtorId() { return atorId; }
    public String getMetodoHttp() { return metodoHttp; }
    public String getCaminho() { return caminho; }
    public String getParametros() { return parametros; }
    public int getStatusHttp() { return statusHttp; }
    public String getEnderecoIp() { return enderecoIp; }
    public String getCorrelacao() { return correlacao; }
    public Instant getOcorridoEm() { return ocorridoEm; }
}
