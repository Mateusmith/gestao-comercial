package br.com.commercecore.purchasing.internal;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierQuoteRepository extends JpaRepository<SupplierQuoteEntity, UUID> {
    @Query("""
            select cotacao from SupplierQuoteEntity cotacao
            where cotacao.requisicaoId = :requisicaoId
            order by cotacao.total.valor asc, cotacao.criadoEm asc
            """)
    List<SupplierQuoteEntity> findByRequisicaoIdOrderByTotalAsc(
            @Param("requisicaoId") UUID requisicaoId);

    @Query(value = "select nextval('cotacao_fornecedor_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
