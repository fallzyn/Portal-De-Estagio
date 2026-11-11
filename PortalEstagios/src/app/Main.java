package app;
import model.*;
import service.PortalService;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.*;
public class Main {
    private static final Scanner sc=new Scanner(System.in);
    private static PortalService svc;
    private static ConfigurableApplicationContext contexto;

    // O contexto é iniciado só quando uma ação realmente precisa do banco.
    private static PortalService servico() {
        if (svc == null) {
            SpringApplication aplicacao = new SpringApplication(PortalApplication.class);
            aplicacao.setWebApplicationType(WebApplicationType.NONE);
            aplicacao.setBannerMode(Banner.Mode.OFF);
            aplicacao.setLogStartupInfo(false);
            contexto = aplicacao.run();
            svc = contexto.getBean(PortalService.class);
        }
        return svc;
    }
    private static Usuario atual;
    private static String ler(String msg){
        System.out.print(msg+": ");
        return sc.nextLine().trim();
    }
    private static long numero(String msg){
        while (true) {
            try {
                return Long.parseLong(ler(msg));
            } catch (NumberFormatException e) {
            System.out.println("Número inválido. Tente novamente.");
        }
    }

    }
    private static int inteiro(String msg){
        while (true) {
            try {
            return Integer.parseInt(ler(msg));
        } catch (NumberFormatException e) {
            System.out.println("Número inválido. Tente novamente.");
        }
    }
    }
    private static <E extends Enum<E>> E opcao(String msg,Class<E> tipo){
        String valor=ler(msg+" "+Arrays.toString(tipo.getEnumConstants())).toUpperCase(Locale.ROOT);
        try {
            return Enum.valueOf(tipo,valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Opção inválida. Use "+Arrays.toString(tipo.getEnumConstants())+".");
        }
    }
    private static void executar(Acao r){
        try{
            r.run();
        }
        catch(NumberFormatException e){
            System.out.println("Número inválido.");
        }
        catch(IllegalArgumentException e){
            System.out.println("Erro: "+e.getMessage());
        }
        catch (DataIntegrityViolationException e) {
            System.out.println("Operação rejeitada: registro duplicado ou ainda utilizado por outros dados.");
        }
        catch (DataAccessException e) {
            System.out.println("Falha no banco: não foi possível concluir a operação.");
        }
        catch (org.springframework.beans.BeansException | IllegalStateException e) {
            System.out.println("Falha no banco: confira DB_URL, DB_USER, DB_PASSWORD e o esquema existente.");
        }
    }
    @FunctionalInterface private interface Acao{
        void run();
    }
    public static void main(String[] args){
        try {
            while(true){
                System.out.println("\nPORTAL DE ESTÁGIOS | Usuário: "+(atual==null?"nenhum":atual.nome()+" ("+atual.perfil()+")"));
                System.out.println("""
                    | 1 Cadastrar estudante
                    | 2 Cadastrar empresa
                    | 3 Selecionar usuário
                    | 4 Consultar vagas abertas
                    | 5 Listar áreas
                    | 6 Ações do perfil
                    | 0 Sair
                    """);
                String op=ler("Selecione sua opção");
                if(op.equals("0"))return;
                executar(()->{
                    switch(op){
                        case "1"->cadastrar(Perfil.ESTUDANTE);
                        case "2"->cadastrar(Perfil.EMPRESA);
                        case "3"->selecionar();
                        case "4"->consultarVagas();
                        case "5"->servico().areas().forEach(System.out::println);
                        case "6"->acoes();
                        default->System.out.println("Opção inválida!");
                    }
                });
            }
        } catch (NoSuchElementException e) {
            // Fim da entrada também pode acontecer no meio de uma operação.
            System.out.println("\nEntrada encerrada.");
        } finally {
            if (contexto != null) contexto.close();
        }
    }
    private static void cadastrar(Perfil perfil){
        String nome=ler("Nome"),email=ler("E-mail");
        String extra=ler(perfil==Perfil.ESTUDANTE?"Curso":"Nome da empresa");
        System.out.println("Cadastrado: "+servico().cadastrar(nome,email,perfil,perfil==Perfil.ESTUDANTE?extra:null,perfil==Perfil.EMPRESA?extra:null));
    }
    private static void selecionar(){
        servico().usuarios().forEach(System.out::println);
        long id=numero("ID do usuário");
        atual=servico().usuarios().stream().filter(u->u.id()==id).findFirst().orElseThrow(()->new IllegalArgumentException("Usuário inexistente."));
    }
    private static void consultarVagas(){
        String a=ler("ID da área (Enter para todas)");
        Long area=a.isEmpty()?null:Long.valueOf(a);
        String m=ler("Modalidade PRESENCIAL/REMOTA/HIBRIDA (Enter para todas)");
        Modalidade mod;
        try {
            mod=m.isEmpty()?null:Modalidade.valueOf(m.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Modalidade inválida. Use PRESENCIAL, REMOTA ou HIBRIDA.");
        }
        servico().abertas(area,mod).forEach(System.out::println);
    }
    private static void acoes(){
        if(atual==null)throw new IllegalArgumentException("Selecione um usuário primeiro.");
        switch(atual.perfil()){
            case ESTUDANTE->estudante();
            case EMPRESA->empresa();
            case ADMINISTRADOR->admin();
        }
    }
    private static void estudante()  {

    System.out.println(
        "1 Candidatar-se | 2 Minhas candidaturas | 0 Voltar"
    );

    while (true) {
        switch (ler("Opção")) {

            case "1" -> System.out.println(
                servico().candidatar(
                    atual.id(),
                    numero("ID da vaga")
                )
            );

            case "2" -> servico().minhas(atual.id())
                .forEach(System.out::println);

            case "0" -> {
                return;
            }
            default -> {
                System.out.println(
                    "Opção inválida. Tente novamente."
                );
                continue;
            }
        }
        return;
    }
}
    private static void empresa(){
        System.out.println("""
            | 1 Publicar 
            | 2 Minhas vagas 
            | 3 Editar vaga 
            | 4 Encerrar vaga 
            | 5 Candidatos de uma vaga
            """);
        switch(ler("Opção")){
            case "1"->System.out.println(servico().publicar(atual.id(),ler("Título"),ler("Descrição"),numero("ID da área"),ler("Localização"),opcao("Modalidade",Modalidade.class),inteiro("Carga horária semanal")));
            case "2"->servico().vagasEmpresa(atual.id()).forEach(System.out::println);
            case "3"->{
                long id=numero("ID da vaga");
                servico().editarVaga(atual.id(),id,ler("Título"),ler("Descrição"),numero("ID da área"),ler("Localização"),opcao("Modalidade",Modalidade.class),inteiro("Carga horária semanal"));
                System.out.println("Vaga editada.");
            }
            case "4"->{
                servico().encerrar(atual.id(),numero("ID da vaga"));
                System.out.println("Vaga encerrada.");
            }
            case "5"->servico().candidatos(atual.id(),numero("ID da vaga")).forEach(System.out::println);
            default->System.out.println("Opção inválida.");
        }
    }
    private static void admin(){
        System.out.println("""
            | 1 Cadastrar área 
            | 2 Listar áreas 
            | 3 Editar área 
            | 4 Excluir área 
            | 5 Contagens
            """);
        switch(ler("Opção")){
            case "1"->System.out.println(servico().criarArea(atual.id(),ler("Nome")));
            case "2"->servico().areas().forEach(System.out::println);
            case "3"->{
                servico().editarArea(atual.id(),numero("ID da área"),ler("Novo nome"));
                System.out.println("Área editada.");
            }
            case "4"->{
                servico().excluirArea(atual.id(),numero("ID da área"));
                System.out.println("Área excluída.");
            }
            case "5"->System.out.println(servico().contagens(atual.id()));
            default->System.out.println("Opção inválida.");
        }
    }
}
