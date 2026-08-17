package br.com.commercecore.organization;

import br.com.commercecore.organization.internal.BranchEntity;
import br.com.commercecore.organization.internal.BranchRepository;
import br.com.commercecore.organization.internal.CompanyEntity;
import br.com.commercecore.organization.internal.CompanyRepository;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.Documentos;
import br.com.commercecore.shared.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

    private final CompanyRepository empresas;
    private final BranchRepository filiais;

    public OrganizationService(CompanyRepository empresas, BranchRepository filiais) {
        this.empresas = empresas;
        this.filiais = filiais;
    }

    @Transactional
    public CompanyResponse criarEmpresa(CreateCompanyRequest requisicao) {
        String cnpj = validarCnpj(requisicao.cnpj());
        if (empresas.existsByCnpj(cnpj)) {
            throw new BusinessRuleException("CNPJ_DUPLICADO", "Ja existe uma empresa com este CNPJ.");
        }
        CompanyEntity empresa = new CompanyEntity(requisicao.razaoSocial().trim(), requisicao.nomeFantasia().trim(), cnpj);
        return resposta(empresas.save(empresa));
    }

    @Transactional
    public BranchResponse criarFilial(UUID empresaId, CreateBranchRequest requisicao) {
        if (!empresas.existsById(empresaId)) {
            throw new NotFoundException("Empresa nao encontrada.");
        }
        String cnpj = validarCnpj(requisicao.cnpj());
        if (filiais.existsByCnpj(cnpj)) {
            throw new BusinessRuleException("CNPJ_DUPLICADO", "Ja existe uma filial com este CNPJ.");
        }
        BranchEntity filial = new BranchEntity(
                empresaId,
                requisicao.codigo().trim().toUpperCase(),
                requisicao.nome().trim(),
                cnpj,
                requisicao.fusoHorario().trim());
        return resposta(filiais.save(filial));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> listarEmpresas() {
        return empresas.findAll().stream().map(OrganizationService::resposta).toList();
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> listarFiliais(UUID empresaId) {
        if (!empresas.existsById(empresaId)) {
            throw new NotFoundException("Empresa nao encontrada.");
        }
        return filiais.findByEmpresaIdOrderByNome(empresaId).stream().map(OrganizationService::resposta).toList();
    }

    static CompanyResponse resposta(CompanyEntity empresa) {
        return new CompanyResponse(empresa.getId(), empresa.getRazaoSocial(), empresa.getNomeFantasia(), empresa.getCnpj(), empresa.isAtiva(), empresa.getVersao());
    }

    static BranchResponse resposta(BranchEntity filial) {
        return new BranchResponse(filial.getId(), filial.getEmpresaId(), filial.getCodigo(), filial.getNome(), filial.getCnpj(), filial.getFusoHorario(), filial.isAtiva(), filial.getVersao());
    }

    private String validarCnpj(String valor) {
        String cnpj = Documentos.somenteDigitos(valor);
        if (!Documentos.cnpjValido(cnpj)) {
            throw new BusinessRuleException("CNPJ_INVALIDO", "O CNPJ informado e invalido.");
        }
        return cnpj;
    }
}
