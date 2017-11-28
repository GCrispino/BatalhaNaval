import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Crispino on 11/17/2017.
 */
public class JogoImpl extends UnicastRemoteObject implements Jogo{

    private final int TAMANHO_TABELA = 10;
    private Jogo jogoOponente;
    private int id;
    private Stage theStage;
    private GridController gridController;
    private boolean solicitante;
    private boolean vez;
    private boolean acabou = false;
    private boolean tabelaNavios[][];
    private boolean tabelaNaviosOponente[][];
    private boolean tabelaRegistroDisparos[][];
    private final Integer navios[][] = {
            {1,5}, //porta-aviões
            {2,4}, //navios-tanque
            {3,3}, //contratorpedeiros
            {4,2}  //submarinos
    };
    private int nCasasRestantes;
    private int nCasasRestantesOponente;


    public JogoImpl(Stage stage, boolean solicitante, int id, Jogo jogoOponente) throws RemoteException{
        this.theStage = stage;
        this.solicitante = solicitante;
        this.vez = solicitante;
        this.jogoOponente = jogoOponente;

        this.nCasasRestantes = this.getNCasasRestantes();
        this.nCasasRestantesOponente = this.getNCasasRestantes();

        this.tabelaNaviosOponente = new boolean[this.TAMANHO_TABELA][this.TAMANHO_TABELA];
        this.tabelaRegistroDisparos = new boolean[this.TAMANHO_TABELA][this.TAMANHO_TABELA];

        this.id = id;
    }

    public int getNCasasRestantes(){
        int nCasasRestantes = 0;
        for (Integer navio[]: this.navios)
            nCasasRestantes += navio[0] * navio[1];

        return nCasasRestantes;
    }


    public boolean[][] geraTabelaNavios(int tamanho){
        int nTiposNavios = 4;
        ArrayList<Integer[]> listaNavios = new ArrayList<>();

        boolean tabelaNavios[][] = new boolean[tamanho][tamanho];


        for (int i = 0; i < nTiposNavios; ++i)
            listaNavios.add(this.navios[i]);

        Random gen = new Random();

        int nIteracoesLaco = 0;
        while(nTiposNavios > 0) {
            boolean achouEspaco = false,horizontal = true;
            int iEscolhido = gen.nextInt(nTiposNavios),
                    x = 0,y = 0,
                    qtdNavio = listaNavios.get(iEscolhido)[0],
                    tamanhoNavio = listaNavios.get(iEscolhido)[1];

            while(!achouEspaco){
                //se alcança 500 iterações, re-executa a função para evitar loops infinitos
                if (++nIteracoesLaco >= 500) {
                    System.out.println("Atingiu numero maximo de iterações!");
                    System.out.println(nIteracoesLaco);

                    return null;
                }
                horizontal = gen.nextBoolean();

                if (horizontal){
                    x = gen.nextInt(tamanho - tamanhoNavio);
                    y = gen.nextInt(tamanho);
                }
                else{
                    x = gen.nextInt(tamanho);
                    y = gen.nextInt(tamanho - tamanhoNavio);
                }


                if (x != 0)
                    if (tabelaNavios[x - 1][y])
                        continue;
                if (x + tamanhoNavio + 1 < tamanho)
                    if (tabelaNavios[x + tamanhoNavio + 1][y])
                        continue;

                if (y != 0)
                    if (tabelaNavios[x][y - 1])
                        continue;
                if (y + tamanhoNavio + 1 < tamanho)
                    if (tabelaNavios[x][y + tamanhoNavio + 1])
                        continue;


                boolean achouNavio = false;

                for (int i = 0;i < tamanhoNavio;++i) {
                    if (horizontal) {
                        boolean achouMesmaPosicao = tabelaNavios[x + i][y],
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
                        boolean achouMesmaPosicao = tabelaNavios[x][y + i],
                                achouEsquerda = x > 0 && tabelaNavios[x - 1][y + i],
                                achouDireita = x < tamanho - 1 && tabelaNavios[x + 1][y + i],
                                achouEmBaixo = y > 0 && tabelaNavios[x][y + i - 1],
                                achouEmCima = y < tamanho - 1 && tabelaNavios[x][y + i + 1];
                        if (achouMesmaPosicao || achouEsquerda || achouDireita || achouEmCima || achouEmBaixo) {
                            achouNavio = true;
                            break;
                        }
                    }
                }

                if (achouNavio) continue;

                achouEspaco = true;
            }

            listaNavios.get(iEscolhido)[0] = --qtdNavio;

            for (int i = 0;i < tamanhoNavio;++i) {
                if (horizontal){
                    tabelaNavios[x + i][y] = true;
                }
                else{
                    tabelaNavios[x][y + i] = true;
                }
            }

            if (qtdNavio == 0) {
                listaNavios.remove(iEscolhido);
                --nTiposNavios;
            }

        }

        return tabelaNavios;
    }



    public void carregarCenaJogo(){

        System.out.println("JogoImpl iniciado!");


        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                System.out.println("Gerando tabela dos navios...");
                while(tabelaNavios == null) {
                    System.out.println("Gerando tabela dos navios...");

                    tabelaNavios = geraTabelaNavios(10);
                }


                try {
                    boolean tabelaNaviosProprio[][] = getTabelaNaviosProprio(),
                            tabelaNaviosOponente[][] = getTabelaNaviosOponente();

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("TelaPrincipalGrid.fxml"));
                    gridController = new GridController();

                    loader.setController(gridController);

                    Parent root = null;
                    try {
                        root = loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    theStage.setScene(new Scene(root));
                    theStage.show();

                    Platform.runLater(() -> {
                        System.out.println("CARREGOU NOVA CENA!!!");

                        gridController.setGridOponente(tabelaNaviosOponente);
                        gridController.setGridProprio(tabelaNaviosProprio);


                        String textoLabel;
                        if (vez)
                            textoLabel = "Sua vez!";
                        else
                            textoLabel = "Vez do oponente!";

                        gridController.mudaTextoLabel(textoLabel);


                        iniciarJogo();
                    });


                }
                catch(RemoteException e){
                    e.printStackTrace();
                }


            }
        });

    }

    @Override
    public boolean disparo(int x, int y) throws RemoteException{

        boolean acertou = this.checaTiro(x, y);
        Color corParaPintar;

        if (acertou){
            corParaPintar = Color.GREEN;

            if (--this.nCasasRestantes == 0){
                //Acabou o jogo!
                this.acabou = true;
                this.setTextoLabel("Acabou o jogo, você perdeu!");
            }
        }
        else{
            corParaPintar = Color.BLUE;
            this.trocaVez();

            this.setTextoLabel("Sua vez!");
        }

        //pinta no tabuleiro do oponente o resultado do tiro
        this.pintaQuadrado(x,y,corParaPintar.getRed(),corParaPintar.getGreen(),corParaPintar.getBlue());


        return acertou;
    }

    public boolean checaTiro(int x,int y){
        return this.tabelaNavios[x][y];
    }


    public void iniciarJogo(){
        Scene cenaJogo = this.theStage.getScene();
        GridPane gridOponente = this.gridController.getGridOponente(),
                 gridProprio = this.gridController.getGridProprio();

        gridOponente.setOnMouseClicked(event -> {
            if (!vez) return;

            Rectangle rect = (Rectangle)event.getTarget();

            byte x = gridOponente.getColumnIndex(rect).byteValue(),
                 y = gridOponente.getRowIndex(rect).byteValue();


            try {

                //verifica se disparo nessas coordenadas já foi realizado
                if (this.tabelaRegistroDisparos[x][y])
                    return;

                boolean acertou = this.jogoOponente.disparo(x,y);
                Color corParaPintar;

                //registra o disparo
                this.tabelaRegistroDisparos[x][y] = true;

                if (acertou) {
                    corParaPintar = Color.GREEN;


                    if (--this.nCasasRestantesOponente == 0) {
                        //Acabou o jogo!
                        this.acabou = true;
                        this.setTextoLabel("Acabou o jogo, você venceu!");
                        jogoOponente.setTextoLabel("Acabou o jogo, você perdeu!");
                    }
                }
                else{
                    corParaPintar = Color.BLUE;
                    this.trocaVez();

                    this.setTextoLabel("Vez do oponente!");
                    jogoOponente.setTextoLabel("Sua vez!");
                }
                this.gridController.pintaQuadradoOponente(x,y,corParaPintar);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        });

    }

    @Override
    public void trocaVez() throws RemoteException {
        this.vez = !this.vez;
    }

    @Override
    public void pintaQuadrado(int x, int y, double r, double g, double b) throws RemoteException {
        this.gridController.pintaQuadradoProprio(x,y,new Color(r,g,b,1));
    }

    public void setFinalizado() throws RemoteException{
        this.acabou = true;
    }

    @Override
    public void setJogoOponente(Jogo jogoOponente) throws RemoteException{
        this.jogoOponente = jogoOponente;
    }

    @Override
    public void setTextoLabel(String texto) throws RemoteException {
        Platform.runLater(() -> this.gridController.mudaTextoLabel(texto));
    }

    @Override
    public int getID() throws RemoteException {
        return this.id;
    }

    public boolean[][] getTabelaNaviosProprio() throws RemoteException{
        return this.tabelaNavios;
    }


    public boolean[][] getTabelaNaviosOponente() throws RemoteException{
        return this.tabelaNaviosOponente;
    }
}
