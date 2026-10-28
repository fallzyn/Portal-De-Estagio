package model;
public record Vaga(long id, long empresaId, String titulo, String descricao, long areaId, String localizacao, Modalidade modalidade, int cargaHoraria, Situacao situacao) {
}
