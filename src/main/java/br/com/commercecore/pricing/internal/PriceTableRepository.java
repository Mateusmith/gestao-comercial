package br.com.commercecore.pricing.internal;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceTableRepository extends JpaRepository<PriceTableEntity, UUID> {

    @Query("""
            select t from PriceTableEntity t
            join fetch t.itens i
            where t.empresaId = :empresaId
              and (t.filialId = :filialId or t.filialId is null)
              and i.skuId = :skuId
              and t.ativa = true
              and t.vigenteDe <= :instante
              and (t.vigenteAte is null or t.vigenteAte >= :instante)
            order by t.filialId desc nulls last, t.vigenteDe desc
            """)
    List<PriceTableEntity> buscarAplicaveis(
            @Param("empresaId") UUID empresaId,
            @Param("filialId") UUID filialId,
            @Param("skuId") UUID skuId,
            @Param("instante") Instant instante);
}
