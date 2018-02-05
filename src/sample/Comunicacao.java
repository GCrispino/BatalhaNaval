package sample;

import com.rabbitmq.client.*;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Comunicacao {
    private Connection connection;
    private Jogo jogo;
    private Stage primaryStage;
    private String exchangeName;

    Comunicacao(Connection connection,Jogo jogo,Stage primaryStage){
        this.connection = connection;
        this.jogo = jogo;
        this.primaryStage = primaryStage;
    }

    Comunicacao(Connection connection,Jogo jogo,String exchangeName){
        this.connection = connection;
        this.jogo = jogo;
        this.exchangeName = exchangeName;
    }

    public void enviaIDJogador(String message, String queueName){

        try {
            Channel channel = connection.createChannel();

            channel.queueDeclare(queueName, false, false, false, null);

            channel.basicPublish("", queueName, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");


            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void consomeIDJogador(String idJogador, String queueName){

        try {
            Channel channel = connection.createChannel();

            channel.queueDeclare(queueName, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8"),idOponente = null,oponenteSolicitante = null,split[];
                    System.out.println(" [x] Received '" + message + "'");

                    split = message.split(";");
                    idOponente = split[0];
                    oponenteSolicitante = split[1];

                    if (!idOponente.equals(idJogador)){
                        boolean solicitante = oponenteSolicitante.equals("0") ? true: false;

                        if (!solicitante)
                            enviaIDJogador(idJogador + ";0",queueName);

                        connection.close();

                        jogo = new Jogo(primaryStage,solicitante,idJogador,idOponente);

                        jogo.carregarCenaJogo();

                    }

                }
            };

            channel.basicConsume(queueName, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void disparo(int x, int y){
        String mensagem = this.jogo.getIDJogador() + ';' + this.jogo.getFlagDisparo() + ';' + x + ',' + y;

        try {
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(this.exchangeName, "fanout");

            channel.basicPublish(this.exchangeName, "", null, mensagem.getBytes());
            System.out.println(" [x] Sent '" + mensagem + "'");

            channel.close();
        }
        catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void enviarResultadoDisparo(int x,int y,boolean acertou){
        char charAcertou = acertou ? '1' : '0';
        String mensagem
                = this.jogo.getIDJogador() + ';' + this.jogo.getFlagResultadoDisparo() + ';' + x + ',' + y + ';' + charAcertou;

        try {
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(this.exchangeName, "fanout");

            channel.basicPublish(this.exchangeName, "", null, mensagem.getBytes());
            System.out.println(" [x] Sent '" + mensagem + "'");

            channel.close();
        }
        catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void recebeDisparo(){
        Channel channel = null;

        try {
            channel = this.connection.createChannel();

            channel.exchangeDeclare(this.exchangeName, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, this.exchangeName, "");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String mensagem = new String(body, "UTF-8"),split[],
                        idRemetente = null;
                    int tipoMensagem;
                    boolean acertou;

                    split = mensagem.split(";");

                    idRemetente = split[0];

                    if (idRemetente.equals(jogo.getIDJogador()))
                        return;

                    tipoMensagem = Integer.parseInt(split[1]);
                    String coordsStr[] = split[2].split(",");
                    int
                            x = Integer.parseInt(coordsStr[0]),
                            y = Integer.parseInt(coordsStr[1]);


                    if (tipoMensagem == jogo.getFlagDisparo()){
                        //DISPARO

                        Color corParaPintar;

                        acertou = jogo.checaTiro(x,y);


                        if (acertou){
                            int nCasasRestantes = jogo.getNCasasRestantes();
                            corParaPintar = Color.GREEN;
                            if ((nCasasRestantes - 1) == 0){

                                //Acabou o jogo!
                                jogo.setFinalizado();
                                Platform.runLater(() -> jogo.getGridController().mudaTextoLabel("Acabou o jogo, você perdeu!"));
                            }
                            jogo.setNCasasRestantes(nCasasRestantes - 1);
                        }
                        else{
                            corParaPintar = Color.BLUE;
                            boolean vez = jogo.getVez();
                            jogo.setVez(!vez);
                        }

                        enviarResultadoDisparo(x,y,acertou);


                        //PINTA RESULTADO NO GRID
                        jogo.getGridController().pintaQuadradoProprio(x,y,corParaPintar);

                    }
                    else if (tipoMensagem == jogo.getFlagResultadoDisparo()){
                        //RESULTADO DE DISPARO
                        int resDisparo = Integer.parseInt(split[3]);
                        acertou = resDisparo == 1;

                        Color corParaPintar;
                        if (acertou){
                            int nCasasRestantesOponente = jogo.getNCasasRestantesOponente();
                            corParaPintar = Color.GREEN;

                            if ((nCasasRestantesOponente - 1) == 0){
                                //Acabou o jogo!
                                Platform.runLater(() -> jogo.getGridController().mudaTextoLabel("Acabou o jogo, você venceu!"));
                                jogo.setFinalizado();
                            }

                            jogo.setNCasasRestantesOponente(nCasasRestantesOponente - 1);
                        }
                        else{
                            corParaPintar = Color.BLUE;
                            boolean vez = jogo.getVez();
                            jogo.setVez(!vez);
                        }

                        //pinta no tabuleiro do oponente o resultado do tiro
                        jogo.getGridController().pintaQuadradoOponente(x,y,corParaPintar);

                    }

                }
            };
            channel.basicConsume(queueName, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void fecharConexao(){
        try {
            this.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}