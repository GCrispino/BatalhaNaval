package sample;

import com.rabbitmq.client.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Date;

public class Main extends Application {
    private Jogo jogo = null;
    private String host;
    private String idJogador;
    private Stage primaryStage;
    private boolean solicitante = false;
    private Comunicacao comm;

    private final static String QUEUE_NAME = "jogadores";

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.comm = null;

        this.primaryStage = primaryStage;
        Parameters params = this.getParameters();

        try {
            host = params.getRaw().get(0);
        }
        catch(IndexOutOfBoundsException e){
            host = "localhost";
        }

        System.out.println("host: " + host);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        Connection connection = null;
        this.idJogador = new Long((new Date()).getTime()).toString();

        connection = factory.newConnection();

        comm = new Comunicacao(connection,jogo,primaryStage);

        comm.consomeIDJogador(this.idJogador,this.QUEUE_NAME);

        comm.enviaIDJogador(this.idJogador + ";1",this.QUEUE_NAME);


        Parent rootPrimeiraScene = FXMLLoader.load(getClass().getResource("PrimeiraScene.fxml"));

        primaryStage.setScene(new Scene(rootPrimeiraScene));
        primaryStage.show();

    }

    @Override
    public void stop(){
        if (this.comm != null)
            this.comm.fecharConexao();
        if (this.jogo != null)
            this.jogo.setFinalizado();
        System.out.println("Fechou");
    }

    public static void main(String[] args) {
        launch(args);
    }


}
