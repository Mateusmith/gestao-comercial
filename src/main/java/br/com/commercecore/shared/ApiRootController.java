package br.com.commercecore.shared;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiRootController {

    @GetMapping("/")
    Map<String, Object> raiz() {
        return Map.of(
                "aplicacao", "Gestão Comercial",
                "versao", "1.0.0",
                "documentacao", "/swagger-ui.html",
                "saude", "/actuator/health");
    }
}
