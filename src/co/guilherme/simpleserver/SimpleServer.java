package co.guilherme.simpleserver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class SimpleServer {

    private final String PATH = "C:/Users/Guilherme/Academia de Código/Projects/SimpleServer/www/test";

    private final String HEADER_SUCCESS_HTML = "HTTP/1.0 200 Document Follows\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "Content-Length: <file_byte_size> \r\n" +
            "\r\n";

    private final String HEADER_SUCCESS_FILE = "HTTP/1.0 200 Document Follows\r\n" +
            "Content-Type: image/<image_file_extension> \r\n" +
            "Content-Length: <file_byte_size> \r\n" +
            "\r\n";

    private final String HEADER_404 = "HTTP/1.0 404 Not Found\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "Content-Length: <file_byte_size> \r\n" +
            "\r\n";

    private final String [] files = {"page.html"};

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
            while(serverSocket.isBound()){
                renewSocket();
                initBuffers();
                readData();
                closeBuffers();
            }

            if(!connection.isClosed()){
                connection.close();
            }

            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't open connection.");
        }
    }

    private void initConnection() throws IOException {
        serverSocket = new ServerSocket(8989);
    }

    private void renewSocket() throws IOException {
        connection = serverSocket.accept();
        System.out.println("Accepted connection...");
    }

    private void initBuffers() throws IOException {
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        writer = new PrintWriter(connection.getOutputStream(), true);
    }

    private void closeBuffers() throws IOException {
        if(reader != null && writer != null){
            reader.close();
            writer.close();
        }
    }

    private void readData() throws IOException {
        String result = reader.readLine();
        System.out.println(result);

        if(result != null){
            String [] cmds = result.split(" ");
            String filename = cmds[1].substring(1);

            if(!cmds[1].contains(".ico") && !cmds[1].contains(".png") && !cmds[1].contains(".jpg")){
                switch(cmds[1]){
                    case "/":
                        System.out.println("Called for Index");
                        header = HEADER_SUCCESS_HTML.replace("<file_byte_size>", String.valueOf(fileSize("index.html")));
                        writer.println(header);
                        //System.out.println(header);
                        sendFile("index.html");
                        break;

                    case "/page.html":
                        header = HEADER_SUCCESS_HTML.replace("<file_byte_size>", String.valueOf(fileSize(filename)));
                        writer.println(header);
                        sendFile(filename);
                        writer.println(header);
                        break;

                    default:

                    case "/404" :
                        if(!exists(filename)){
                            filename = "404.html";
                        }
                        System.out.println(filename);
                        header = HEADER_404.replace("<file_byte_size>", String.valueOf(fileSize(filename)));
                        writer.println(header);
                        sendFile(filename);
                        break;
                }
            }

            else {
                if(cmds[1].equalsIgnoreCase("/favicon.ico")){
                    header = HEADER_SUCCESS_FILE.replace("<file_byte_size>", String.valueOf(fileSize("favicon.ico")));
                    //writer.println(header);
                    sendImage("favicon.ico");
                }
                else if(cmds[1].contains(".jpg") || cmds[1].contains(".png")){
                    String imgFile = cmds[1].substring(1);
                    sendImage(imgFile); //Contains header sending inside, as well
                }
            }
        }

        connection.close();

    }

    private String fileExtension(String filename){

        int index = 0;

        while(filename.charAt(index) != '.'){
            index++;
        }

        return filename.substring(index + 1);
    }

    private boolean exists(String filename){
        for(String string: files){
            if(filename.equalsIgnoreCase(string)){
                return true;
            }
        }
        return false;
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
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    private void sendImage(String filename) {

        try {
            File file = new File("www/test/" + filename);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            DataOutputStream binaryOut = new DataOutputStream(connection.getOutputStream());
            binaryOut.writeBytes("HTTP/1.0 200 OK\r\n");
            binaryOut.writeBytes("Content-Type: image/" + fileExtension(filename) + "\r\n");
            binaryOut.writeBytes("Content-Length: " + data.length);
            System.out.println("Image size: " + data.length);
            binaryOut.writeBytes("\r\n\r\n");
            binaryOut.write(data);

            binaryOut.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}
