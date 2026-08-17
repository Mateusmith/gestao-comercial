@org.springframework.modulith.ApplicationModule(
        displayName = "Vendas",
        allowedDependencies = {"shared", "organization", "partner", "catalog", "pricing", "inventory"}
)
package br.com.commercecore.sales;
