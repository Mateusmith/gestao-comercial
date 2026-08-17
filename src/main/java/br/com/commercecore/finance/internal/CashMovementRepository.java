package br.com.commercecore.finance.internal;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashMovementRepository extends JpaRepository<CashMovementEntity, UUID> {
}
