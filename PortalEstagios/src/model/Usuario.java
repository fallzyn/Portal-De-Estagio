package model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(length = 150)
    private String curso;

    @Column(name = "nome_empresa", length = 150)
    private String nomeEmpresa;

    @JsonIgnore
    @Column(name = "senha_hash", length = 60)
    private String senhaHash;

    @JsonIgnore
    public String senhaHash() { return senhaHash; }

    public void definirSenhaHash(String hash) {
        if (!security.Senhas.formatoBcryptValido(hash)) {
            throw new IllegalArgumentException("Hash BCrypt inválido.");
        }
        this.senhaHash = hash;
    }

    protected Usuario() {
    }

    public Usuario(long id, String nome, String email, Perfil perfil, String curso, String nomeEmpresa) {
        this.id = id == 0 ? null : id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.curso = curso;
        this.nomeEmpresa = nomeEmpresa;
    }

    public long id() { return id == null ? 0 : id; }
    public String nome() { return nome; }
    public String email() { return email; }
    public Perfil perfil() { return perfil; }
    public String curso() { return curso; }
    public String nomeEmpresa() { return nomeEmpresa; }

    @Override
    public String toString() {
        return "Usuario[id=" + id() + ", nome=" + nome + ", email=" + email
            + ", perfil=" + perfil + ", curso=" + curso + ", nomeEmpresa=" + nomeEmpresa + "]";
    }
}
