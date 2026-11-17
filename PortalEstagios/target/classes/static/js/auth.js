'use strict';
const mensagem = document.getElementById('mensagem');
function avisar(texto) { mensagem.textContent = texto; }
async function csrf() {
  const resposta = await fetch('/api/auth/csrf', {credentials:'same-origin'});
  if (!resposta.ok) throw new Error('Não foi possível preparar a operação. Recarregue a página.');
  return resposta.json();
}
const cadastro = document.getElementById('cadastro');
if (cadastro) {
  function atualizarPerfil() {
    const estudante = cadastro.elements.perfil.value === 'ESTUDANTE';
    document.getElementById('campo-curso').hidden = !estudante;
    document.getElementById('campo-empresa').hidden = estudante;
    cadastro.elements.curso.disabled = !estudante;
    cadastro.elements.curso.required = estudante;
    cadastro.elements.nomeEmpresa.disabled = estudante;
    cadastro.elements.nomeEmpresa.required = !estudante;
  }
  cadastro.elements.perfil.addEventListener('change', atualizarPerfil);
  atualizarPerfil();
  cadastro.addEventListener('submit', async evento => {
    evento.preventDefault();
    const botao = cadastro.querySelector('button');
    botao.disabled = true;
    avisar('Cadastrando…');
    try {
      const token = await csrf();
      const valores = new FormData(cadastro);
      const dados = Object.fromEntries(valores);
      const resposta = await fetch('/api/auth/cadastro', {
        method:'POST', credentials:'same-origin',
        headers:{'Content-Type':'application/json', [token.headerName]:token.token},
        body:JSON.stringify(dados)
      });
      if (!resposta.ok) {
        const erro = await resposta.json().catch(() => ({}));
        throw new Error([erro.mensagem || 'Não foi possível cadastrar.', ...(erro.detalhes || [])].join('\n'));
      }
      cadastro.reset();
      atualizarPerfil();
      avisar('Cadastro criado. Clique em “Já tenho conta — entrar” para fazer login com seu e-mail e senha.');
    } catch (erro) { avisar(erro.message); }
    finally { botao.disabled = false; }
  });
}
if (document.getElementById('identidade')) {
  (async () => {
    try {
      const resposta = await fetch('/api/auth/eu', {credentials:'same-origin'});
      if (resposta.status === 401) { window.location.replace('/login'); return; }
      if (!resposta.ok) throw new Error('Não foi possível carregar sua conta.');
      const conta = await resposta.json();
      document.getElementById('identidade').textContent = `${conta.nome} — ${conta.email} (${conta.perfil})`;
      const token = await csrf();
      const form = document.getElementById('sair');
      form.querySelector('input').name = token.parameterName;
      form.querySelector('input').value = token.token;
      form.querySelector('button').disabled = false;
      const consulta = await fetch('/api/areas', {credentials:'same-origin'});
      if (!consulta.ok) throw new Error('Não foi possível consultar as áreas.');
      for (const area of await consulta.json()) {
        const item = document.createElement('li'); item.textContent = area.nome;
        document.getElementById('areas').append(item);
      }
      if (conta.perfil === 'ADMINISTRADOR') {
        const respostaContagens = await fetch('/api/admin/contagens', {credentials:'same-origin'});
        if (!respostaContagens.ok) throw new Error('Não foi possível consultar as contagens.');
        const nomes = {estudantes:'Estudantes',empresas:'Empresas',vagasAbertas:'Vagas abertas',vagasEncerradas:'Vagas encerradas',candidaturas:'Candidaturas'};
        for (const [chave, valor] of Object.entries(await respostaContagens.json())) {
          const titulo = document.createElement('dt'), numero = document.createElement('dd');
          titulo.textContent = nomes[chave] || chave; numero.textContent = valor;
          document.getElementById('contagens').append(titulo, numero);
        }
        document.getElementById('administracao').hidden = false;
      }
    } catch (erro) { avisar(erro.message); }
  })();
}

const login = document.getElementById('login');
if (login) {
  const parametros = new URLSearchParams(window.location.search);
  if (parametros.has('error')) avisar('E-mail ou senha inválidos.');
  else if (parametros.has('logout')) avisar('Você saiu da conta.');
  csrf().then(token => {
    const campo = login.querySelector('input[type=hidden]');
    campo.name = token.parameterName; campo.value = token.token;
    login.querySelector('button').disabled = false;
  }).catch(erro => avisar(erro.message));
}
