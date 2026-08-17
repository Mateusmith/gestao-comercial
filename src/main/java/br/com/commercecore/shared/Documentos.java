package br.com.commercecore.shared;

public final class Documentos {

    private Documentos() {
    }

    public static String somenteDigitos(String documento) {
        return documento == null ? "" : documento.replaceAll("\\D", "");
    }

    public static boolean cnpjValido(String documento) {
        String cnpj = somenteDigitos(documento);
        if (cnpj.length() != 14 || cnpj.chars().distinct().count() == 1) {
            return false;
        }
        return digito(cnpj, 12) == cnpj.charAt(12) - '0'
                && digito(cnpj, 13) == cnpj.charAt(13) - '0';
    }

    public static boolean cpfValido(String documento) {
        String cpf = somenteDigitos(documento);
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digitoCpf(cpf, 9) == cpf.charAt(9) - '0'
                && digitoCpf(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digito(String valor, int tamanho) {
        int[] pesos = tamanho == 12
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int indice = 0; indice < tamanho; indice++) {
            soma += (valor.charAt(indice) - '0') * pesos[indice];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int digitoCpf(String valor, int tamanho) {
        int soma = 0;
        for (int indice = 0; indice < tamanho; indice++) {
            soma += (valor.charAt(indice) - '0') * (tamanho + 1 - indice);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
