package br.com.ddsfacil.configuracao.multitenant;

public final class ContextoEmpresa {

    private static final ThreadLocal<Long> EMPRESA_ATUAL = new ThreadLocal<>();

    private ContextoEmpresa() {
    }

    public static void definirEmpresaId(Long empresaId) {
        EMPRESA_ATUAL.set(empresaId);
    }

    public static Long obterEmpresaId() {
        return EMPRESA_ATUAL.get();
    }

    public static Long obterEmpresaIdObrigatorio() {
        Long empresaId = EMPRESA_ATUAL.get();
        if (empresaId == null) {
            throw new IllegalStateException("Empresa não informada no contexto da requisição.");
        }
        return empresaId;
    }

    public static void limpar() {
        EMPRESA_ATUAL.remove();
    }
}
