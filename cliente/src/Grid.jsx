import React, { Component } from 'react';

export default class Grid extends Component{
	constructor(props){
		super(props);

	}

	constroiGrid = () => {
		const 
			{tabuleiro,tamQuadrado,corQuadrado,corQuadradoSelecionado,espacoQuadrados} = this.props,
			altura = tabuleiro.length,largura = tabuleiro[0].length;

		const rects = [];

		for (let y = 0;y < altura;++y){
			for (let x = 0;x < largura;++x){
				const corQuadradoAtual = tabuleiro[y][x] ? corQuadradoSelecionado : corQuadrado;
				rects.push(
					<rect 
						key={x + ',' + y}
						id={x + ',' + y}
						fill={this.props.tabuleiro[y][x]} 
						x={(tamQuadrado + espacoQuadrados) * x} y={(tamQuadrado + espacoQuadrados) * y} 
						height={tamQuadrado} width={tamQuadrado}
						onClick={this.props.onClickQuadrado}
					></rect>
				);
			}
		}

		return rects;
	}

	render(){
		return (
			<svg id='grid' width={this.props.width} height={this.props.height}>
				{this.constroiGrid()}
			</svg>
		);
	}
}