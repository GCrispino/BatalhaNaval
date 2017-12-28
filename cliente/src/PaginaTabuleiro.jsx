/**
 * Fazer tratamento de chamadas fetch() que retornam algum código de erro
 */


import React, { Component } from 'react';
import Grid from './Grid.jsx';
import './PaginaTabuleiro.css';


export default class PaginaTabuleiro extends Component {
	
	tiposCamposTabuleiro = {
		NAO_PREENCHIDO: 0,
		OCUPADO_SEM_DISPARO: 1,
		OCUPADO_DISPARO_ERRADO: 2,
		OCUPADO_DISPARO_CERTO: 3
	}
	
	coresCasasTabuleiro = ['#cd5c5c', 'black','#2068db','green'];
	
	constructor(props){
		super(props);

		const {vez,nomeJogador} = props;

		this.state = {};

		fetch(`http://localhost:8888/jogador/${nomeJogador}`)
		.then(res => res.json())
		.then(jogador => {

			console.log('jogador: ',jogador);
			console.log('vez: ',vez);
	
			
			this.nDisparosCertosVencer = //número de disparos certos para vencer o jogo
				jogador.tabuleiro.reduce((cont, linha) => cont + linha.reduce((cont, valAtual) => valAtual ? ++cont : cont, 0), 0);
			this.setState({
				vez,
				jogoFinalizado: false,
				venceu: false, perdeu: false,
				jogador,
				tabuleiroDisparos: Array(10).fill([]).map(linha => Array(10).fill(this.coresCasasTabuleiro[this.tiposCamposTabuleiro.NAO_PREENCHIDO])),
				nDisparosCertos: 0,
				tabuleiroDisparosOponente: jogador.tabuleiro.map(
					linha => linha.map(
						rect => rect ? 
							this.coresCasasTabuleiro[this.tiposCamposTabuleiro.OCUPADO_SEM_DISPARO]
							: this.coresCasasTabuleiro[this.tiposCamposTabuleiro.NAO_PREENCHIDO]
					)
				),
				nDisparosCertosOponente: 0
			},() => {
				if (!vez){
					console.log('ouvir disparo construtor!!!');
					this.ouvirDisparo();
				}
			});
		})
		.catch(() => alert('Erro de rede!'));

	}

	componentDidUpdate(){
		if (this.state.jogoFinalizado && this.state.vez){
			fetch(`http://localhost:8888/jogador/${this.state.jogador.nome}/partida/`)
			.then(res => res.json())
			.then(partida => {
				if (partida.erro)
					throw new Error(partida.erro);

				const headers = new Headers();

				headers.append('Content-Type', 'application/json');

				return fetch(`http://localhost:8888/partida/${partida.id}`, {
					method: 'DELETE',
					headers
				});
			})
			.then(res => res.json())
			.then(resJSON => {
				if (resJSON.erro){
					alert('Erro ao remover partida!');
					console.error(resJSON.erro);
				}
			})
			.catch(() => alert('Erro ao remover partida!'));			
		}
	}

	ouvirDisparo = () => {
		const 
			{jogador} = this.state,
			{nome,tabuleiro} = jogador;

		console.log('dentro ouvir disparo!');
		fetch(`http://localhost:8888/jogador/${nome}/disparo/ouvir`)
		.then(res => res.json())
		.then(disparo => {
			const 
				{x,y} = disparo,
				{ tabuleiroDisparosOponente } = this.state,
				acertou = tabuleiro[y][x];

			if (acertou){
				const 
					nDisparosCertosOponenteNovo = this.state.nDisparosCertosOponente + 1,
					jogoFinalizado = nDisparosCertosOponenteNovo === this.nDisparosCertosVencer,
					perdeu = jogoFinalizado;

				tabuleiroDisparosOponente[y][x] = this.coresCasasTabuleiro[this.tiposCamposTabuleiro.OCUPADO_DISPARO_CERTO];

				return this.setState({ 
					tabuleiroDisparosOponente, 
					nDisparosCertosOponente: nDisparosCertosOponenteNovo,
					jogoFinalizado,
					perdeu
				}, this.ouvirDisparo)

			}
			else{
				tabuleiroDisparosOponente[y][x] = this.coresCasasTabuleiro[this.tiposCamposTabuleiro.OCUPADO_DISPARO_ERRADO];

				return this.setState({tabuleiroDisparosOponente,vez:true});
			}
		})
		.catch(erro => {
			console.error(erro);
			this.ouvirDisparo();
		});
	}

	handleClickTabuleiroOponente = e => {
		const
			elem = e.target,
			id = elem.id,
			[x, y] = id.split(',');
		console.log(x, y, this.state.tabuleiroDisparos[y][x]);

		if (!this.state.vez || this.state.tabuleiroDisparos[y][x] !== this.coresCasasTabuleiro[this.tiposCamposTabuleiro.NAO_PREENCHIDO]) return;

		const 
			disparo = {x,y},
			headers = new Headers();

		headers.append('Content-Type', 'application/json');

		console.log(elem);
		console.log(x, y);

		fetch(`http://localhost:8888/jogador/${this.state.jogador.nome}/disparo`,{
			method: 'POST',
			headers,
			body: JSON.stringify(disparo)
		})
		.then(res => res.json())
		.then(({acertou}) => {
			const {tabuleiroDisparos} = this.state;
			console.log(acertou);
			let fill;
			if (acertou){
				const 
					nDisparosCertosNovo = this.state.nDisparosCertos + 1,
					jogoFinalizado = nDisparosCertosNovo === this.nDisparosCertosVencer,
					venceu = jogoFinalizado;
				
				tabuleiroDisparos[y][x] = this.coresCasasTabuleiro[this.tiposCamposTabuleiro.OCUPADO_DISPARO_CERTO];


				this.setState({
					tabuleiroDisparos,
					nDisparosCertos: nDisparosCertosNovo,
					jogoFinalizado,
					venceu
				});
			}
			else{
				tabuleiroDisparos[y][x] = this.coresCasasTabuleiro[this.tiposCamposTabuleiro.OCUPADO_DISPARO_ERRADO];
				this.setState({ vez: false, tabuleiroDisparos},() => {
					this.ouvirDisparo();
				});
				
			}

			// elem.setAttributeNS(null, 'fill', fill);
		})
		.catch(() => alert('Erro ao executar o disparo!'));


	};

	render(){
		return (
			<div>
			{
				this.state.jogador ?
				<div>
					{
						this.state.jogoFinalizado ?
							this.state.venceu ?
								<p>Você venceu o jogo!</p>
								: this.state.perdeu ?
								<p>Você perdeu o jogo!</p>
								: <p>Erro</p>
						: <p>Jogador {this.state.jogador.nome}</p>
					}
					{
						this.state.vez ?
						<p>Sua vez!</p>
						:<p>Vez do oponente!</p>
					}
					<Grid 
						id='tabuleiroOponente' 
						tabuleiro={this.state.tabuleiroDisparos} 
						onClickQuadrado = {this.handleClickTabuleiroOponente}
						tamQuadrado={60} 
						corQuadrado='#CD5C5C' /*corQuadradoSelecionado='#2068db'*/ 
						espacoQuadrados={5}/>
					<Grid id='tabuleiroJogador' tabuleiro={this.state.tabuleiroDisparosOponente} tamQuadrado={10} corQuadrado='#CD5C5C' espacoQuadrados={2}/>
				</div>
				:<p>Carregando tabuleiro...</p>
			}
			</div>
		);
	}
}