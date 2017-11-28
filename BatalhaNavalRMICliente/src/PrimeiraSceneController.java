import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class PrimeiraSceneController {
    public Label helloWorld;
    public TextField valorHost;
    public TextField valorPorta;

    public void sayHelloWorld(ActionEvent actionEvent) {
        helloWorld.setText("Hello World!");
    }

    public void mudarParaPrimeiraScene(ActionEvent actionEvent) throws IOException {
        Parent rootPrimeiraScene = FXMLLoader.load(getClass().getResource("PrimeiraScene.fxml"));

        Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();

        window.setScene(new Scene(rootPrimeiraScene));
        window.show();
    }

    public void mudarParaGrid(ActionEvent actionEvent) throws IOException {

        Stage theStage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        Socket cnxSocket = null;
        String host = valorHost.getText();

        int porta = Integer.parseInt(valorPorta.getText());

        try {
            cnxSocket = new Socket(host, porta);
            System.out.println("host: " + host + ", porta: " + porta);

            String sentence = "vim do cliente", sentenceServer;

            DataOutputStream outToServer = new DataOutputStream(cnxSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(cnxSocket.getInputStream()));
            outToServer.writeBytes(sentence + '\n');
            sentenceServer = inFromServer.readLine();

            System.out.println("Recebeu do servidor: " + sentenceServer);

        } catch (IOException e) {
            System.out.println("Erro na conexão com oponente!");

            return;
        }

        /*JogoImpl jogo = new JogoImpl(theStage,true,0);
        jogo.carregarCenaJogo();*/

    }

}
