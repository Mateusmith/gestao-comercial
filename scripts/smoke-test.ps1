param(
    [string] $UrlApi = "http://localhost:8082",
    [string] $UrlIdentidade = "http://localhost:18083",
    [string] $Usuario = "admin@commercecore.local",
    [string] $Senha = "CommerceCore@123"
)

$ErrorActionPreference = "Stop"

$empresaId = "11111111-1111-1111-1111-111111111111"
$filialId = "22222222-2222-2222-2222-222222222222"
$clienteId = "33333333-3333-3333-3333-333333333333"
$fornecedorId = "44444444-4444-4444-4444-444444444444"
$skuId = "77777777-7777-7777-7777-777777777777"
$depositoId = "88888888-8888-8888-8888-888888888888"
$contaFinanceiraId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
$sufixoExecucao = [DateTimeOffset]::UtcNow.ToString("yyyyMMddHHmmssfff")

function Confirmar {
    param([bool] $Condicao, [string] $Mensagem)

    if (-not $Condicao) {
        throw "Falha: $Mensagem"
    }
    Write-Host "[OK] $Mensagem" -ForegroundColor Green
}

function Obter-Token {
    param([string] $NomeUsuario = $Usuario, [string] $SenhaUsuario = $Senha)

    $resposta = Invoke-RestMethod `
        -Method Post `
        -Uri "$UrlIdentidade/realms/commercecore/protocol/openid-connect/token" `
        -ContentType "application/x-www-form-urlencoded" `
        -Body @{
            grant_type = "password"
            client_id = "commercecore-postman"
            username = $NomeUsuario
            password = $SenhaUsuario
        }
    return $resposta.access_token
}

$token = Obter-Token
$cabecalhosBase = @{
    Authorization = "Bearer $token"
    "X-Empresa-ID" = $empresaId
    "X-Filial-ID" = $filialId
    "X-Correlation-ID" = "smoke-$sufixoExecucao"
}

function Invocar-Api {
    param(
        [ValidateSet("GET", "POST", "PUT", "PATCH", "DELETE")]
        [string] $Metodo,
        [string] $Caminho,
        [object] $Corpo,
        [hashtable] $CabecalhosAdicionais = @{},
        [hashtable] $CabecalhosPersonalizados
    )

    $cabecalhos = @{}
    $origem = if ($null -eq $CabecalhosPersonalizados) { $cabecalhosBase } else { $CabecalhosPersonalizados }
    foreach ($item in $origem.GetEnumerator()) {
        $cabecalhos[$item.Key] = $item.Value
    }
    foreach ($item in $CabecalhosAdicionais.GetEnumerator()) {
        $cabecalhos[$item.Key] = $item.Value
    }

    $parametros = @{
        Method = $Metodo
        Uri = "$UrlApi$Caminho"
        Headers = $cabecalhos
        SkipHttpErrorCheck = $true
    }
    if ($null -ne $Corpo) {
        $parametros.ContentType = "application/json"
        $parametros.Body = $Corpo | ConvertTo-Json -Depth 20 -Compress
    }

    $resposta = Invoke-WebRequest @parametros
    $dados = $null
    $conteudo = if ($resposta.Content -is [byte[]]) {
        [Text.Encoding]::UTF8.GetString($resposta.Content)
    } else {
        $resposta.Content
    }
    if (-not [string]::IsNullOrWhiteSpace($conteudo)) {
        try {
            $dados = $conteudo | ConvertFrom-Json
        } catch {
            $dados = $resposta.Content
        }
    }

    return [pscustomobject]@{
        Status = [int] $resposta.StatusCode
        Dados = $dados
        Cabecalhos = $resposta.Headers
        Tamanho = $resposta.RawContentLength
    }
}

function Esperar-Titulos {
    param(
        [string] $Tipo,
        [string[]] $Origens,
        [int] $QuantidadeEsperada
    )

    for ($tentativa = 1; $tentativa -le 20; $tentativa++) {
        $resposta = Invocar-Api GET "/api/v1/financeiro/titulos?empresaId=$empresaId&tipo=$Tipo&tamanho=100"
        if ($resposta.Status -eq 200) {
            $titulos = @($resposta.Dados.conteudo | Where-Object { $Origens -contains $_.origemId })
            if ($titulos.Count -eq $QuantidadeEsperada) {
                return $titulos | Sort-Object documentoOrigem, parcela
            }
        }
        Start-Sleep -Milliseconds 500
    }
    throw "Os $QuantidadeEsperada titulos do tipo $Tipo nao foram publicados no prazo esperado."
}

Write-Host "`nCommerceCore - validacao ponta a ponta" -ForegroundColor Cyan

$saude = Invoke-RestMethod "$UrlApi/actuator/health"
Confirmar ($saude.status -eq "UP") "API esta saudavel"

$semToken = Invoke-WebRequest `
    -Uri "$UrlApi/api/v1/empresas" `
    -SkipHttpErrorCheck
Confirmar ($semToken.StatusCode -eq 401) "Endpoint protegido rejeita chamada sem JWT"

$saldoAntesDoCenario = Invocar-Api GET "/api/v1/estoque/depositos/$depositoId/skus/$skuId/saldos"
$saldoFisicoInicial = [decimal] @($saldoAntesDoCenario.Dados)[0].saldoFisico

$preco = Invocar-Api POST "/api/v1/precificacao/simulacoes" @{
    empresaId = $empresaId
    filialId = $filialId
    skuId = $skuId
    quantidade = 2
    codigoCupom = "BEMVINDO10"
}
Confirmar ($preco.Status -eq 200 -and [decimal] $preco.Dados.subtotal -eq 9360) `
    "Precificacao aplica tabela, promocao e cupom"

$orcamento = Invocar-Api POST "/api/v1/vendas/orcamentos" @{
    empresaId = $empresaId
    filialId = $filialId
    clienteId = $clienteId
    validoAte = [DateTimeOffset]::UtcNow.AddDays(7).ToString("o")
    codigoCupom = "BEMVINDO10"
    itens = @(@{ skuId = $skuId; quantidade = 2 })
}
Confirmar ($orcamento.Status -eq 201 -and $orcamento.Dados.status -eq "EM_EDICAO") `
    "Orcamento criado com fotografia de preco"

$orcamentoId = $orcamento.Dados.id
$enviado = Invocar-Api POST "/api/v1/vendas/orcamentos/$orcamentoId/envio"
Confirmar ($enviado.Status -eq 200 -and $enviado.Dados.status -eq "ENVIADO") "Orcamento enviado"

$aceito = Invocar-Api POST "/api/v1/vendas/orcamentos/$orcamentoId/aceite"
Confirmar ($aceito.Status -eq 200 -and $aceito.Dados.status -eq "ACEITO") "Orcamento aceito"

$pedido = Invocar-Api POST "/api/v1/vendas/orcamentos/$orcamentoId/conversao" @{
    depositoId = $depositoId
    numeroParcelas = 2
    primeiroVencimento = (Get-Date).AddDays(15).ToString("yyyy-MM-dd")
}
Confirmar ($pedido.Status -eq 201 -and $pedido.Dados.status -eq "RASCUNHO") `
    "Orcamento convertido em pedido"

$pedidoVendaId = $pedido.Dados.id
$confirmado = Invocar-Api POST "/api/v1/vendas/pedidos/$pedidoVendaId/confirmacao"
Confirmar ($confirmado.Status -eq 200 -and $confirmado.Dados.status -eq "CONFIRMADO") `
    "Pedido confirmado"

$saldoReservado = Invocar-Api GET "/api/v1/estoque/depositos/$depositoId/skus/$skuId/saldos"
$linhaSaldo = @($saldoReservado.Dados)[0]
Confirmar ([decimal] $linhaSaldo.saldoReservado -eq 2) "Estoque reservado por FEFO"

$fatura = Invocar-Api POST "/api/v1/vendas/pedidos/$pedidoVendaId/faturamento"
Confirmar ($fatura.Status -eq 200 -and [decimal] $fatura.Dados.total -eq 9360) `
    "Pedido faturado e estoque baixado"

$saldoAposVenda = Invocar-Api GET "/api/v1/estoque/depositos/$depositoId/skus/$skuId/saldos"
$linhaSaldo = @($saldoAposVenda.Dados)[0]
Confirmar ([decimal] $linhaSaldo.saldoFisico -eq ($saldoFisicoInicial - 2) -and [decimal] $linhaSaldo.saldoReservado -eq 0) `
    "Saldo fisico e reserva refletem o faturamento"

$recebiveis = @(Esperar-Titulos "RECEBER" @($fatura.Dados.id) 2)
Confirmar (($recebiveis | Measure-Object saldo -Sum).Sum -eq 9360) `
    "Evento de venda gerou duas parcelas exatas a receber"

$primeiraLiquidacao = $null
foreach ($titulo in $recebiveis) {
    $chave = "smoke-cr-$($titulo.parcela)-$sufixoExecucao"
    $liquidacao = Invocar-Api POST "/api/v1/financeiro/titulos/$($titulo.id)/liquidacoes" @{
        contaFinanceiraId = $contaFinanceiraId
        valor = $titulo.saldo
        formaPagamento = "PIX"
        observacao = "Recebimento automatizado do smoke test"
    } @{ "Idempotency-Key" = $chave }
    Confirmar ($liquidacao.Status -eq 200 -and [decimal] $liquidacao.Dados.saldoTituloAposOperacao -eq 0) `
        "Parcela $($titulo.parcela) recebida"

    if ($null -eq $primeiraLiquidacao) {
        $primeiraLiquidacao = $liquidacao
        $repetida = Invocar-Api POST "/api/v1/financeiro/titulos/$($titulo.id)/liquidacoes" @{
            contaFinanceiraId = $contaFinanceiraId
            valor = $titulo.saldo
            formaPagamento = "PIX"
            observacao = "Recebimento automatizado do smoke test"
        } @{ "Idempotency-Key" = $chave }
        Confirmar ($repetida.Status -eq 200 -and $repetida.Dados.id -eq $liquidacao.Dados.id) `
            "Repeticao idempotente nao duplica o recebimento"
    }
}
$saldoContaAntesDosRecebimentos = `
    [decimal] $primeiraLiquidacao.Dados.saldoContaAposOperacao - [decimal] $primeiraLiquidacao.Dados.valor
$saldoContaAposRecebimentos = [decimal] $liquidacao.Dados.saldoContaAposOperacao
Confirmar (($saldoContaAposRecebimentos - $saldoContaAntesDosRecebimentos) -eq 9360) `
    "Conta financeira recebeu o total da venda"

$requisicao = Invocar-Api POST "/api/v1/compras/requisicoes" @{
    empresaId = $empresaId
    filialId = $filialId
    justificativa = "Reposicao planejada do estoque de notebooks"
    itens = @(@{ skuId = $skuId; quantidade = 10 })
}
Confirmar ($requisicao.Status -eq 201 -and $requisicao.Dados.status -eq "SOLICITADA") `
    "Requisicao de compra criada"

$requisicaoId = $requisicao.Dados.id
$aprovada = Invocar-Api POST "/api/v1/compras/requisicoes/$requisicaoId/aprovacao"
Confirmar ($aprovada.Status -eq 200 -and $aprovada.Dados.status -eq "APROVADA") `
    "Requisicao aprovada com autoria"

$cotacao = Invocar-Api POST "/api/v1/compras/requisicoes/$requisicaoId/cotacoes" @{
    fornecedorId = $fornecedorId
    validoAte = [DateTimeOffset]::UtcNow.AddDays(10).ToString("o")
    itens = @(@{ skuId = $skuId; quantidade = 10; custoUnitario = 4000 })
}
Confirmar ($cotacao.Status -eq 201 -and [decimal] $cotacao.Dados.total -eq 40000) `
    "Cotacao do fornecedor registrada"

$comparativo = Invocar-Api GET "/api/v1/compras/requisicoes/$requisicaoId/comparativo"
Confirmar ($comparativo.Status -eq 200 -and @($comparativo.Dados).Count -ge 1) `
    "Comparativo de cotacoes ordenado por custo"

$pedidoCompra = Invocar-Api POST "/api/v1/compras/cotacoes/$($cotacao.Dados.id)/pedido" @{
    depositoId = $depositoId
    numeroParcelas = 2
    primeiroVencimento = (Get-Date).AddDays(30).ToString("yyyy-MM-dd")
}
Confirmar ($pedidoCompra.Status -eq 201 -and $pedidoCompra.Dados.status -eq "EMITIDO") `
    "Pedido de compra emitido"

$pedidoCompraId = $pedidoCompra.Dados.id
$chaveRecebimento1 = "smoke-rec-1-$sufixoExecucao"
$recebimento1 = Invocar-Api POST "/api/v1/compras/pedidos/$pedidoCompraId/recebimentos" @{
    documentoFornecedor = "NF-$sufixoExecucao-1"
    itens = @(@{ skuId = $skuId; quantidade = 4 })
} @{ "Idempotency-Key" = $chaveRecebimento1 }
Confirmar ($recebimento1.Status -eq 200 -and [decimal] $recebimento1.Dados.valorTotal -eq 16000) `
    "Primeiro recebimento parcial atualiza o estoque"

$recebimentoRepetido = Invocar-Api POST "/api/v1/compras/pedidos/$pedidoCompraId/recebimentos" @{
    documentoFornecedor = "NF-$sufixoExecucao-1"
    itens = @(@{ skuId = $skuId; quantidade = 4 })
} @{ "Idempotency-Key" = $chaveRecebimento1 }
Confirmar ($recebimentoRepetido.Status -eq 200 -and $recebimentoRepetido.Dados.id -eq $recebimento1.Dados.id) `
    "Recebimento repetido nao duplica entrada"

$pedidoParcial = Invocar-Api GET "/api/v1/compras/pedidos/$pedidoCompraId"
Confirmar ($pedidoParcial.Dados.status -eq "PARCIALMENTE_RECEBIDO") `
    "Pedido permanece parcial ate completar todos os itens"

$recebimento2 = Invocar-Api POST "/api/v1/compras/pedidos/$pedidoCompraId/recebimentos" @{
    documentoFornecedor = "NF-$sufixoExecucao-2"
    itens = @(@{ skuId = $skuId; quantidade = 6 })
} @{ "Idempotency-Key" = "smoke-rec-2-$sufixoExecucao" }
Confirmar ($recebimento2.Status -eq 200 -and [decimal] $recebimento2.Dados.valorTotal -eq 24000) `
    "Segundo recebimento completa a quantidade"

$pedidoRecebido = Invocar-Api GET "/api/v1/compras/pedidos/$pedidoCompraId"
Confirmar ($pedidoRecebido.Dados.status -eq "RECEBIDO") "Pedido de compra concluido"

$saldoFinal = Invocar-Api GET "/api/v1/estoque/depositos/$depositoId/skus/$skuId/saldos"
$linhaSaldo = @($saldoFinal.Dados)[0]
Confirmar ([decimal] $linhaSaldo.saldoFisico -eq ($saldoFisicoInicial + 8)) `
    "Estoque final concilia venda e dois recebimentos"

$pagaveis = @(Esperar-Titulos "PAGAR" @($recebimento1.Dados.id, $recebimento2.Dados.id) 4)
Confirmar (($pagaveis | Measure-Object saldo -Sum).Sum -eq 40000) `
    "Recebimentos geraram quatro parcelas exatas a pagar"

$primeiroPagavel = $pagaveis | Where-Object { $_.origemId -eq $recebimento1.Dados.id } | Select-Object -First 1
$pagamento = Invocar-Api POST "/api/v1/financeiro/titulos/$($primeiroPagavel.id)/liquidacoes" @{
    contaFinanceiraId = $contaFinanceiraId
    valor = $primeiroPagavel.saldo
    formaPagamento = "TRANSFERENCIA"
    observacao = "Pagamento do primeiro titulo da compra"
} @{ "Idempotency-Key" = "smoke-cp-1-$sufixoExecucao" }
Confirmar ($pagamento.Status -eq 200 -and `
        [decimal] $pagamento.Dados.saldoContaAposOperacao -eq ($saldoContaAposRecebimentos - [decimal] $primeiroPagavel.saldo)) `
    "Conta a pagar liquidada com conciliacao do caixa"

$saldoDisponivel = [decimal] $pagamento.Dados.saldoContaAposOperacao
$pagavelSemSaldo = $pagaveis |
    Where-Object { $_.id -ne $primeiroPagavel.id -and [decimal] $_.saldo -gt $saldoDisponivel } |
    Select-Object -First 1
Confirmar ($null -ne $pagavelSemSaldo) "Cenario possui um titulo maior que o saldo disponivel"
$semSaldo = Invocar-Api POST "/api/v1/financeiro/titulos/$($pagavelSemSaldo.id)/liquidacoes" @{
    contaFinanceiraId = $contaFinanceiraId
    valor = $pagavelSemSaldo.saldo
    formaPagamento = "TRANSFERENCIA"
} @{ "Idempotency-Key" = "smoke-sem-saldo-$sufixoExecucao" }
Confirmar ($semSaldo.Status -eq 422 -and $semSaldo.Dados.codigo -eq "SALDO_FINANCEIRO_INSUFICIENTE") `
    "Regra financeira impede pagamento sem saldo"

$inicio = (Get-Date).AddDays(-1).ToString("yyyy-MM-dd")
$fim = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
$resumo = Invocar-Api GET "/api/v1/relatorios/resumo-gerencial?empresaId=$empresaId&filialId=$filialId&inicio=$inicio&fim=$fim"
Confirmar ($resumo.Status -eq 200) "Resumo gerencial consolida os modulos"

$xlsx = Invocar-Api GET "/api/v1/relatorios/exportacoes/gerencial.xlsx?empresaId=$empresaId&filialId=$filialId&inicio=$inicio&fim=$fim"
Confirmar ($xlsx.Status -eq 200 -and $xlsx.Tamanho -gt 1000) "Relatorio XLSX foi gerado"

$inicioAuditoria = [Uri]::EscapeDataString([DateTimeOffset]::UtcNow.AddHours(-2).ToString("o"))
$fimAuditoria = [Uri]::EscapeDataString([DateTimeOffset]::UtcNow.AddHours(1).ToString("o"))
$auditoria = Invocar-Api GET "/api/v1/auditoria?empresaId=$empresaId&inicio=$inicioAuditoria&fim=$fimAuditoria&tamanho=100"
Confirmar ($auditoria.Status -eq 200 -and $auditoria.Dados.totalElementos -ge 10) `
    "Trilha de auditoria registrou as operacoes mutaveis"

Write-Host "`nCenario concluido com sucesso." -ForegroundColor Cyan
Write-Host "Orcamento: $orcamentoId"
Write-Host "Pedido de venda: $pedidoVendaId"
Write-Host "Fatura: $($fatura.Dados.id)"
Write-Host "Pedido de compra: $pedidoCompraId"
Write-Host "Correlacao: smoke-$sufixoExecucao"
