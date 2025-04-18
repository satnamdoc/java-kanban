import ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager;
import ru.yandex.practicum.taskmanagerapp.task.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = new TaskManager();
        testInitTaskManager(taskManager);
        testGetFuncs(taskManager);
        testUpdateFuncs(taskManager);
        testRemoveFuncs(taskManager);
    }

    // заполнение объекта TaskManager случайным набором данных
    // тестирование  TaskManager.addTask
    public static void testInitTaskManager(TaskManager taskManager) {
        System.out.println("--init--");
        taskManager.clear();

        Random rnd = new Random();
        int rndInt = rnd.nextInt(10) + 1;
        for (int i = 0; i < rndInt; i++) {
            taskManager.addTask(new Task("Simple task #" + i, "test task #" + i));
        }

        rndInt = rnd.nextInt(10) + 1;
        for (int i = 0; i < rndInt; i++) {
            int epicId = taskManager.addEpic(new Epic("Epic #" + i, "test epic #" + i));
            int rndInt_ = rnd.nextInt(10) + 1;
            for (int j = 0; j < rndInt_; j++) {
                taskManager.addSubTask(new SubTask("Subtask #" + j, "subtask #" + j
                        + " for epic #" + i, epicId));
            }
        }
    }

    // тестирование TaskManager.getTaskList, TaskManager.getEpicList, TaskManager.getSubTaskList,
    // TaskManager.getSubTasksOfEpic
    public static void testGetFuncs(TaskManager taskManager) {
        System.out.println("--\"get\" functions tests--");
        System.out.println("--Simple tasks--");
        for (Task task : taskManager.getTaskList()) {
            System.out.println(task);
        }
        System.out.println("--Epics--");
        for (Epic epic : taskManager.getEpicList()) {
            System.out.println(epic);
        }
        System.out.println("--Subtasks--");
        for (SubTask subTask : taskManager.getSubTaskList()) {
            System.out.println(subTask);
        }

        System.out.println("--Epics and binding subtasks--");
        for(Epic epic : taskManager.getEpicList()) {
            System.out.println(epic);
            for (SubTask subTask : taskManager.getSubTasksOfEpic(epic.getId())) {
                System.out.println("\t" + subTask);
            }
        }
    }

    // тестирование TaskManager.removeTask, TaskManager.clear... для каждого вида задач
    public static void testRemoveFuncs(TaskManager taskManager) {
        System.out.println("--remove functions tests--");

        Random rnd = new Random();

        ArrayList<Task> tasks = taskManager.getTaskList();
        System.out.println("--before--");
        taskManager.printStatuses();

        int id = tasks.get(rnd.nextInt(tasks.size())).getId();
        taskManager.removeTask(id);
        System.out.println("--after " + id + " removal--");
        taskManager.printStatuses();

        ArrayList<Epic> epics = taskManager.getEpicList();
        id = epics.get(rnd.nextInt(epics.size())).getId();
        taskManager.removeEpic(id);
        System.out.println("--after " + id + " removal--");
        taskManager.printStatuses();

        ArrayList<SubTask> subTasks = taskManager.getSubTaskList();
        id = subTasks.get(rnd.nextInt(subTasks.size())).getId();
        taskManager.removeSubTask(id);
        System.out.println("--after " + id + " removal--");
        taskManager.printStatuses();

        System.out.println("--remove all subtasks--");
        taskManager.clearSubTasks();
        taskManager.printStatuses();

        testInitTaskManager(taskManager);
        taskManager.printStatuses();
        System.out.println("--remove all epics--");
        taskManager.clearEpics();
        taskManager.printStatuses();

        testInitTaskManager(taskManager);
        taskManager.printStatuses();
        System.out.println("--remove all tasks--");
        taskManager.clearTasks();
        taskManager.printStatuses();
    }

    // тестирование ru.yandex.practicum.taskmanagerapp.taskmanager.TaskManager.updateTask с учетом изменения статусов и целостности внутренних связей
    public static void testUpdateFuncs(TaskManager taskManager) {
        System.out.println("--update functions tests--");
        
        Random rnd = new Random();

        ArrayList<Task> tasks = taskManager.getTaskList();        
        System.out.println("--before--");
        for (Task task : tasks) {
            System.out.println(task);
        }
        Task oldTask = tasks.get(rnd.nextInt(tasks.size()));
        Task newTask = new Task("*Updated*" + oldTask.getName(), "*Updated*" + oldTask.getDescription());
        newTask.setId(oldTask.getId());
        newTask.setStatus(TaskStatus.IN_PROGRESS);
        if(taskManager.updateTask(newTask)) {
            System.out.println("--task #" + newTask.getId() + " has been updated--");
        }
        oldTask = tasks.get(rnd.nextInt(tasks.size()));
        newTask = new Task("*Updated*" + oldTask.getName(), "*Updated*" + oldTask.getDescription());
        newTask.setId(oldTask.getId());
        newTask.setStatus(TaskStatus.DONE);
        if(taskManager.updateTask(newTask)) {
            System.out.println("--task #" + oldTask.getId() + " has been updated--");
        }
        for (Task task : taskManager.getTaskList()) {
            System.out.println(task);
        }

        ArrayList<Epic> epics = taskManager.getEpicList();
        System.out.println("--before--");
        for (Epic curEpic : epics) {
            System.out.println(curEpic);
        }
        Epic oldEpic = epics.get(rnd.nextInt(epics.size()));
        Epic newEpic = new Epic("*Updated*" + oldEpic.getName(), "*Updated*" + oldEpic.getDescription());
        newEpic.setId(oldEpic.getId());
        if(taskManager.updateEpic(newEpic)) {
            System.out.println("--epic #" + oldEpic.getId() + " has been updated--");
        }
        newEpic = new Epic("*Updated*" + oldEpic.getName(), "*Updated*" + oldEpic.getDescription());
        newEpic.setId(oldEpic.getId());
        newEpic.setStatus(TaskStatus.DONE);                                        // запрещено менять статус эпика
        if(taskManager.updateEpic(newEpic)) {
            System.out.println("--epic #" + oldEpic.getId() + " has been updated--");
        }
        for (Epic epic : taskManager.getEpicList()) {
            System.out.println(epic);
        }

        ArrayList<SubTask> subTasks = taskManager.getSubTaskList();
        System.out.println("--before--");
        for (SubTask subTask : subTasks) {
            System.out.println(subTask);
        }
        SubTask oldSubTask = subTasks.get(rnd.nextInt(subTasks.size()));
        SubTask newSubTask = new SubTask("*Updated*" + oldSubTask.getName(),
            "*Updated*" + oldSubTask.getDescription(), oldSubTask.getEpicId());
        newSubTask.setId(oldSubTask.getId());
        newSubTask.setStatus(TaskStatus.IN_PROGRESS);
        if(taskManager.updateSubTask(newSubTask)) {
            System.out.println("--subtask #" + oldSubTask.getId() + " has been updated--");
        }
        subTasks = taskManager.getSubTaskList();
        for (SubTask curSubTask : subTasks) {
            System.out.println(curSubTask);
        }
        taskManager.printStatuses();


        epics = taskManager.getEpicList();
        Epic epic = epics.get(rnd.nextInt(epics.size()));
        for (Integer subTaskId : epic.getSubTaskIds()) {
            oldSubTask = (SubTask)taskManager.getSubTask(subTaskId);
            newSubTask = new SubTask("*Updated*" + oldSubTask.getName(),
                    "*Updated*" + oldSubTask.getDescription(), oldSubTask.getEpicId());
            newSubTask.setId(oldSubTask.getId());
            newSubTask.setStatus(TaskStatus.DONE);
            if(taskManager.updateTask(newSubTask)) {
                System.out.println("--subtask #" + subTaskId + " has been updated--");
            }
        }
        taskManager.printStatuses();
    }
}
