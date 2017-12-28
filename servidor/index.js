/**
 * ROTAS:
 * 
 * - Obter todos os jogadores(GET /jogador/)
 * - Se registrar como jogador(POST /jogador/nome)
 * 
 * - Ouvir por convite de partida(GET /jogador/nomeJogador/partida/ouvir)
 * - Iniciar partida com jogador(POST /partida/nomeOponente{nome: nomeJogador})
 * - Atualizar partida(PUT /partida/id/)
 * - Obter informações sobre partida(GET /partida/id)
 * - Ouvir por confirmação/mudança de partida(GET /partida/id/ouvir)
 * 
 * 
 * - Ouvir por disparo(GET /disparo/nomeJogador)
 * - Disparar(POST /disparo/nomeOponente)
 * 
 * - Obter tabuleiro de jogador(GET /jogador/:idJogador/tabuleiro OU /tabuleiro/:idTabuleiro)
 */


const 
	express = require('express'),
	morgan = require('morgan'),
	bodyParser = require('body-parser'),
	{EventEmitter} = require('events'),
	Tabuleiro = require('./Tabuleiro'),
	eventBus = new EventEmitter();


const 
	app = express(),
	porta = process.env.PORT || 8888,
	STATUS_PARTIDA = {
		INICIADA: 0,
		ACEITA: 1,
		RECUSADA: 2
	};

eventBus.setMaxListeners(100);

let jogadores = [
	{
		nome: 'Fulano',
		tabuleiro: null,
		tabuleiroOponente: Array(10).fill([]).map(linha => Array(10).fill(false))
	},
	{
		nome: 'Ciclano',
		tabuleiro: null,
		tabuleiroOponente: Array(10).fill([]).map(linha => Array(10).fill(false))
	},
	{
		nome: 'Beltrano',
		tabuleiro: null,
		tabuleiroOponente: Array(10).fill([]).map(linha => Array(10).fill(false))
	}
];

let partidas = {};
let nPartidas = 0;

app.use(function (req, res, next) {
	res.header("Access-Control-Allow-Origin", "*");
	res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
	res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
	next();
});

app.use(bodyParser.json());
app.use(morgan('combined'));

app.get('/reset',(req,res) => {
	partidas = {};
	jogadores = [];

	res.json({sucesso: true});
});

app.get('/jogador',(req,res) => res.json(jogadores));

app.get('/jogador/:nomeJogador',(req,res) => {
	const {nomeJogador} = req.params;

	if (typeof(nomeJogador) === 'undefined')
		res.status(400).json({erro: 'Nome do usuário ausente'});


	const jogador = jogadores.find(jogador => (jogador.nome === nomeJogador));

	if (!jogador)//jogador não existe
		res.status(404).json({erro: 'Jogador não encontrado'});
	else res.send(jogador); 
});

app.post('/jogador/:nomeJogador',(req,res) => {
	const {nomeJogador} = req.params;

	if (typeof (nomeJogador) === 'undefined')
		res.status(400).json({ erro: 'Nome do usuário ausente' });		


	const jogador = jogadores.find(jogador => jogador.nome === nomeJogador);
	if (typeof(jogador) === 'undefined'){
		jogadores.push({
			nome: nomeJogador,
			tabuleiro: null,
			tabuleiroOponente: Array(10).fill([]).map(linha => Array(10).fill(false))
		});
		res.json({mensagem: 'Jogador cadastrado'});
	}
	else res.status(404).json({erro: 'Nome de jogador já existe'});
});

app.delete('/jogador/:nomeJogador',(req,res) => {
	const { nomeJogador } = req.params;

	if (typeof (nomeJogador) === 'undefined')
		return res.status(400).json({ erro: 'Nome do usuário ausente' });

	const jogador = jogadores.find(jogador => jogador.nome === nomeJogador);	
	if (jogador){ //nomeJogador existe
		jogadores.splice(indiceJogador,1);
		res.json({ mensagem: 'Jogador removido' });
	}
	else res.status(404).json({ erro: 'Jogador não encontrado' });
});

app.get('/jogador/:nomeJogador/partida/',(req,res) => {
	const 
		{nomeJogador} = req.params,
		idPartida = Object.keys(partidas).find(chave_partida => {
			const partida = partidas[chave_partida];
			return partida.nomeJogadorSolicitante === nomeJogador || partida.nomeOponente === nomeJogador;
		}),
		partida = partidas[idPartida];

		if (!idPartida || !partida)
			return res.status(404).json({ erro: 'Partida não existe' });

		return res.json(partida);
});

app.get('/jogador/:nomeJogador/partida/ouvir',(req,res) => {
	const {nomeJogador} = req.params;
	
	if (typeof (nomeJogador) === 'undefined')
		return res.status(400).json({ erro: 'Nome do jogador solicitante ausente' });


	const jogador = jogadores.find(jogador => jogador.nome === nomeJogador);

	if (!jogador) //nomeJogador não existe
		return res.status(404).json({ erro: 'Jogador não encontrado' });

	eventBus.once('mudancaJogador-' + nomeJogador,
		partida => res.json(partida)
	);
	
});


app.get('/jogador/:nomeJogador/disparo/ouvir',(req,res) => {
	const 
		{nomeJogador} = req.params,
		jogador = jogadores.find(jogador => (jogador.nome === nomeJogador));

	if (!jogador)//jogador não existe
		return res.status(404).json({ erro: 'Jogador não encontrado' });
		

	console.log('event once - ' + 'disparo-' + nomeJogador);
	eventBus.once('disparo-' + nomeJogador,disparo => res.json(disparo));
});

app.post('/jogador/:nomeJogador/disparo',(req,res) => {
	const 
		{nomeJogador} = req.params,
		{x,y} = disparo = req.body;

	const 
		jogador = jogadores.find(jogador => (jogador.nome === nomeJogador)),
		idPartida = Object.keys(partidas).find(chave_partida => {
			const partida = partidas[chave_partida];
			return partida.nomeJogadorSolicitante === nomeJogador || partida.nomeOponente === nomeJogador;	
		}),
		partida = partidas[idPartida];

	if (!jogador)
		return res.status(404).json({ erro: 'Jogador não encontrado' });

	if (!partida)
		return	res.status(404).json({ erro: 'Jogador não se encontra em nenhuma partida' });


	console.log('jogador',jogador);
	console.log('partida',partida);

	const 
		{nomeOponente,nomeJogadorSolicitante} = partida,
		isJogadorSolicitante = nomeJogador === partida.nomeJogadorSolicitante,
		nomeOutroJogador = isJogadorSolicitante ? nomeOponente : nomeJogadorSolicitante,
		outroJogador = jogadores.find(jogador => (jogador.nome === nomeOutroJogador));

	console.log('nomeOponente', nomeOponente);
	console.log('nomeJogadorSolicitante', nomeJogadorSolicitante);
	console.log('nomeOutroJogador', nomeOutroJogador);
	console.log('outroJogador', outroJogador);
	

	try{	

		const
			jaAtirouCasa = jogador.tabuleiroOponente[y][x],
			acertou = outroJogador.tabuleiro[y][x];


		if (jaAtirouCasa)
			return res.status(400).json({ erro: 'Casa já utilizada para disparo' });


		console.log('event emit - ' + 'disparo-' + nomeOutroJogador);

		eventBus.emit('disparo-' + nomeOutroJogador,disparo);
		
		return res.json({acertou});
	}catch(e){
		res.status(400).json({erro: 'Erro no disparo!'});
	}

});

app.get('/partida/:idPartida', (req, res) => {
	const 
		{idPartida} = req.params,
		partida = partidas[idPartida];

	if (typeof (partida) === 'undefined')
		return res.status(404).json({ erro: 'Partida não existe' });

	res.json(partida);

});


app.get('/partida/:idPartida/ouvir/', (req, res) => {

	const { idPartida } = req.params;

	if (typeof (idPartida) === 'undefined')
		return res.status(400).json({ erro: 'ID da partida ausente' });

	const partida = partidas[idPartida];

	if (typeof (partida) === 'undefined')
		return res.status(404).json({ erro: 'Partida não existe' });

	eventBus.once('mudancaPartida-' + idPartida,
		partida => res.json(partida)
	);
});

app.post('/partida/:nomeOponente', (req, res) => {
	const
		{ nomeJogador } = req.body,
		{ nomeOponente } = req.params;

	if (typeof (nomeJogador) === 'undefined')
		return res.status(400).json({ erro: 'Nome do jogador solicitante ausente' });

	if (typeof (nomeOponente) === 'undefined')
		return res.status(400).json({ erro: 'Nome do jogador oponente ausente' });


	const
		jogadorSolicitante = jogadores.find(jogador => jogador.nome === nomeJogador),
		jogadorOponente = jogadores.find(jogador => jogador.nome === nomeOponente);

	if (!jogadorSolicitante) //nomeJogador não existe
		return res.status(404).json({ erro: 'Jogador solicitante não encontrado' });

	if (!jogadorOponente) //nomeOponente não existe
		return res.status(404).json({ erro: 'Jogador oponente não encontrado' });

	const novaPartida = {
		id: ++nPartidas,
		status: STATUS_PARTIDA.INICIADA,
		nomeJogadorSolicitante: nomeJogador,
		nomeOponente
	}

	partidas[novaPartida.id] = novaPartida;

	eventBus.emit('mudancaJogador-' + novaPartida.nomeOponente, novaPartida);

	res.json(novaPartida);

});

app.delete('/partida/:idPartida',(req,res) => {
	const 
		{idPartida} = req.params,
		partida = partidas[idPartida];

	if (typeof (partida) === 'undefined')
		return res.status(404).json({ erro: 'Partida não existe' });

	if (delete partidas[idPartida])
		res.json({mensagem: 'Partida removida com sucesso'});
	else
		res.status(500).json({erro: 'Problema ao deletar a partida'})

});


app.put('/partida/:idPartida/status/:status', (req, res) => {
	const { idPartida, status } = req.params;

	if (typeof (status) === 'undefined')
		return res.status(400).json({ erro: 'Novo status da partida ausente' });
	else if (
		status != STATUS_PARTIDA.INICIADA &&
		status != STATUS_PARTIDA.ACEITA &&
		status != STATUS_PARTIDA.RECUSADA
	) {
		return res.status(400).json({ erro: 'Status informado inválido' });
	}

	if (typeof (idPartida) === 'undefined')
		return res.status(400).json({ erro: 'Nome do jogador solicitante ausente' });

	const
		partida = partidas[idPartida],
		statusAntigo = partida.status;

	if (typeof (partida) === 'undefined')
		return res.status(404).json({ erro: 'Partida não existe' });


	partida.status = parseInt(status, 10);

	if (statusAntigo != status) {
		if (status == STATUS_PARTIDA.ACEITA) {
			const
				jogadorSolicitante = jogadores.find(jogador => jogador.nome === partida.nomeJogadorSolicitante),
				oponente = jogadores.find(jogador => jogador.nome === partida.nomeOponente);



			jogadorSolicitante.tabuleiro = Tabuleiro.geraTabuleiro(10);
			oponente.tabuleiro = Tabuleiro.geraTabuleiro(10);
		}
		eventBus.emit('mudancaPartida-' + idPartida, partida);
	}

	res.json(partida);
});


app.listen(porta,() => console.log('Rodando na porta ' + porta))

