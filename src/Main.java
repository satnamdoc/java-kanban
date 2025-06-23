import ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager;
import ru.yandex.practicum.taskmanagerapp.taskmanager.FileBackedTaskManager;
import ru.yandex.practicum.taskmanagerapp.task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");
        FileBackedTaskManager.main(null);
    }
}