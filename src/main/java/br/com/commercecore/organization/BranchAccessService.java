package br.com.commercecore.organization;

import br.com.commercecore.organization.internal.BranchEntity;
import br.com.commercecore.organization.internal.BranchRepository;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.CurrentActor;
import br.com.commercecore.shared.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BranchAccessService {

    private final BranchRepository filiais;
    private final CurrentActor atorAtual;

    public BranchAccessService(BranchRepository filiais, CurrentActor atorAtual) {
        this.filiais = filiais;
        this.atorAtual = atorAtual;
    }

    public BranchResponse garantirAcesso(UUID filialId) {
        BranchEntity filial = filiais.findById(filialId)
                .orElseThrow(() -> new NotFoundException("Filial nao encontrada."));
        boolean administrador = atorAtual.temPapel("ADMINISTRADOR");
        boolean permitida = atorAtual.filiaisPermitidas().contains(filialId.toString());
        if (!administrador && !permitida) {
            throw new BusinessRuleException("FILIAL_NAO_AUTORIZADA", "O usuario nao possui acesso a esta filial.");
        }
        if (!filial.isAtiva()) {
            throw new BusinessRuleException("FILIAL_INATIVA", "A filial esta inativa.");
        }
        return OrganizationService.resposta(filial);
    }
}
