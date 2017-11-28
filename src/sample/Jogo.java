package sample;

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
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Crispino on 11/17/2017.
 */
public class Jogo{

    private final int TAMANHO_TABELA = 10;
    private Stage theStage;
    private GridController gridController;
    private Socket cnxSocket;
    private boolean solicitante;
    private boolean vez;
    private boolean acabou = false;
    private boolean tabelaNavios[][];
    private boolean tabelaNaviosOponente[][];
    private final Integer navios[][] = {
            {1,5}, //porta-aviões
            {2,4}, //navios-tanque
            {3,3}, //contratorpedeiros
            {4,2}  //submarinos
    };
    private int nCasasRestantes;
    private int nCasasRestantesOponente;


    public Jogo(Stage stage,Socket cnxSocket,boolean solicitante){
        this.theStage = stage;
        this.cnxSocket = cnxSocket;
        this.solicitante = solicitante;
        this.vez = solicitante;

        this.nCasasRestantes = this.getNCasasRestantes();
        this.nCasasRestantesOponente = this.getNCasasRestantes();

        this.tabelaNaviosOponente = new boolean[this.TAMANHO_TABELA][this.TAMANHO_TABELA];

    }

    public int getNCasasRestantes(){
        int nCasasRestantes = 0;
        for (Integer navio[]: this.navios)
            nCasasRestantes += navio[0] * navio[1];

        return nCasasRestantes;
    }


    private boolean[][] geraTabelaNavios(int tamanho){
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
                    /*nTiposNavios = 4;
                    listaNavios.clear();
                    for (int i = 0; i < nTiposNavios; ++i)
                        listaNavios.add(this.navios[i]);

                    nIteracoesLaco = 0;
                    break;*/
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

        System.out.println("Jogo iniciado!");


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Socket cnxSocket = null;

                System.out.println("Gerando tabela dos navios...");
                while(tabelaNavios == null) {
                    System.out.println("Gerando tabela dos navios...");

                    tabelaNavios = geraTabelaNavios(10);
                }

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
        });

    }

    public boolean checaTiro(int x,int y){
        return this.tabelaNavios[x][y];
    }


    public void iniciarJogo(){
        Scene cenaJogo = this.theStage.getScene();
        GridPane gridOponente = this.gridController.getGridOponente(),
                 gridProprio = this.gridController.getGridProprio();
        TextField text = (TextField)cenaJogo.lookup("#valorHost");

        gridOponente.setOnMouseClicked(event -> {
            if (!vez) return;

            Rectangle rect = (Rectangle)event.getTarget();

            byte x = gridOponente.getColumnIndex(rect).byteValue(),
                 y = gridOponente.getRowIndex(rect).byteValue();


            boolean acertou = false;
            byte [] coordenadaTiro = {x,y};
            int respostaOponente;
            try {
                BufferedInputStream inOponente =
                        new BufferedInputStream(this.cnxSocket.getInputStream());
                DataOutputStream outOponente = new DataOutputStream(this.cnxSocket.getOutputStream());

                outOponente.write(coordenadaTiro);
                System.out.println("Coordenadas do tiro enviadas para oponente!");
                System.out.println("Coordenadas: " + x + "," + y);

                respostaOponente = inOponente.read();
                System.out.println("Resposta do oponente recebida!");
                System.out.println("Resposta: " + respostaOponente);
                acertou = respostaOponente == 1 ? true : false;

                Color corParaPintar;
                if (acertou){
                    corParaPintar = Color.GREEN;

                    if (--this.nCasasRestantesOponente == 0){
                        //Acabou o jogo!
                        this.acabou = true;
                        Platform.runLater(() -> gridController.mudaTextoLabel("Acabou o jogo, você venceu!"));
                        cnxSocket.close();
                    }
                }
                else{
                    corParaPintar = Color.BLUE;
                    this.vez = !this.vez;
                }

                //pinta no tabuleiro do oponente o resultado do tiro
                gridController.pintaQuadradoOponente(x,y,corParaPintar);
                System.out.println("vez: " + this.vez);

            }
            catch(IOException e){
                e.printStackTrace();
            }

        });

        //nova thread que espera pela jogada do oponente se a vez nao for do jogador
        Thread esperaConexaoOponente = new Thread(() -> {
            while(true){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (this.acabou) break;
                if (!this.vez){
                    Platform.runLater(() -> gridController.mudaTextoLabel("Vez do oponente!"));

                    System.out.println("Esperando mensagem do oponente...");

                    boolean acertou = false;
                    byte [] coordenadaTiro = new byte[2];
                    try {
                        BufferedInputStream inOponente =
                                new BufferedInputStream(this.cnxSocket.getInputStream());
                        DataOutputStream outOponente = new DataOutputStream(this.cnxSocket.getOutputStream());

                        inOponente.read(coordenadaTiro);

                        System.out.println("Coordenadas do tiro do oponente recebidos!");
                        System.out.println("Coordenadas: " + coordenadaTiro[0] + ',' + coordenadaTiro[1]);

                        acertou = this.checaTiro(coordenadaTiro[0], coordenadaTiro[1]);

                        outOponente.writeBoolean(acertou);
                    }
                    catch(IOException e){
                        this.setFinalizado();
                        e.printStackTrace();
                    }
                    System.out.println("Resultado do tiro recebido!");
                    System.out.println("Resultado: " + acertou);
                    System.out.println();


                    Color corParaPintar;
                    if (acertou){
                        corParaPintar = Color.GREEN;

                        if (--this.nCasasRestantes == 0){
                            //Acabou o jogo!
                            this.acabou = true;
                            Platform.runLater(() -> gridController.mudaTextoLabel("Acabou o jogo, você perdeu!"));
                            try {
                                cnxSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    else{
                        corParaPintar = Color.BLUE;
                        this.vez = !this.vez;

                        Platform.runLater(() -> gridController.mudaTextoLabel("Sua vez!"));
                    }
                    System.out.println("ncasasrestantes: " + nCasasRestantes);

                    //pinta no tabuleiro resultado do tiro
                    gridController.pintaQuadradoProprio(coordenadaTiro[0],coordenadaTiro[1],corParaPintar);

                }
            }
        });

        esperaConexaoOponente.start();

    }

    public void setFinalizado(){
        this.acabou = true;
    }

    public boolean[][] getTabelaNaviosProprio(){
        return this.tabelaNavios;
    }


    public boolean[][] getTabelaNaviosOponente(){
        return this.tabelaNaviosOponente;
    }
}
