package br.com.commercecore;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArchitectureTest {

    @Test
    void modulosDevemRespeitarAsDependenciasDeclaradas() {
        ApplicationModules.of(CommerceCoreApplication.class).verify();
    }
}
