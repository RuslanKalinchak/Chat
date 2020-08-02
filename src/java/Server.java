package java;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    public static void sendBroadcastMessage(Message message){
        for (Map.Entry<String, Connection> sending: connectionMap.entrySet()){
            Connection connection = sending.getValue();
            try {
                connection.send(message);
            } catch (IOException e) {
                System.out.println("Сообщение не было отправлено");
            }
        }
    }



    private static class Handler extends Thread{
        private  Socket socket;
        public Handler (Socket socket){
            this.socket=socket;
        }
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message request = new Message(MessageType.NAME_REQUEST);
            Message answer;
            String userName;
            do{
                connection.send(request);
                answer = connection.receive();
                userName = answer.getData();
            } while (answer.getType()!= MessageType.USER_NAME||userName.isEmpty()||connectionMap.containsKey(userName));
            connectionMap.put(userName, connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED, "Ваше имя принято"));

            return userName;
        }
        private void notifyUsers(Connection connection, String userName) throws IOException{
            for (Map.Entry<String, Connection> sending: connectionMap.entrySet()){
                String name = sending.getKey();
                if (!name.equals(userName)){
                    Message answer = new Message(MessageType.USER_ADDED, name);
                    connection.send(answer);}
            }
        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{


            while (true){
                Message userMessage = connection.receive();
                if (userMessage.getType()==MessageType.TEXT){
                    String formatted = userName+": "+userMessage.getData();
                    Message    formattedUserMessage = new Message(MessageType.TEXT, formatted);
                    sendBroadcastMessage(formattedUserMessage);
                } else {
                    ConsoleHelper.writeMessage(String.format("Error: expected to get text message, but got %s\n", userMessage.getType()));
                }
            }

        }

        public void run(){
            System.out.println("Установлено новое соеденение с адресом "+ socket.getRemoteSocketAddress());
            Connection connection = null;
            String name=null;
            try {
                connection = new Connection(socket);
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                notifyUsers(connection, name);
                serverMainLoop(connection, name);

            } catch (IOException e) {
                e.printStackTrace();
                ConsoleHelper.writeMessage("Соединение с удаленным адресом: " + socket.getRemoteSocketAddress() + " закрыто.");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                ConsoleHelper.writeMessage("Соединение с удаленным адресом: " + socket.getRemoteSocketAddress() + " закрыто.");
            } finally {
                if (name!=null) {
                    connectionMap.remove(name);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
                }

                ConsoleHelper.writeMessage("Соединение с удаленным адресом: " + socket.getRemoteSocketAddress() + " закрыто.");

            }
        }

        public static void main(String[] args) throws IOException {
            int consoleHelher = ConsoleHelper.readInt();
            ServerSocket serverSocket = new ServerSocket(consoleHelher);
            System.out.println("Сервер запущен");
            while (true){
                try{
                    Socket socket = serverSocket.accept();
                    Handler handler = new Handler(socket);
                    handler.start();

                } catch (Exception e){
                    serverSocket.close();
                    System.out.println(e.getMessage());
                    break;
                }
            }
        }
    }}

