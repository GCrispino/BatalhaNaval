import React, { Component } from 'react';
import './ListaJogadores.css';

function ListaJogadores(props){
	return (
		<ul id='lista'>
			{props.jogadores.map(
				(jogador, i) => <li
					className={jogador === props.jogadores[props.iJogadorSelecionado] ? 'selecionado' : ''}
					onClick={props.onClickItemLista}
					key={i} id={`jogador${i}`}>
					{jogador.nome}
				</li>
			)}
		</ul>
	);
}

export default ListaJogadores;