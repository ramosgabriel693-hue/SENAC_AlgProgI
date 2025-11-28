import java.time.LocalTime;
import java.util.Scanner;

public class teste_Anti_golpe {
    // Simulação de "arquivo" / lista negra de chaves (array)
    private static final String[] LISTA_NEGRA_DE_CHAVES = {
            "11111111-aaaa-0000-xxxx-000000000001",
            "22222222-bbbb-1111-yyyy-000000000002",
            "fraude@exemplo.com",
            "+5511999998888"
    };
    // Limite de bloqueio (conforme especificação: 70 pontos)
    private static final int LIMITE_BLOQUEIO = 70;
    // Pontuações (constantes para fácil ajuste)
    private static final int PONTUACAO_LISTA_NEGRA = 100;
    private static final int PONTUACAO_DEVOLUCAO_DIFERENTE = 50;
    private static final int PONTUACAO_DESTINATARIO_NOVO = 30;
    private static final int PONTUACAO_VALOR_ELEVADO = 20;
    private static final int PONTUACAO_TIPO_INSEGURO = 40; // ex: chave aleatória
    // Método para verificar se uma chave está na lista negra (simula leitura de arquivo)
    public static boolean estaNaListaNegra(String chave) {
        for (int i = 0; i < LISTA_NEGRA_DE_CHAVES.length; i++) {
            if (LISTA_NEGRA_DE_CHAVES[i].equalsIgnoreCase(chave.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean horarioSuspeito() {
        LocalTime agora = LocalTime.now();
        LocalTime inicioDia = LocalTime.of(6, 0);
        LocalTime fimDia = LocalTime.of(18, 0);
        return agora.isAfter(inicioDia) && agora.isBefore(fimDia);
    }

    // Método para calcular a pontuação de risco com base nos parâmetros informados
    public static int calcularPontuacaoRisco(String chaveOrigem, String chaveDevolucao, String tipoChave, String statusDestinatario, double valorTransacao) {
        int pontuacao = 0;
        // 1) Lista negra
        if (estaNaListaNegra(chaveOrigem)) {
            pontuacao += PONTUACAO_LISTA_NEGRA;
        }
        // 2) Verificação de inconsistência (chave de devolução diferente)
        if (chaveDevolucao != null && !chaveDevolucao.trim().isEmpty()) {
            if (!chaveOrigem.equals(chaveDevolucao)) {
                pontuacao += PONTUACAO_DEVOLUCAO_DIFERENTE;
            }
        }
        // 3) Tipo de chave (exemplo: "ALEATORIA" é considerado inseguro)
        if (tipoChave != null) {
            switch (tipoChave.trim().toUpperCase()) {
                case "ALEATORIA":
                    pontuacao += PONTUACAO_TIPO_INSEGURO;
                    break;
                case "CPF":
                case "CNPJ":
                case "EMAIL":
                case "TELEFONE":
                    // tipos comuns — sem pontuação extra por padrão
                    break;
                default:
                    // tipo desconhecido -> considerar um pouco de risco
                    pontuacao += 10;
                    break;
            }
        }
        // 4) Status do destinatário
        if (statusDestinatario != null) {
            String st = statusDestinatario.trim().toLowerCase();
            if (st.equals("novo") || st.equals("incomum")) {
                pontuacao += PONTUACAO_DESTINATARIO_NOVO;
            }
        }
        // 5) Valor da transação
        if (valorTransacao > 5000.0) {
            pontuacao += PONTUACAO_VALOR_ELEVADO;
        }
        return pontuacao;
    }
    // Mensagem final: decide bloquear ou autorizar
    public static boolean deveBloquear(int pontuacao) {
        return pontuacao >= LIMITE_BLOQUEIO;
    }
    // Método principal com loop do..while para permitir repetição das análises
    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);
        int opcaoContinuar = 1;
        System.out.println("=== Sistema Anti-Golpe PIX (Simulação) ===");
        do {
            System.out.println("\\nInforme os dados do PIX para análise:");
            System.out.print("Chave PIX do destinatário (ex: cpf/email/telefone/aleatoria): ");
            String chave = entrada.nextLine().trim();
            System.out.print("Tipo da chave (CPF/CNPJ/EMAIL/TELEFONE/ALEATORIA): ");
            String tipoChave = entrada.nextLine().trim();
            System.out.print("Chave de devolução (se houver, enter para ignorar): ");
            String chaveDevolucao = entrada.nextLine().trim();
            System.out.print("Status do destinatário (novo/incomum/regular): ");
            String statusDest = entrada.nextLine().trim();
            double valor = 0.0;
            boolean valorValido = false;
            // validação simples do valor usando laço (exemplo de validação com do..while poderia também ser usada)
            do {
                try {
                    System.out.print("Valor da transação (R$): ");
                    String s = entrada.nextLine().trim();
                    valor = Double.parseDouble(s.replace(",", "."));
                    if (valor < 0) {
                        System.out.println("Valor inválido. Informe um valor maior ou igual a zero.");
                    } else {
                        valorValido = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Formato de valor inválido. Tente novamente (ex: 1234.56).");
                }
            } while (!valorValido);

            // calcula pontuação de risco
            int pontuacao = calcularPontuacaoRisco(chave, chaveDevolucao, tipoChave, statusDest, valor);

            // imprime detalhes do cálculo (transparência)
            System.out.println("\\n--- Resultado da Análise ---");
            System.out.println("Pontuação total de risco: " + pontuacao + " pontos");

            if (estaNaListaNegra(chave)) {
                System.out.println("Motivo: chave encontrada na LISTA NEGRA. (+100 pontos)");
            }
            if (!chaveDevolucao.isEmpty() && !chave.equals(chaveDevolucao)) {
                System.out.println("Motivo: chave de devolução diferente da chave informada. (+50 pontos)");
            }
            if (tipoChave != null && tipoChave.equalsIgnoreCase("ALEATORIA")) {
                System.out.println("Motivo: tipo de chave 'ALEATORIA' (considerado mais arriscado). (+40 pontos)");
            }
            if (statusDest != null && (statusDest.equalsIgnoreCase("novo") || statusDest.equalsIgnoreCase("incomum"))) {
                System.out.println("Motivo: destinatário com status 'novo' ou 'incomum'. (+30 pontos)");
            }
            if (valor > 5000.0) {
                System.out.println("Motivo: valor da transação acima de R$ 5.000. (+20 pontos)");
            }
            // decisão final
            if (deveBloquear(pontuacao)) {
                System.out.println("\\n>>> ALERTA GOLPE: Transação BLOQUEADA (pontuação >= " + LIMITE_BLOQUEIO + ")");
            } else {
                System.out.println("\\n>>> Transferência segura: Transação AUTORIZADA (pontuação < " + LIMITE_BLOQUEIO + ")");
            }
            // pergunta se o usuário deseja analisar outra transação (do..while via variável de controle)
            System.out.println("\\nDeseja analisar outra transação? (1 = Sim / 0 = Não): ");
            // ler opção como int protegido
            String resp = entrada.nextLine().trim();
            if (resp.equals("1")) {
                opcaoContinuar = 1;
            } else {
                opcaoContinuar = 0;
            }
        } while (opcaoContinuar == 1);
        System.out.println("Encerrando Sistema Anti-Golpe PIX. Obrigado.");
        entrada.close();
    }
}