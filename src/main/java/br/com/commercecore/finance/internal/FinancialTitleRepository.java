package br.com.commercecore.finance.internal;

import br.com.commercecore.finance.FinancialTitleStatus;
import br.com.commercecore.finance.TitleType;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface FinancialTitleRepository extends JpaRepository<FinancialTitleEntity, UUID> {
    boolean existsByTipoOrigemAndOrigemIdAndParcela(String tipoOrigem, UUID origemId, int parcela);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from FinancialTitleEntity t where t.id = :id")
    Optional<FinancialTitleEntity> buscarComBloqueio(UUID id);

    Page<FinancialTitleEntity> findByEmpresaIdAndTipoAndStatusIn(
            UUID empresaId, TitleType tipo, Collection<FinancialTitleStatus> status, Pageable pageable);

    @Query(value = "select nextval('titulo_financeiro_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
