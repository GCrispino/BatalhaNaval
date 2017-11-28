import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Gerenciador extends Remote {

    boolean registroFinalizado() throws RemoteException;

    boolean pronto(int id) throws RemoteException;

    void setPronto(int id) throws RemoteException;

    int registra() throws RemoteException;
    void finaliza(int idCliente) throws RemoteException;

}
