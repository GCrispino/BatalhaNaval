import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import PaginaInicial from './PaginaInicial';
import EscolhaJogadores from './EscolhaJogadores';
import PaginaTabuleiro from './PaginaTabuleiro';

class App extends Component {
 
  componentWillMount = () => {
    this.setState({
      loading: false,
      registrado: false,
      nomeJogador: null,
      jogoIniciado: false
    });
  }

  inicioJogo = vez => this.setState({jogoIniciado: true,vez});

  handleSubmit = (e) => {
    e.preventDefault();
    const nomeJogador = e.target.querySelector('input[name="nomeJogador"]').value;

    this.setState({ loading: true });

    fetch(`https://batalha-naval-webservice.herokuapp.com/jogador/${nomeJogador}`, {
      method: 'POST'
    })
      .then(res => res.json())
      .then(resJSON => {
        console.log(resJSON.erro);
        if (resJSON.erro)
          this.setState({ loading: false }, () => alert(resJSON.erro));
        else
          this.setState({ loading: false, registrado: true,nomeJogador }, () => alert(resJSON.mensagem));
      })
      .catch(console.error);

  }

  render() {
    return (
      // <EscolhaJogadores onInicioJogo={this.inicioJogo} nomeJogador={'Fulano'}/>
      <div>
      {
        this.state.registrado ?
          this.state.jogoIniciado ?
            <PaginaTabuleiro vez={this.state.vez} nomeJogador={this.state.nomeJogador}/>
            :<EscolhaJogadores onInicioJogo={this.inicioJogo} nomeJogador={this.state.nomeJogador}/>
        : <PaginaInicial handleSubmit={this.handleSubmit} />
      }
      </div>
    );
  }
}

export default App;
