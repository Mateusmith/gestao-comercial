package br.com.commercecore.pricing.internal;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionRepository extends JpaRepository<PromotionEntity, UUID> {

    @Query("""
            select p from PromotionEntity p
            where p.empresaId = :empresaId
              and (p.filialId = :filialId or p.filialId is null)
              and p.skuId = :skuId
              and p.ativa = true
              and p.inicio <= :instante
              and p.fim >= :instante
              and p.quantidadeMinima <= :quantidade
              and (p.codigoCupom is null or upper(p.codigoCupom) = :cupom)
            order by p.prioridade desc, p.inicio desc
            """)
    List<PromotionEntity> buscarAplicaveis(
            @Param("empresaId") UUID empresaId,
            @Param("filialId") UUID filialId,
            @Param("skuId") UUID skuId,
            @Param("quantidade") java.math.BigDecimal quantidade,
            @Param("instante") Instant instante,
            @Param("cupom") String cupom);
}
