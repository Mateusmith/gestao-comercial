package br.com.commercecore.partner;

import br.com.commercecore.partner.internal.PartnerEntity;
import br.com.commercecore.partner.internal.PartnerRepository;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.Documentos;
import br.com.commercecore.shared.NotFoundException;
import br.com.commercecore.shared.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartnerService {

    private final PartnerRepository parceiros;

    public PartnerService(PartnerRepository parceiros) {
        this.parceiros = parceiros;
    }

    @Transactional
    public PartnerResponse criar(CreatePartnerRequest requisicao) {
        String documento = validarDocumento(requisicao.tipoPessoa(), requisicao.cpfCnpj());
        if (parceiros.existsByEmpresaIdAndCpfCnpj(requisicao.empresaId(), documento)) {
            throw new BusinessRuleException("PARCEIRO_DUPLICADO", "Ja existe um parceiro com este documento na empresa.");
        }
        PartnerEntity parceiro = new PartnerEntity(
                requisicao.empresaId(),
                requisicao.tipoPessoa(),
                requisicao.nomeRazaoSocial().trim(),
                normalizarOpcional(requisicao.nomeFantasia()),
                documento,
                normalizarOpcional(requisicao.email()),
                normalizarOpcional(requisicao.telefone()),
                requisicao.papeis());
        return resposta(parceiros.save(parceiro));
    }

    @Transactional(readOnly = true)
    public PageResponse<PartnerResponse> listar(UUID empresaId, int pagina, int tamanho) {
        var paginacao = PageRequest.of(pagina, Math.min(tamanho, 100), Sort.by("nomeRazaoSocial").ascending());
        return PageResponse.de(parceiros.findByEmpresaId(empresaId, paginacao).map(PartnerService::resposta));
    }

    @Transactional(readOnly = true)
    public PartnerSnapshot obter(UUID parceiroId, UUID empresaId, PartnerRole papelObrigatorio) {
        PartnerEntity parceiro = parceiros.findById(parceiroId)
                .orElseThrow(() -> new NotFoundException("Parceiro comercial nao encontrado."));
        if (!parceiro.getEmpresaId().equals(empresaId)) {
            throw new BusinessRuleException("PARCEIRO_OUTRA_EMPRESA", "O parceiro nao pertence a esta empresa.");
        }
        if (!parceiro.isAtivo() || !parceiro.possuiPapel(papelObrigatorio)) {
            throw new BusinessRuleException("PAPEL_PARCEIRO_INVALIDO", "O parceiro nao esta ativo com o papel exigido para a operacao.");
        }
        return new PartnerSnapshot(parceiro.getId(), parceiro.getEmpresaId(), parceiro.getNomeRazaoSocial(), parceiro.getPapeis(), parceiro.isAtivo());
    }

    private String validarDocumento(PersonType tipo, String valor) {
        String documento = Documentos.somenteDigitos(valor);
        boolean valido = tipo == PersonType.JURIDICA ? Documentos.cnpjValido(documento) : Documentos.cpfValido(documento);
        if (!valido) {
            throw new BusinessRuleException("DOCUMENTO_INVALIDO", "O CPF ou CNPJ informado e invalido.");
        }
        return documento;
    }

    private static PartnerResponse resposta(PartnerEntity parceiro) {
        return new PartnerResponse(
                parceiro.getId(), parceiro.getEmpresaId(), parceiro.getTipoPessoa(), parceiro.getNomeRazaoSocial(),
                parceiro.getNomeFantasia(), parceiro.getCpfCnpj(), parceiro.getEmail(), parceiro.getTelefone(),
                parceiro.getPapeis(), parceiro.isAtivo(), parceiro.getVersao());
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
