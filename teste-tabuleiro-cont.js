const tabuleiro = JSON.parse(` [ [ false, false, false, false, true, true, false, true, false, false ],
     [ false, false, false, false, false, false, false, 'true', false, true ],
     [ false, true, false, false, true, true, false, false, false, true ],
     [ false, true, false, false, false, false, false, false, false, true ],
     [ false, true, false, false, false, false, false, false, false, false ],
     [ false, true, false, false, false, false, false, true, false, false ],
     [ false, false, true, true, true, true, false, true, false, false ],
     [ false, false, false, false, false, false, false, true, false, true ],
     [ false, true, true, true, true, true, false, false, false, true ],
	 [ false, false, false, false, false, false, true, true, true, false ] ]`);
	 
let resultRow = tabuleiro[0].reduce((cont, valAtual) => valAtual ? ++cont : cont, 0);

let result = tabuleiro.reduce((cont, linha) => cont + linha.reduce((cont, valAtual) => valAtual ? ++cont : cont, 0), 0);

console.log(result,resultRow);