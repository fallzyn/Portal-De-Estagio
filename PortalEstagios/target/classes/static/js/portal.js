'use strict';
const $ = id => document.getElementById(id);
let conta = null;
let editando = null;
function avisar(texto) { $('mensagem').textContent = texto; }
async function api(url, metodo = 'GET', dados) {
  const headers = {'Accept':'application/json'};
  if (metodo !== 'GET') {
    const resposta = await fetch('/api/auth/csrf', {credentials:'same-origin'});
    if (!resposta.ok) throw new Error('Não foi possível preparar a operação. Recarregue a página.');
    const token = await resposta.json();
    headers[token.headerName] = token.token;
  }
  if (dados !== undefined) headers['Content-Type'] = 'application/json';
  const resposta = await fetch(url, {method:metodo, credentials:'same-origin', headers,
    body:dados === undefined ? undefined : JSON.stringify(dados)});
  if (!resposta.ok) {
    const erro = await resposta.json().catch(() => ({}));
    throw new Error(resposta.status === 401 ? 'Entre novamente para realizar esta operação.' :
      [erro.mensagem || 'Não foi possível realizar a operação.', ...(erro.detalhes || [])].join('\n'));
  }
  return resposta.status === 204 ? null : resposta.json();
}
function texto(tag, conteudo) { const e = document.createElement(tag); e.textContent = conteudo; return e; }
function listaVazia(alvo, itens) {
  alvo.replaceChildren();
  if (!itens.length) alvo.append(texto('p', 'Nenhum registro encontrado.'));
}
function botao(rotulo, acao) {
  const b = texto('button', rotulo); b.type = 'button';
  b.addEventListener('click', async () => {
    b.disabled = true;
    try { await acao(); } catch (e) { avisar(e.message); }
    finally { b.disabled = false; }
  }); return b;
}
function cartao(v) {
  const artigo = document.createElement('article'); artigo.dataset.vagaId = v.id;
  artigo.append(texto('h3', v.titulo), texto('p', v.descricao),
    texto('p', `${v.nomeEmpresa} · ${v.nomeArea} · ${v.localizacao}`),
    texto('p', `${v.modalidade} · ${v.cargaHoraria} h/semana · ${v.situacao}`));
  return artigo;
}
async function abertas() {
  const consulta = new URLSearchParams();
  const form = $('filtros');
  if (form.elements.area.value) consulta.set('area', form.elements.area.value);
  if (form.elements.modalidade.value) consulta.set('modalidade', form.elements.modalidade.value);
  const vagas = await api('/api/vagas?' + consulta);
  listaVazia($('vagas'), vagas);
  for (const v of vagas) {
    const artigo = cartao(v);
    if (conta?.perfil === 'ESTUDANTE') artigo.append(botao('Candidatar-se', async () => {
      await api('/api/estudante/candidaturas', 'POST', {vagaId:v.id});
      avisar('Candidatura registrada.'); await minhas();
    }));
    $('vagas').append(artigo);
  }
}
async function minhas() {
  const itens = await api('/api/estudante/candidaturas');
  listaVazia($('minhas'), itens);
  for (const c of itens) {
    const artigo = cartao(c.vaga);
    artigo.append(texto('p', 'Inscrição: ' + c.dataInscricao.replace('T', ' ') + ' UTC'));
    $('minhas').append(artigo);
  }
}
function cancelarEdicao() {
  editando = null; $('vaga-form').reset(); $('titulo-form').textContent = 'Publicar vaga';
  $('cancelar-edicao').hidden = true;
}
async function vagasEmpresa() {
  const itens = await api('/api/empresa/vagas');
  listaVazia($('minhas-vagas'), itens);
  for (const v of itens) {
    const artigo = cartao(v);
    if (v.situacao === 'ABERTA') {
      artigo.append(botao('Editar', () => {
        editando = v.id; const f = $('vaga-form');
        for (const nome of ['titulo','descricao','areaId','localizacao','modalidade','cargaHoraria']) f.elements[nome].value = v[nome];
        $('titulo-form').textContent = 'Editar vaga'; $('cancelar-edicao').hidden = false;
        f.elements.titulo.focus();
      }), botao('Encerrar', async () => {
        if (!window.confirm('Encerrar esta vaga? As candidaturas existentes serão preservadas.')) return;
        await api(`/api/empresa/vagas/${v.id}/encerramento`, 'PATCH');
        if (editando === v.id) cancelarEdicao();
        avisar('Vaga encerrada.'); await vagasEmpresa(); await abertas();
      }));
    }
    artigo.append(botao('Ver candidatos', async () => {
      const candidatos = await api(`/api/empresa/vagas/${v.id}/candidatos`);
      $('vaga-selecionada').textContent = v.titulo;
      listaVazia($('candidatos'), candidatos);
      for (const c of candidatos) $('candidatos').append(texto('p', `${c.nome} · ${c.email} · ${c.curso}`));
    }));
    $('minhas-vagas').append(artigo);
  }
}
$('filtros').addEventListener('submit', async e => { e.preventDefault(); try { await abertas(); } catch(e) { avisar(e.message); } });
$('atualizar-minhas').addEventListener('click', () => minhas().catch(e => avisar(e.message)));
$('atualizar-empresa').addEventListener('click', () => vagasEmpresa().catch(e => avisar(e.message)));
$('cancelar-edicao').addEventListener('click', cancelarEdicao);
$('vaga-form').addEventListener('submit', async e => {
  e.preventDefault(); const form = e.currentTarget, b = form.querySelector('[type=submit]'); b.disabled = true;
  try {
    const dados = Object.fromEntries(new FormData(form));
    dados.areaId = Number(dados.areaId); dados.cargaHoraria = Number(dados.cargaHoraria);
    await api('/api/empresa/vagas' + (editando === null ? '' : '/' + editando), editando === null ? 'POST' : 'PUT', dados);
    cancelarEdicao(); avisar('Vaga salva.'); await vagasEmpresa(); await abertas();
  } catch(e) { avisar(e.message); } finally { b.disabled = false; }
});
(async () => {
  try {
    const identidade = await fetch('/api/auth/eu', {credentials:'same-origin'});
    if (identidade.ok) conta = await identidade.json();
    else if (identidade.status !== 401) throw new Error('Não foi possível consultar sua sessão.');
    if (conta) $('usuario').textContent = `${conta.nome} (${conta.perfil})`;
    const areas = await api('/api/areas');
    for (const a of areas) {
      for (const seletor of [$('filtros').elements.area, $('vaga-form').elements.areaId]) {
        const opcao = texto('option', a.nome); opcao.value = a.id; seletor.append(opcao);
      }
    }
    await abertas();
    if (conta?.perfil === 'ESTUDANTE') { $('estudante').hidden = false; await minhas(); }
    if (conta?.perfil === 'EMPRESA') { $('empresa').hidden = false; await vagasEmpresa(); }
  } catch(e) { avisar(e.message); }
})();
