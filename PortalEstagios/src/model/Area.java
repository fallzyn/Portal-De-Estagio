package model;

import jakarta.persistence.*;

@Entity
@Table(name = "areas")
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    protected Area() {
    }

    public Area(long id, String nome) {
        this.id = id == 0 ? null : id;
        this.nome = nome;
    }

    public long id() { return id == null ? 0 : id; }
    public String nome() { return nome; }

    public void editar(String nome) { this.nome = nome; }

    @Override
    public String toString() {
        return "Area[id=" + id() + ", nome=" + nome + "]";
    }
}