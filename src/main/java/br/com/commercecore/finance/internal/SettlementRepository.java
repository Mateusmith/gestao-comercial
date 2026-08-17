package br.com.commercecore.finance.internal;

import br.com.commercecore.finance.SettlementKind;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<SettlementEntity, UUID> {
    Optional<SettlementEntity> findByChaveIdempotencia(String chaveIdempotencia);
    boolean existsByLiquidacaoOriginalIdAndTipo(UUID liquidacaoOriginalId, SettlementKind tipo);
}
