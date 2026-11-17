import model.*;
import org.junit.jupiter.api.Test;
import repository.UsuarioRepository;
import security.Senhas;
import service.InicializacaoAdminService;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InicializacaoAdminTest {
    @Test void criaAdminComHashSemSenhaPadrao() {
        var repo=mock(UsuarioRepository.class);var encoder=Senhas.encoder();
        when(repo.findByEmail("admin@exemplo.com")).thenReturn(Optional.empty());
        new InicializacaoAdminService(repo,encoder).inicializar("admin@exemplo.com","SomenteTeste#123");
        verify(repo).saveAndFlush(argThat(u -> u.perfil()==Perfil.ADMINISTRADOR && encoder.matches("SomenteTeste#123",u.senhaHash())));
    }
    @Test void naoPromoveEstudante() {
        var repo=mock(UsuarioRepository.class);
        when(repo.findByEmail("aluno@exemplo.com")).thenReturn(Optional.of(new Usuario(1,"Aluno","aluno@exemplo.com",Perfil.ESTUDANTE,"SI",null)));
        assertThrows(IllegalArgumentException.class,()->new InicializacaoAdminService(repo,Senhas.encoder()).inicializar("aluno@exemplo.com","SomenteTeste#123"));
        verify(repo,never()).saveAndFlush(any());
    }
    @Test void reexecucaoNaoRedefineSenha() {
        var repo=mock(UsuarioRepository.class);var admin=new Usuario(1,"Admin","admin@exemplo.com",Perfil.ADMINISTRADOR,null,null);
        admin.definirSenhaHash(Senhas.encoder().encode("SenhaOriginal#123"));String antes=admin.senhaHash();
        when(repo.findByEmail("admin@exemplo.com")).thenReturn(Optional.of(admin));
        new InicializacaoAdminService(repo,Senhas.encoder()).inicializar("admin@exemplo.com","OutraSenha#123");
        assertEquals(antes,admin.senhaHash());verify(repo,never()).saveAndFlush(any());
    }
    @Test void emailOuSenhaInvalidaNaoGravam() {
        var repo=mock(UsuarioRepository.class);var servico=new InicializacaoAdminService(repo,Senhas.encoder());
        assertThrows(IllegalArgumentException.class,()->servico.inicializar("invalido","SomenteTeste#123"));
        when(repo.findByEmail("admin@exemplo.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,()->servico.inicializar("admin@exemplo.com","curta"));
        verify(repo,never()).saveAndFlush(any());
    }
}
