import ru.yandex.practicum.taskmanagerapp.httpserver.HttpTaskServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Поехали!");
        HttpTaskServer.main(null);
    }
}