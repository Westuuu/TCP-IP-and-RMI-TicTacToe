import controllers.GameServerImpl;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import interfaces.GameServerInterface;

public class TicTacToeServer {
    private static final int PORT = 1099;
    private static final String BIND_NAME = "TicTacToeServer";

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "localhost");
            
            GameServerImpl server = new GameServerImpl();
            
            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(BIND_NAME, server);

            System.out.println("TicTacToe Server is running on port " + PORT);
            System.out.println("Server bound with name: " + BIND_NAME);
            
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
} 