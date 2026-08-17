package br.com.commercecore.finance.internal;

import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evento_financeiro_processado")
public class ProcessedFinancialEventEntity extends AbstractEntity {

    @Column(name = "evento_id", nullable = false, unique = true)
    private UUID eventoId;

    @Column(name = "tipo_evento", nullable = false, length = 100)
    private String tipoEvento;

    @Column(name = "processado_em", nullable = false)
    private Instant processadoEm;

    protected ProcessedFinancialEventEntity() {
    }

    public ProcessedFinancialEventEntity(UUID eventoId, String tipoEvento, Instant processadoEm) {
        this.eventoId = eventoId;
        this.tipoEvento = tipoEvento;
        this.processadoEm = processadoEm;
    }
}
