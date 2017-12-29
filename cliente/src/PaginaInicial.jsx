import React, { Component } from 'react';


class PaginaInicial extends Component {
	
	componentWillMount = e => {
		this.setState({
			loading: false,
			registrado: false
		});
	};
	
	render() {
		return (

			<div>
			{
				this.state.loading ?
					<p>Carregando...</p>
				:
					<div>
						<form onSubmit={this.props.handleSubmit}>	
							<label><h3>Nome de jogador</h3></label><br/>
							<input name='nomeJogador' type='text' /><br />

							<button type='submit'>Registrar</button>
						</form>
						{/* {
							this.state.registrado ?
								<button onClick={this.props.afterRegistro}>Seguir para tela de jogadores!</button>
								: <button disabled>Seguir para tela de jogadores!</button>
						} */}
					</div>
			}
			</div>
		);
	}
}

export default PaginaInicial;