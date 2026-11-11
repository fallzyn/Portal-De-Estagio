package model;

import jakarta.persistence.*;

@Entity
@Table(name = "vagas")
public class Vaga {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Usuario empresa;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(nullable = false, length = 150)
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidade modalidade;

    @Column(name = "carga_horaria", nullable = false)
    private int cargaHoraria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Situacao situacao;

    protected Vaga() {
    }

    public Vaga(long id, Usuario empresa, String titulo, String descricao, Area area,
                String localizacao, Modalidade modalidade, int cargaHoraria, Situacao situacao) {
        this.id = id == 0 ? null : id;
        this.empresa = empresa;
        this.titulo = titulo;
        this.descricao = descricao;
        this.area = area;
        this.localizacao = localizacao;
        this.modalidade = modalidade;
        this.cargaHoraria = cargaHoraria;
        this.situacao = situacao;
    }

    public long id() { return id == null ? 0 : id; }
    public long empresaId() { return empresa.id(); }
    public Usuario empresa() { return empresa; }
    public String titulo() { return titulo; }
    public String descricao() { return descricao; }
    public long areaId() { return area.id(); }
    public Area area() { return area; }
    public String localizacao() { return localizacao; }
    public Modalidade modalidade() { return modalidade; }
    public int cargaHoraria() { return cargaHoraria; }
    public Situacao situacao() { return situacao; }

    public void encerrar() { situacao = Situacao.ENCERRADA; }

    public void editar(String titulo, String descricao, Area area, String localizacao,
                       Modalidade modalidade, int cargaHoraria) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.area = area;
        this.localizacao = localizacao;
        this.modalidade = modalidade;
        this.cargaHoraria = cargaHoraria;
    }

    @Override
    public String toString() {
        return "Vaga[id=" + id() + ", empresaId=" + empresaId() + ", titulo=" + titulo
            + ", descricao=" + descricao + ", areaId=" + areaId() + ", localizacao=" + localizacao
            + ", modalidade=" + modalidade + ", cargaHoraria=" + cargaHoraria + ", situacao=" + situacao + "]";
    }
}