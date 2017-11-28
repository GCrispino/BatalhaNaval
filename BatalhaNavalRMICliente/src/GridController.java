import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Crispino on 11/15/2017.
 */
public class GridController implements Initializable{

    public GridPane gridOponente;
    public GridPane gridProprio;
    public Label estadoJogo;
    private Rectangle rects[][];
    private Rectangle rectsOponente[][];
    private Rectangle rectsProprio[][];
    private boolean tabela[][];
    private GridPane gridPane;
    private int tamQuadrado;
    private final int largura = 10;
    private final int altura = 10;
    private final int tamQuadradoOponente = 50;
    private final int tamQuadradoProprio = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ABRIU!");
    }

    public void GridController(){

    }

    public void setGridOponente(boolean[][] tabela){
        gridOponente.setHgap(this.tamQuadradoOponente / 10);
        gridOponente.setVgap(this.tamQuadradoOponente / 10);

        this.tabela = tabela;
        this.rectsOponente = new Rectangle[altura][largura];

        for (int x = 0;x < altura;++x){
            for (int y = 0;y < largura;++y) {

                Rectangle rect = new Rectangle(this.tamQuadradoOponente,this.tamQuadradoOponente, Color.INDIANRED);

                gridOponente.add(rect,x,y);

                this.rectsOponente[x][y] = rect;
            }
        }

        this.pintaGridOponente();
    }

    public void setGridProprio(boolean[][] tabela){
        gridProprio.setHgap(this.tamQuadradoProprio / 10);
        gridProprio.setVgap(this.tamQuadradoProprio / 10);

        this.tabela = tabela;
        this.rectsProprio = new Rectangle[altura][largura];

        for (int x = 0;x < altura;++x){
            for (int y = 0;y < largura;++y) {

                Rectangle rect = new Rectangle(this.tamQuadradoProprio,this.tamQuadradoProprio, Color.INDIANRED);

                gridProprio.add(rect,x,y);

                this.rectsProprio[x][y] = rect;
            }
        }

        this.pintaGridProprio();
    }

    private void pintaGridOponente(){
        //pinta todos os navios
        for (int x = 0;x < this.altura;++x)
            for (int y = 0;y < this.largura;++y)
                if (this.tabela[x][y])
                    this.rectsOponente[x][y].setFill(Color.BLACK);

    }

    private void pintaGridProprio(){
        //pinta todos os navios
        for (int x = 0;x < this.altura;++x)
            for (int y = 0;y < this.largura;++y)
                if (this.tabela[x][y])
                    this.rectsProprio[x][y].setFill(Color.BLACK);

    }

    public void mudaTextoLabel(String texto){
            System.out.println("TEXTO ATUAL LABEL: " + estadoJogo.getText());
            estadoJogo.setText(texto);
    }

    public void pintaQuadradoOponente(int x, int y, Color cor){
        Platform.runLater(() -> this.rectsOponente[x][y].setFill(cor));
    }

    public void pintaQuadradoProprio(int x, int y, Color cor){
        Platform.runLater(() -> this.rectsProprio[x][y].setFill(cor));
    }

    public GridPane getGridOponente(){
        return this.gridOponente;
    }

    public GridPane getGridProprio(){
        return this.gridProprio;
    }
}


