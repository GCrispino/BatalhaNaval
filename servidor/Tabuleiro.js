module.exports = Tabuleiro = {};

const getNavios = () => {
	return [
		[1, 5], //porta-aviões
		[2, 4], //navios-tanque
		[3, 3], //contratorpedeiros
		[4, 2]  //submarinos
	]
};

// Tabuleiro.navios = navios;

Tabuleiro.geraTabuleiro = tamanho => {
	const tabelaNavios = Array(tamanho).fill([]).map(linha => Array(tamanho).fill(false));
	let listaNavios = getNavios();//copia array

	// console.log(tabelaNavios);
	let nTiposNavios = listaNavios.length;

	let nIteracoesLaco = 0;
	while (nTiposNavios > 0) {
		// console.log('listaNavios: ', JSON.stringify(listaNavios), nTiposNavios);
		const iEscolhido = Math.floor(Math.random() * nTiposNavios);
		// console.log('iescolhido: ', iEscolhido, listaNavios[iEscolhido]);
		const tamanhoNavio = listaNavios[iEscolhido][1];

		let achouEspaco = false, horizontal = true,
			qtdNavio = listaNavios[iEscolhido][0];


		let x = 0, y = 0;

		while (!achouEspaco) {
			//se alcança 5000 iterações, re-executa a função para evitar loops infinitos
			if (++nIteracoesLaco >= 5000) {
				console.log("Atingiu numero maximo de iterações!");
				console.log(nIteracoesLaco);
				console.log('tabela navios: ');
				console.log(tabelaNavios);

				return null;
			}
			horizontal = Math.random() < 0.5 ? true : false;

			if (horizontal) {
				x = Math.floor(Math.random() * (tamanho - tamanhoNavio));
				y = Math.floor(Math.random() * tamanho);
			}
			else {
				x = Math.floor(Math.random() * tamanho);
				y = Math.floor(Math.random() * (tamanho - tamanhoNavio));
			}


			// console.log(tamanho, tamanhoNavio, Math.floor(Math.random() * tamanho));
			// console.log('x: ' + x, ' y: ', y);
			if (x != 0)
				if (tabelaNavios[x - 1][y])
					continue;
			if (x + tamanhoNavio < tamanho)
				if (tabelaNavios[x + tamanhoNavio][y])
					continue;

			if (y != 0)
				if (tabelaNavios[x][y - 1])
					continue;
			if (y + tamanhoNavio < tamanho)
				if (tabelaNavios[x][y + tamanhoNavio])
					continue;


			let achouNavio = false;

			for (let i = 0; i < tamanhoNavio; ++i) {
				if (horizontal) {
					let achouMesmaPosicao = tabelaNavios[x + i][y],
						achouEsquerda = x > 0 && tabelaNavios[x + i - 1][y],
						achouDireita = x < tamanho - 1 && tabelaNavios[x + i + 1][y],
						achouEmCima = y < tamanho - 1 && tabelaNavios[x + i][y + 1],
						achouEmBaixo = y > 0 && tabelaNavios[x + i][y - 1];
					if (achouMesmaPosicao || achouEmCima || achouEmBaixo || achouEsquerda || achouDireita) {
						achouNavio = true;
						break;
					}
				}
				else {
					let achouMesmaPosicao = tabelaNavios[x][y + i],
						achouEsquerda = x > 0 && tabelaNavios[x - 1][y + i],
						achouDireita = x < tamanho - 1 && tabelaNavios[x + 1][y + i],
						achouEmBaixo = y > 0 && tabelaNavios[x][y + i - 1],
						achouEmCima = y < tamanho - 1 && tabelaNavios[x][y + i + 1];
					if (achouMesmaPosicao || achouEsquerda || achouDireita /*|| achouEmCima || achouEmBaixo*/) {
						achouNavio = true;
						break;
					}
				}
			}

			if (achouNavio) continue;

			achouEspaco = true;
		}

		listaNavios[iEscolhido][0] = --qtdNavio;


		for (let i = 0; i < tamanhoNavio; ++i) {
			if (horizontal) {
				tabelaNavios[x + i][y] = true;
			}
			else {
				tabelaNavios[x][y + i] = true;
			}
		}

		if (qtdNavio == 0) {
			listaNavios.splice(iEscolhido, 1);
			--nTiposNavios;
		}

	}

	return tabelaNavios;
};
