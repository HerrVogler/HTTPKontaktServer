//Jakob Vogler

import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HTTPKontaktServer {
    private int localPort;
    private ServerSocket serverSocket;
    private Socket client;

    public HTTPKontaktServer(int port) throws Exception {
        localPort = port;

        serverSocket = new ServerSocket(localPort);
    }

    public void runServer() throws Exception {
        while (true){
            client = serverSocket.accept();

            InputStream rein = client.getInputStream();
            BufferedReader buff = new BufferedReader(new InputStreamReader(rein));

            String row, request = "";
            int contentLength = 0;
            boolean contentSent = false;

            try {
                while (true) {
                    row = buff.readLine();

                    if (row == null || row.equals("")) {
                        request += "\n";

                        if (contentSent) {
                            for (int i = 0; i < contentLength; i++) {
                                request += (char)buff.read();
                            }
                        }

                        break;
                    } else {
                        if (row.contains("Content-Length: ")) {
                            contentLength = Integer.parseInt(row.substring(16));
                            contentSent = true;
                        }
                        request += row + "\n";
                    }
                }

                System.out.println(request);

                PrintStream ps = new PrintStream(client.getOutputStream(), true);
                ps.println(execute(request));
            } catch (Exception e) {
                e.printStackTrace();
            }

            client.close();
        }
    }

    private String execute(String request) {
        String response;

        if (request.startsWith("GET / HTTP/1.1")){
            response = "HTTP/1.1 200 OK\n" +
                    "Server: HTTPKontaktServer: 1.0\n" +
                    "Connection: close\n" +
                    "Content-type: text/html, text, plain\n\n" +
                    //"Content-length: 78\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "<title>Adressbuch</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>Kontakte</h1>\n" +
                    "<p><a href=\"/kontaktformular\">Zum Kontaktformular</a></p><br>\n" +
                    printKontakte(getKontakte()) +
                    "</body>\n" +
                    "</html>\n";
        } else if (request.startsWith("GET /kontaktformular HTTP/1.1")) {
            response = "HTTP/1.1 200 OK\n" +
                    "Server: HTTPKontaktServer: 1.0\n" +
                    "Connection: close\n" +
                    "Content-type: text/html, text, plain\n\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "<title>Kontaktformular</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>Kontaktformular</h1>\n" +
                    "<p><a href=\"/\">Zum Adressbuch</a></p>\n" +
                    "<form method=\"post\">\n" +
                    "<label for=\"vorname\">Vorname</label><br>\n" +
                    "<input type=\"text\" id=\"vorname\" name=\"vorname\"><br>\n" +
                    "<label for=\"nachname\">Nachname</label><br>\n" +
                    "<input type=\"text\" id=\"nachname\" name=\"nachname\"><br>\n" +
                    "<input type=\"radio\" id=\"privat\" name=\"istPrivat\" value=\"true\">\n" +
                    "<label for=\"privat\">Privat</label>\n" +
                    "<input type=\"radio\" id=\"nichtprivat\" name=\"istPrivat\" value=\"false\">\n" +
                    "<label for=\"nichtprivat\">&#214ffentlich</label><br>\n" +
                    "<input type=\"submit\" value=\"Abschicken\">\n" +
                    "<input type=\"reset\" value=\"Zur&uuml;  cksetzen\">\n" +
                    "</form>\n" +
                    "</body>\n" +
                    "</html>\n";
        } else if (request.startsWith("POST /kontaktformular HTTP/1.1")) {
            Kontakt k = ermittelKontakt(request);

            if (k == null) {
                response = "HTTP/1.1 200 OK\n" +
                        "Server: HTTPKontaktServer: 1.0\n" +
                        "Connection: close\n" +
                        "Content-type: text/html, text, plain\n\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "<title>Kontaktformular</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<h1>Kontaktformular</h1>\n" +
                        "<p><a href=\"/\">Zum Adressbuch</a></p>\n" +
                        "<p style=\"color:red;\">Eingaben fehlerhaft</p>\n" +
                        "<form method=\"post\">\n" +
                        "<label for=\"vorname\">Vorname</label><br>\n" +
                        "<input type=\"text\" id=\"vorname\" name=\"vorname\"><br>\n" +
                        "<label for=\"nachname\">Nachname</label><br>\n" +
                        "<input type=\"text\" id=\"nachname\" name=\"nachname\"><br>\n" +
                        "<input type=\"radio\" id=\"privat\" name=\"istPrivat\" value=\"true\">\n" +
                        "<label for=\"privat\">Privat</label>\n" +
                        "<input type=\"radio\" id=\"nichtprivat\" name=\"istPrivat\" value=\"false\">\n" +
                        "<label for=\"nichtprivat\">&#214ffentlich</label><br>\n" +
                        "<input type=\"submit\" value=\"Abschicken\">\n" +
                        "<input type=\"reset\" value=\"Zur&uuml;cksetzen\">\n" +
                        "</form>\n" +
                        "</body>\n" +
                        "</html>\n";
            } else {
                response = "HTTP/1.1 301 Moved Permanently\n" +
                        "Location: /kontaktformular\n" +
                        "Server: HTTPKontaktServer: 1.0\n" +
                        "Connection: close\n";

                hinzufuegen(k);
            }
        } else if (request.startsWith("GET /favicon.ico HTTP/1.1")){
            response = "HTTP/1.1 200 OK\n" +
                    "Server: HTTPKontaktServer: 1.0\n" +
                    "Connection: close\n";
        } else {
            response = "HTTP/1.1 404\n" +
                    "Server: HTTPKontaktServer: 1.0\n" +
                    "Connection: close\n";
        }

        return response;
    }

    private Kontakt ermittelKontakt(String request) {
        if (!(request.contains("vorname") && request.contains("nachname") && request.contains("istPrivat"))) {
            return null;
        }

        String[] param = request.substring(request.indexOf("vorname=")).split("&");
        String vorname = param[0].substring(8), nachname = param[1].substring(9);

        if (vorname.length() == 0 || nachname.length() == 0) {
            return null;
        }

        return new Kontakt(vorname, nachname, Boolean.parseBoolean(param[2].substring(10)));
    }

    public synchronized boolean hinzufuegen(Kontakt k) {
        Gson gson = new Gson();
        List<Kontakt> kontakte = new LinkedList<>(getKontakte());
        kontakte.add(k);

        BufferedWriter bw = null;
        boolean bool = true;
        try {
            bw = new BufferedWriter(new FileWriter(new File(System.getProperty("user.home") + "/desktop/kontakte.txt")));
            bw.write(gson.toJson(kontakte.toArray()));
        } catch (Exception e) {
            e.printStackTrace();
            bool = false;
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                bool = false;
            }
        }

        return bool;
    }

    public synchronized List<Kontakt> getKontakte() {
        Gson gson = new Gson();
        File file = new File(System.getProperty("user.home") + "/desktop/kontakte.txt");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new LinkedList<>();
        }

        String out = "";
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));
            while (br.ready()) {
                out += br.readLine() + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Arrays.asList(gson.fromJson(out, Kontakt[].class));
    }

    public String printKontakte(List<Kontakt> kontakte) {
        String kontakteString = "";
        for (int i = kontakte.size() - 1; i >= 0; i--) {
            if (!kontakte.get(i).istPrivat()) {
                kontakteString += "<p> - " + kontakte.get(i).getNachname() + ", " + kontakte.get(i).getVorname() + "</p>\n";
            }
        }

        return kontakteString;
    }

    public void beendeServer() throws Exception {
        serverSocket.close();
    }
}
