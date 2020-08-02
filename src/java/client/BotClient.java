package java.client;

import java.ConsoleHelper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {
    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() throws IOException {
        int x = (int)(Math.random()*100);
        String botName = "date_bot_"+x;
        return botName;
    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(":")){
                String [] strings = message.split(":");
                String userName = strings[0].trim();
                String userMessage = strings[1].trim();
                Calendar currentTime = Calendar.getInstance();
                SimpleDateFormat dateFormatDay = new SimpleDateFormat("d");
                SimpleDateFormat dateFormatMouth = new SimpleDateFormat("MMMM");
                SimpleDateFormat dateFormatYear = new SimpleDateFormat("YYYY");
                SimpleDateFormat dateFormatFull = new SimpleDateFormat("d.MM.YYYY");
                SimpleDateFormat dateFormatTime = new SimpleDateFormat("H:mm:ss");
                SimpleDateFormat dateFormatHours = new SimpleDateFormat("H");
                SimpleDateFormat dateFormatMinutes = new SimpleDateFormat("m");
                SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("s");
                if (userMessage.equals("дата")){
                    sendTextMessage("Информация для "+userName+": "+dateFormatFull.format(currentTime.getTime()));
                }else if (userMessage.equals("день")){
                    sendTextMessage("Информация для "+userName+": "+dateFormatDay.format(currentTime.getTime()));
                } else if (userMessage.equals("месяц")){
                    sendTextMessage("Информация для "+userName+": "+dateFormatMouth.format(currentTime.getTime()));
                } else if (userMessage.equals("год")){
                    sendTextMessage("Информация для "+userName+": "+dateFormatYear.format(currentTime.getTime()));
                } else if (userMessage.equals("время")){
                    sendTextMessage("Информация для "+userName+": "+dateFormatTime.format(currentTime.getTime()));
                } else if (userMessage.equals("час")){
                    sendTextMessage("Информация для "+userName+": "+dateFormatHours.format(currentTime.getTime()));
                } else if (userMessage.equals("минуты")){
                    sendTextMessage("Информация для "+userName+": "+dateFormatMinutes.format(currentTime.getTime()));
                } else if (userMessage.equals("секунды")){
                    sendTextMessage("Информация для "+userName+": "+dateFormatSeconds.format(currentTime.getTime()));
                }

            }
        }}

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}

