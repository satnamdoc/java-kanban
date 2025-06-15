import ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager;
import ru.yandex.practicum.taskmanagerapp.taskmanager.FileBackedTaskManager;
import ru.yandex.practicum.taskmanagerapp.task.*;

import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        FileBackedTaskManager.main(null);
    }

    public static void testInitTaskManagerSprint6(TaskManager taskManager) {
        System.out.println("--init--");
        taskManager.clear();

        taskManager.addTask(new Task("Simple task #1", "test task #1"));
        taskManager.addTask(new Task("Simple task #1", "test task #2"));

        int epicId = taskManager.addEpic(new Epic("Epic #1", "test epic #1"));
        taskManager.addEpic(new Epic("Epic #2", "test epic #2"));

        taskManager.addSubTask(new SubTask("Subtask #1",
                "subtask #1 for epic #" + epicId, epicId));
        taskManager.addSubTask(new SubTask("Subtask #2",
                "subtask #2 for epic #" + epicId, epicId));
        taskManager.addSubTask(new SubTask("Subtask #3",
                "subtask #3 for epic #" + epicId, epicId));
    }

    // fill TaskManager object with random data
    public static void testInitTaskManager(TaskManager taskManager) {
        System.out.println("--init--");
        taskManager.clear();

        Random rnd = new Random();
        int rndInt = rnd.nextInt(3) + 3;
        for (int i = 0; i < rndInt; i++) {
            taskManager.addTask(new Task("Simple task #" + i, "test task #" + i));
        }

        rndInt = rnd.nextInt(3) + 3;
        for (int i = 0; i < rndInt; i++) {
            int epicId = taskManager.addEpic(new Epic("Epic #" + i, "test epic #" + i));
            int rndInt1 = rnd.nextInt(3) + 3;
            for (int j = 0; j < rndInt1; j++) {
                taskManager.addSubTask(new SubTask("Subtask #" + j, "subtask #" + j
                        + " for epic #" + i, epicId));
            }
        }
    }

    // Вспомогательная функция для отладочной печати
    public static void printTaskManagerState(TaskManager taskManager) {
        List<Task> tasks = taskManager.getTaskList();
        List<Epic> epics = taskManager.getEpicList();
        List<SubTask> subTasks = taskManager.getSubTaskList();

        System.out.println("Tasks");
        if (tasks.isEmpty()) {
            System.out.println("[]");
        } else {
            System.out.print("[" + tasks.getFirst().getId() + ":" + tasks.getFirst().getStatus());
            for (int i = 1; i < tasks.size(); i++) {
                System.out.print(", " + tasks.get(i) + ":" + tasks.get(i).getStatus());
            }
            System.out.println("]");
        }

        System.out.println("Epics");
        if (epics.isEmpty()) {
            System.out.println("[]");
        } else {
            System.out.print("[" + epics.getFirst().getId() + ":" + epics.getFirst().getStatus());
            for (int i = 1; i < epics.size(); i++) {
                System.out.print(", " + epics.get(i).getId() + ":" + epics.get(i).getStatus());
            }
            System.out.println("]");
        }

        System.out.println("Subtasks");
        if (epics.isEmpty()) {
            System.out.println("[]");
        } else {
            System.out.print("[");
        }
        for (Epic epic : epics) {
            List<Integer> subTaskIds = epic.getSubTaskIds();
            System.out.print(epic.getId() + "->");
            if (subTaskIds.isEmpty()) {
                System.out.println("[]");
            } else {
                int id = subTaskIds.getFirst();
                for (SubTask subTask : subTasks) {
                    if (subTask.getId() == id) {
                        System.out.print("\n\t[" + id + ":" + subTask.getStatus());
                        break;
                    }
                }
                for (int i = 1; i < subTaskIds.size(); i++) {
                    id = subTaskIds.get(i);
                    for (SubTask subTask : subTasks) {
                        if (subTask.getId() == id) {
                            System.out.print(", " + id + ":" + subTask.getStatus());
                            break;
                        }
                    }
                }
                System.out.println("]");
            }
        }
        System.out.println("]");

        System.out.println("History");
        List<Task> history = taskManager.getHistory();
        if (history.isEmpty()) {
            System.out.println("[]");
        } else {
            System.out.print("[" + history.getFirst().getId());
            for (int i = 1; i < history.size(); i++) {
                System.out.print(", " + history.get(i).getId());
            }
            System.out.println("]");
        }
    }
}