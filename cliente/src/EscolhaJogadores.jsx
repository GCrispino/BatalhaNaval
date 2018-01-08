import React, { Component } from 'react';
import Loader from 'react-loader';
import ListaJogadores from './ListaJogadores';

class EscolhaJogadores extends Component {
	constructor(props, context) {
		super(props, context);
		
		this.state = {
			jogadores: null,
			iJogadorSelecionado: 0,
			loading: false,
			aguardandoOponente: false
		};

	}
	
	componentDidMount = () => {
		console.log('componentDidMount');

		fetch('https://batalha-naval-webservice.herokuapp.com/jogador/')
		.then(res => res.json())
		.then(jogadores => this.setState({jogadores}))
		.then(() => fetch(`https://batalha-naval-webservice.herokuapp.com/jogador/${this.props.nomeJogador}/partida/ouvir`))
		.then(res => res.json())
		.then(partida => {
			const {nomeJogadorSolicitante} = partida;
			alert(`${nomeJogadorSolicitante} lhe solicitou uma partida!`);
			const 
				confirmou = window.confirm('Você aceita o convite?'),
				status = confirmou ? 1 : 2;


			console.log(partida);
			return fetch(`https://batalha-naval-webservice.herokuapp.com/partida/${partida.id}/status/${status}`,{
				method: 'PUT'
			})
			.then(res => {
				if (res.ok){
					if (confirmou){
						this.props.onInicioJogo(false);
					}
				}
				else alert('Erro ao atualizar status da partid!');
			});

		})
		.catch(console.error);
	}
	
	handleClickItemLista = e => {
		const strOffset = 'jogador'.length;
		this.setState({ iJogadorSelecionado: parseInt(e.target.id.substr(strOffset),10) });
	}

	handleClickSolicitarPartida = e => {
		const 
			{iJogadorSelecionado} = this.state,
			jogadorSelecionado = this.state.jogadores[iJogadorSelecionado];

		this.setState({ loading: true });

		const headers = new Headers();
		headers.append('Content-Type', 'application/json');

		fetch(`https://batalha-naval-webservice.herokuapp.com/partida/${jogadorSelecionado.nome}`,{
			method: 'POST',
			// mode: 'cors',
			headers,
			body: JSON.stringify({nomeJogador: this.props.nomeJogador})
		})
		.then(res => res.json())
		.then(resJSON => {
			const partida = resJSON;
			if (resJSON.erro){
				this.setState({loading: false});
				alert(resJSON.erro);
			}
			else{
				this.setState({ loading: false,aguardandoOponente: true });

				return fetch(`https://batalha-naval-webservice.herokuapp.com/partida/${partida.id}/ouvir`);
			}

		})
		.then(res => res.json())
		.then(partida => {
			this.setState({ aguardandoOponente: false});
			if (partida.status == 1){
				alert(`${jogadorSelecionado.nome} aceitou a partida!`);
				this.props.onInicioJogo(true);
			}
			else if (partida.status == 2){
				alert(`${jogadorSelecionado.nome} recusou a partida!`);
			}
			else
				alert('Erro!');

		})
		.catch(() => alert('Erro de rede!'));
	};


	render() {
		return (
			<div>
				{
					this.state.jogadores ?
					<div>
						<ListaJogadores jogadores={this.state.jogadores} iJogadorSelecionado={this.state.iJogadorSelecionado} onClickItemLista={this.handleClickItemLista}/>
						<button onClick={this.handleClickSolicitarPartida}>Solicitar partida!</button>
					</div>
					: <Loader position='relative' className='spinner'/>
				}
				
			</div>
		);
	}
}

export default EscolhaJogadores;