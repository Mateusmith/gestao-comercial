package br.com.commercecore.finance.internal;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccountEntity, UUID> {
    boolean existsByFilialIdAndCodigo(UUID filialId, String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from FinancialAccountEntity c where c.id = :id")
    Optional<FinancialAccountEntity> findWithLockById(UUID id);
}
