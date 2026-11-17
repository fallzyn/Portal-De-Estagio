package service;

import dto.CadastroEntrada;
import dto.UsuarioResposta;
import model.Perfil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.UsuarioRepository;
import security.Senhas;

@Service
public class CadastroService {
    private final PortalService portal;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public CadastroService(PortalService portal, UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.portal = portal;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional
    public UsuarioResposta cadastrar(CadastroEntrada entrada) {
        if (entrada.perfil() != Perfil.ESTUDANTE && entrada.perfil() != Perfil.EMPRESA) {
            throw new IllegalArgumentException("Cadastro público permite apenas estudante ou empresa.");
        }
        if (usuarios.existsByEmail(entrada.email())) {
            throw new RegistroDuplicadoException("E-mail já cadastrado.");
        }
        String hash = Senhas.gerarHash(entrada.senha(), encoder);
        var usuario = portal.cadastrar(entrada.nome(), entrada.email(), entrada.perfil(),
            entrada.curso(), entrada.nomeEmpresa());
        usuario.definirSenhaHash(hash);
        usuarios.saveAndFlush(usuario);
        return new UsuarioResposta(usuario.id(), usuario.nome(), usuario.email(), usuario.perfil());
    }
}
