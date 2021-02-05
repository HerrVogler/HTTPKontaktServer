//Jakob Vogler

public class KontakteUI {
    public static void main(String[] args) {
        HTTPKontaktServer server = null;
        try {
            server = new HTTPKontaktServer(2222);

            server.runServer();

            server.beendeServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
