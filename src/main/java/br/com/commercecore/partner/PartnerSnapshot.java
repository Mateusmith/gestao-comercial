package br.com.commercecore.partner;

import java.util.Set;
import java.util.UUID;

public record PartnerSnapshot(UUID id, UUID empresaId, String nome, Set<PartnerRole> papeis, boolean ativo) {
    public boolean possuiPapel(PartnerRole papel) {
        return papeis.contains(papel);
    }
}
