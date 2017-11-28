package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;


public class Main extends Application {
    private ServerSocket welcomeSocket = null;
    private Socket connectionSocket = null;
    private Jogo jogo = null;
    private int porta;


    @Override
    public void start(Stage primaryStage) throws Exception {

        Parameters params = this.getParameters();

        try {
            porta = Integer.parseInt(params.getRaw().get(0));
        }
        catch(IndexOutOfBoundsException e){
            porta = 3000;
        }

        System.out.println("Porta: " + porta);


        new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    welcomeSocket = new ServerSocket(porta);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Esperando conexão de um oponente...");

                try {
                    connectionSocket = welcomeSocket.accept();

                    System.out.println("Recebeu!");

                    String clientSentence, serverSentence = "vim do servidor!";
                    BufferedReader inFromClient =
                            new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                    clientSentence = inFromClient.readLine();
                    System.out.println("Received: " + clientSentence);
                    outToClient.writeBytes(serverSentence + '\n');
                    System.out.println("Mensagem enviada para cliente: " + serverSentence);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Dentro do runLater");
                            jogo = new Jogo(primaryStage,connectionSocket,false);

                            jogo.carregarCenaJogo();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();

        Parent rootPrimeiraScene = FXMLLoader.load(getClass().getResource("PrimeiraScene.fxml"));

        primaryStage.setScene(new Scene(rootPrimeiraScene));
        primaryStage.show();

    }

    @Override
    public void stop(){
        try {
            if (welcomeSocket != null)
                welcomeSocket.close();
            if (connectionSocket != null)
                connectionSocket.close();
            if (jogo != null)
                jogo.setFinalizado();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Fechou");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
