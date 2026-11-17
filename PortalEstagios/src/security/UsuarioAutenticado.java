package security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import model.Perfil;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioAutenticado implements UserDetails, CredentialsContainer {
    private final long id;
    private final String nome;
    private final String email;
    private final Perfil perfil;
    private final boolean habilitado;
    @JsonIgnore
    private String hash;

    public UsuarioAutenticado(long id, String nome, String email, Perfil perfil, String hash) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.hash = hash;
        this.habilitado = Senhas.formatoBcryptValido(hash);
    }

    public long id() { return id; }
    public String nome() { return nome; }
    public Perfil perfil() { return perfil; }
    @Override public String getUsername() { return email; }
    @JsonIgnore
    @Override public String getPassword() { return hash; }
    @Override public boolean isEnabled() { return habilitado; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }
    @Override public void eraseCredentials() { hash = null; }
    @Override public String toString() { return "UsuarioAutenticado[id=" + id + "]"; }
}
