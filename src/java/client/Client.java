package java.client;


import java.Connection;
import java.ConsoleHelper;
import java.Message;
import java.MessageType;
import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;
    protected String getServerAddress() throws IOException {
        return ConsoleHelper.readString();
    }
    protected int getServerPort() throws IOException {
        return ConsoleHelper.readInt();
    }
    protected String getUserName() throws IOException {
        return ConsoleHelper.readString();
    }
    protected boolean shouldSendTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }
    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Во время отправки произошла ошибка");
            clientConnected =false;
        }
    }
    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("При работе клиента возникла ошибка");
        }
        if (clientConnected){
            ConsoleHelper.writeMessage("Соединение установлено. "+
                    "Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        while(clientConnected) {
            String line = ConsoleHelper.readString();
            if (line.equals("exit")) break;
            if (shouldSendTextFromConsole()) sendTextMessage(line);
        }

    }
    public class SocketThread extends Thread{
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName+" присоединился к чату.");
        }
        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName+" покинул чат.");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while(true){
                if (connection.receive().getType()==MessageType.NAME_REQUEST){
                    String userName=getUserName();
                    Message message = new Message(MessageType.USER_NAME, userName);
                    connection.send(message);
                } else if (connection.receive().getType()==MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }
        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if (message.getType()==MessageType.TEXT){
                    processIncomingMessage(message.getData());
                } else if (message.getType()==MessageType.USER_ADDED){
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType()==MessageType.USER_REMOVED){
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }

        }
        public void run(){
            try {
                Socket socket = new Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException e) {
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}

