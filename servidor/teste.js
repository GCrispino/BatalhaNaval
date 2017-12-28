const Tabuleiro = require('./Tabuleiro');

let tabuleiro = null,tabuleiro2 = null;

while(tabuleiro === null || tabuleiro2 === null){
	console.log(Tabuleiro.navios);
	tabuleiro = Tabuleiro.geraTabuleiro(10);
	// for (let i = 0;i < 1000;++i)
	// 	Math.random();
	console.log(Tabuleiro.navios);		
	tabuleiro2 = Tabuleiro.geraTabuleiro(10);
	console.log(Tabuleiro.navios);
	
}
console.log(tabuleiro);
console.log(tabuleiro2);