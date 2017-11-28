import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Servidor {

    public static void main(String args[]){
        try {
            LocateRegistry.createRegistry(1099);
            System.out.println("Servidor rodando...");
            GerenciadorImpl ger = new GerenciadorImpl();
            Naming.rebind("//127.0.0.1/BatalhaNavalServidor",ger);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
