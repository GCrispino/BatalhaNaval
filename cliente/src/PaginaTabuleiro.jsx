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

		fetch(`https://batalha-naval-webservice.herokuapp.com/jogador/${nomeJogador}`)
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
			fetch(`https://batalha-naval-webservice.herokuapp.com/jogador/${this.state.jogador.nome}/partida/`)
			.then(res => res.json())
			.then(partida => {
				if (partida.erro)
					throw new Error(partida.erro);

				const headers = new Headers();

				headers.append('Content-Type', 'application/json');

				return fetch(`https://batalha-naval-webservice.herokuapp.com/partida/${partida.id}`, {
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
		fetch(`https://batalha-naval-webservice.herokuapp.com/jogador/${nome}/disparo/ouvir`)
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

		fetch(`https://batalha-naval-webservice.herokuapp.com/jogador/${this.state.jogador.nome}/disparo`,{
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
			<div id='containerTabuleiro'>
			{
				this.state.jogador ?
				<div>
					{
						this.state.jogoFinalizado ?
							this.state.venceu ?
								<h2>Você venceu o jogo!</h2>
								: this.state.perdeu ?
								<h2>Você perdeu o jogo!</h2>
								: <h2>Erro</h2>
						: <h2>Jogador {this.state.jogador.nome}</h2>
					}
					{
						this.state.vez ?
						<h3>Sua vez!</h3>
						:<h3>Vez do oponente!</h3>
					}
					<Grid 
						id='tabuleiroOponente' 
						tabuleiro={this.state.tabuleiroDisparos} 
						onClickQuadrado = {this.handleClickTabuleiroOponente}
						tamQuadrado={60} 
						corQuadrado='#CD5C5C' /*corQuadradoSelecionado='#2068db'*/ 
						espacoQuadrados={5}
						width={650}
						height={650}/>
					<Grid 
						id='tabuleiroJogador' 
						tabuleiro={this.state.tabuleiroDisparosOponente} 
						tamQuadrado={10} 
						corQuadrado='#CD5C5C' 
						espacoQuadrados={2}
						width={120}
						height={120}/>
				</div>
				:<p>Carregando tabuleiro...</p>
			}
			</div>
		);
	}
}