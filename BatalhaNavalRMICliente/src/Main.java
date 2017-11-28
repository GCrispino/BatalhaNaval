import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class Main extends Application {
    private JogoImpl jogoProprio = null;
    private Jogo jogoOponente = null;
    private Gerenciador gerenciador;
    private String ipRegistry = null;


    @Override
    public void start(Stage primaryStage) throws Exception {

        Parameters params = this.getParameters();

        try {
            ipRegistry = params.getRaw().get(0);
        }
        catch(IndexOutOfBoundsException e){
            ipRegistry = "127.0.0.1";
        }
        System.out.println("IP do registry: " + ipRegistry);

        new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    gerenciador = (Gerenciador) Naming.lookup("//" + ipRegistry + "/BatalhaNavalServidor");

                    int idResultRegistro,idOponente;
                    boolean solicitante;

                    idResultRegistro = gerenciador.registra();
                    idOponente = idResultRegistro == 1 ? 2 : 1;
                    solicitante = idResultRegistro == 1 ? true : false;

                    if (idResultRegistro == 0){
                        System.err.println("Não foi possível registrar o objeto!");
                        System.exit(1);
                        return;
                    }


                    System.out.println("Objeto registrado! ID " + idResultRegistro);
                    System.out.println("ID oponente: " + idOponente);

                    if (!gerenciador.registroFinalizado())
                        System.out.println("Esperando o oponente se registrar...");


                    while(!gerenciador.registroFinalizado()){}

                    System.out.println("Pronto!!!");

                    jogoProprio = new JogoImpl(primaryStage,solicitante,idResultRegistro,jogoOponente);
                    Naming.rebind("//127.0.0.1/Jogador" + idResultRegistro,jogoProprio);

                    gerenciador.setPronto(idResultRegistro);

                    System.out.println("Aguardando pelo registro do objeto remoto do oponente...");

                    while(!gerenciador.pronto(idOponente)){}

                    jogoOponente = (Jogo)Naming.lookup("//127.0.0.1/Jogador" + idOponente);
                    jogoProprio.setJogoOponente(jogoOponente);

                    if (idResultRegistro == 2){
                        while(true){}
                    }
                    else {
                        jogoProprio.carregarCenaJogo();
                        jogoOponente.carregarCenaJogo();
                    }


                } catch (NotBoundException e) {
                    System.out.println("Not bound exception");
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
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
            if (jogoProprio != null) {
                jogoProprio.setFinalizado();
                gerenciador.finaliza(jogoProprio.getID());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Fechou");
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
