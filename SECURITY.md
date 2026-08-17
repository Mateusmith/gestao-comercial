# Política de segurança

## Versões suportadas

| Versão | Suporte |
|---|---|
| 1.x | Sim |
| anteriores | Não |

## Relatar uma vulnerabilidade

Não abra uma issue pública para uma vulnerabilidade ainda não corrigida. Use o recurso **Security advisories** do repositório e inclua:

- versão e configuração afetadas;
- passos mínimos para reprodução;
- impacto observado ou esperado;
- sugestão de correção, quando disponível.

O recebimento será confirmado em até cinco dias úteis. A correção e a divulgação serão coordenadas conforme gravidade e possibilidade de exploração.

## Ambiente de demonstração

As senhas presentes no `compose.yaml`, realm do Keycloak e coleção Postman são apenas dados locais previsíveis. Antes de qualquer implantação externa:

- substitua todas as credenciais;
- use TLS e um gerenciador de segredos;
- desabilite Swagger, pgAdmin e endpoints detalhados de saúde;
- troque a credencial técnica de métricas e restrinja o acesso às interfaces de monitoramento;
- restrinja CORS e origens do emissor JWT;
- use clientes OAuth confidenciais quando apropriado;
- configure backup, rotação, retenção de logs e limites de requisição.
