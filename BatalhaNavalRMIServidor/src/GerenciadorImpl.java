import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GerenciadorImpl extends UnicastRemoteObject implements Gerenciador {
    private int counter = 0;
    private boolean prontoJogador1,prontoJogador2;

    protected GerenciadorImpl() throws RemoteException {
        super();

        this.prontoJogador1 = false;
        this.prontoJogador2 = false;
    }


    @Override
    public boolean registroFinalizado() throws RemoteException{
        return this.counter == 2;
    }

    @Override
    public boolean pronto(int id) throws RemoteException{
        if (id == 1)
            return this.prontoJogador1;
        else if (id == 2)return this.prontoJogador2;

        return this.prontoJogador1 && this.prontoJogador2;
    }


    @Override
    public void setPronto(int id) throws RemoteException{
        if (id == 1)
            this.prontoJogador1 = true;
        else if(id == 2)
            this.prontoJogador2 = true;
    }

    @Override
    public int registra() throws RemoteException {
        if (counter >= 2) return 0;

        return ++counter;
    }

    @Override
    public void finaliza(int idJogador) throws RemoteException {
        --counter;

        if (idJogador == 1)
            prontoJogador1 = false;
        else
            prontoJogador2 = false;
    }
}
