package co.guilherme.simpleserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.stream.Collectors;

public class SimpleServer {

    private final String PATH = "C:/Users/Guilherme/Academia de Código/Projects/SimpleServer/www/test";

    private final String HEADER_SUCCESS_HTML = "HTTP/1.0 200 Document Follows\r\n\n" +
            "Content-Type: text/html; charset=UTF-8\r\n\n" +
            "Content-Length: <file_byte_size> \r\n\n" +
            "\r\n";

    private final String HEADER_SUCCESS_FILE = "HTTP/1.0 200 Document Follows\r\n\n" +
            "Content-Type: image/<image_file_extension> \r\n\n" +
            "Content-Length: <file_byte_size> \r\n\n" +
            "\r\n";

    private final String HEADER_404 = "HTTP/1.0 404 Not Found\n" +
            "Content-Type: text/html; charset=UTF-8\r\n\n" +
            "Content-Length: <file_byte_size> \r\n\n" +
            "\r\n";

    private ServerSocket serverSocket;
    private Socket connection;
    private BufferedReader reader;
    private PrintWriter writer;
    private String header;
    private String sendResource;
    private String getResource;

    public static void main(String[] args) {
        SimpleServer server = new SimpleServer();
        server.start();
    }

    public void start() {
        try {
            initConnection();
            initBuffers();
            readData();
            closeBuffers();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't open connection.");
        }
    }

    private void initConnection() throws IOException {
        serverSocket = new ServerSocket(8989);
        connection = serverSocket.accept();
        System.out.println("Accepted connection...");
    }

    private void initBuffers() throws IOException {
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        writer = new PrintWriter(connection.getOutputStream());
    }

    private void closeBuffers() throws IOException {
        if(reader != null && writer != null){
            reader.close();
            writer.close();
        }
    }

    private void readData() throws IOException {
        String result = reader.readLine();
        String [] cmds = result.split(" ");

        switch(cmds[1]){
            case "/":
                System.out.println("Called for Index");
                header = HEADER_SUCCESS_HTML.replace("<file_byte_size>", String.valueOf(fileSize("index.html")));
                writer.println(header);
                System.out.println(header);
                sendFile("index.html");
                break;

            default: break;
        }

    }

    private long fileSize(String filename){
        File file = new File("www/test/" + filename);
        return file.length();
    }

    private void sendFile(String filename){

        try {
            FileReader fReader = new FileReader("www/test/" + filename);
            BufferedReader bReader = new BufferedReader(fReader);
            String text = bReader.readLine();

            while(text != null){
                writer.println(text);
                text = bReader.readLine();
            }
            writer.flush();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
    }

}
