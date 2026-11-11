package service;

import java.util.List;
import model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.*;

@Service
@Transactional
public class PortalService {
    private final UsuarioRepository usuarios;
    private final AreaRepository areas;
    private final VagaRepository vagas;
    private final CandidaturaRepository candidaturas;

    public PortalService(UsuarioRepository usuarios, AreaRepository areas,
                         VagaRepository vagas, CandidaturaRepository candidaturas) {
        this.usuarios = usuarios;
        this.areas = areas;
        this.vagas = vagas;
        this.candidaturas = candidaturas;
    }

    private static void texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
    }

    private Usuario exigir(long id, Perfil perfil) {
        Usuario usuario = usuarios.findById(id).orElse(null);
        if (usuario == null || usuario.perfil() != perfil) {
            throw new IllegalArgumentException("Operação não permitida para este perfil.");
        }
        return usuario;
    }

    private Area area(long id) {
        return areas.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Área inexistente."));
    }

    private Vaga vaga(long id, boolean bloquear) {
        return (bloquear ? vagas.bloquearPorId(id) : vagas.findById(id))
            .orElseThrow(() -> new IllegalArgumentException("Vaga inexistente."));
    }

    private Vaga propria(long empresa, long id, boolean bloquear) {
        exigir(empresa, Perfil.EMPRESA);
        Vaga vaga = vaga(id, bloquear);
        if (vaga.empresaId() != empresa) {
            throw new IllegalArgumentException("A vaga pertence a outra empresa.");
        }
        return vaga;
    }

    public Usuario cadastrar(String nome, String email, Perfil perfil, String curso, String empresa) {
        if (perfil == null) throw new IllegalArgumentException("Perfil é obrigatório.");
        texto(nome, "Nome");
        texto(email, "E-mail");
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        if (perfil == Perfil.ADMINISTRADOR) {
            throw new IllegalArgumentException("Cadastro público de administrador não permitido.");
        }
        if (perfil == Perfil.ESTUDANTE) texto(curso, "Curso");
        else texto(empresa, "Nome da empresa");
        if (usuarios.existsByEmail(email)) throw new IllegalArgumentException("E-mail já cadastrado.");
        return usuarios.saveAndFlush(new Usuario(0, nome, email, perfil,
            perfil == Perfil.ESTUDANTE ? curso : null,
            perfil == Perfil.EMPRESA ? empresa : null));
    }

    @Transactional(readOnly = true)
    public List<Usuario> usuarios() {
        return usuarios.findAllByOrderByIdAsc();
    }

    public Area criarArea(long admin, String nome) {
        exigir(admin, Perfil.ADMINISTRADOR);
        texto(nome, "Nome da área");
        if (areas.existsByNome(nome)) throw new IllegalArgumentException("Área já cadastrada.");
        return areas.saveAndFlush(new Area(0, nome));
    }

    public void editarArea(long admin, long id, String nome) {
        exigir(admin, Perfil.ADMINISTRADOR);
        texto(nome, "Nome da área");
        Area atual = area(id);
        if (areas.existsByNomeAndIdNot(nome, id)) {
            throw new IllegalArgumentException("Área já cadastrada.");
        }
        atual.editar(nome);
        areas.saveAndFlush(atual);
    }

    public void excluirArea(long admin, long id) {
        exigir(admin, Perfil.ADMINISTRADOR);
        Area atual = area(id);
        if (vagas.existsByArea_Id(id)) {
            throw new IllegalArgumentException("Área utilizada por uma vaga não pode ser excluída.");
        }
        areas.delete(atual);
        areas.flush();
    }

    @Transactional(readOnly = true)
    public List<Area> areas() {
        return areas.findAllByOrderByNomeAsc();
    }

    private Area validarVaga(String titulo, String descricao, long idArea,
                             String local, Modalidade modalidade, int carga) {
        texto(titulo, "Título");
        texto(descricao, "Descrição");
        texto(local, "Localização");
        if (modalidade == null) throw new IllegalArgumentException("Modalidade obrigatória.");
        if (carga <= 0) throw new IllegalArgumentException("Carga horária deve ser positiva.");
        return area(idArea);
    }

    public Vaga publicar(long empresa, String titulo, String descricao, long idArea,
                         String local, Modalidade modalidade, int carga) {
        Usuario proprietaria = exigir(empresa, Perfil.EMPRESA);
        Area area = validarVaga(titulo, descricao, idArea, local, modalidade, carga);
        return vagas.saveAndFlush(new Vaga(0, proprietaria, titulo, descricao, area,
            local, modalidade, carga, Situacao.ABERTA));
    }

    public void editarVaga(long empresa, long id, String titulo, String descricao, long idArea,
                           String local, Modalidade modalidade, int carga) {
        Vaga atual = propria(empresa, id, true);
        if (atual.situacao() == Situacao.ENCERRADA) {
            throw new IllegalArgumentException("Vaga encerrada não pode ser editada.");
        }
        Area area = validarVaga(titulo, descricao, idArea, local, modalidade, carga);
        atual.editar(titulo, descricao, area, local, modalidade, carga);
        vagas.saveAndFlush(atual);
    }

    public void encerrar(long empresa, long id) {
        Vaga atual = propria(empresa, id, true);
        if (atual.situacao() == Situacao.ENCERRADA) {
            throw new IllegalArgumentException("Vaga já encerrada.");
        }
        atual.encerrar();
        vagas.saveAndFlush(atual);
    }

    @Transactional(readOnly = true)
    public List<Vaga> abertas(Long area, Modalidade modalidade) {
        return vagas.listarAbertas(area, modalidade).stream()
            .filter(vaga -> vaga.situacao() == Situacao.ABERTA).toList();
    }

    @Transactional(readOnly = true)
    public List<Vaga> vagasEmpresa(long empresa) {
        exigir(empresa, Perfil.EMPRESA);
        return vagas.findByEmpresa_IdOrderByIdAsc(empresa);
    }

    public Candidatura candidatar(long estudante, long idVaga) {
        Usuario aluno = exigir(estudante, Perfil.ESTUDANTE);
        // Encerramento, edição e candidatura disputam o mesmo bloqueio de linha.
        // A verificação da situação e a inscrição ficam na mesma transação.
        Vaga vaga = vaga(idVaga, true);
        if (vaga.situacao() != Situacao.ABERTA) {
            throw new IllegalArgumentException("Vaga encerrada.");
        }
        if (candidaturas.existsByEstudante_IdAndVaga_Id(estudante, idVaga)) {
            throw new IllegalArgumentException("Candidatura já existente.");
        }
        return candidaturas.saveAndFlush(new Candidatura(aluno, vaga));
    }

    @Transactional(readOnly = true)
    public List<Candidatura> minhas(long estudante) {
        exigir(estudante, Perfil.ESTUDANTE);
        return candidaturas.findByEstudante_IdOrderByDataInscricaoDescIdDesc(estudante);
    }

    @Transactional(readOnly = true)
    public List<Usuario> candidatos(long empresa, long idVaga) {
        propria(empresa, idVaga, false);
        return candidaturas.findByVaga_IdOrderByDataInscricaoAscIdAsc(idVaga).stream()
            .map(Candidatura::estudante).toList();
    }

    @Transactional(readOnly = true)
    public Contagens contagens(long admin) {
        exigir(admin, Perfil.ADMINISTRADOR);
        return new Contagens(usuarios.countByPerfil(Perfil.ESTUDANTE),
            usuarios.countByPerfil(Perfil.EMPRESA), vagas.countBySituacao(Situacao.ABERTA),
            vagas.countBySituacao(Situacao.ENCERRADA), candidaturas.count());
    }
}
