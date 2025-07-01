package ru.yandex.practicum.taskmanagerapp.taskmanager;

import ru.yandex.practicum.taskmanagerapp.exception.ManagerLoadException;
import ru.yandex.practicum.taskmanagerapp.exception.ManagerSaveException;
import ru.yandex.practicum.taskmanagerapp.history.HistoryManager;
import ru.yandex.practicum.taskmanagerapp.task.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File dataFile;
    private static final String CSVFILE_HEADER =
            "id,type,status,name,description,start time,duration,epic";

    public FileBackedTaskManager(File dataFile, HistoryManager historyManager) {
        super(historyManager);
        this.dataFile = dataFile;
    }

    private void save() {
        try (FileWriter fw = new FileWriter(dataFile); BufferedWriter writer = new BufferedWriter(fw)) {
            writer.write(CSVFILE_HEADER + "\n");
            for (Task task : super.getTaskList()) {
                writer.write(task.toCSVString() + "\n");
            }
            for (Epic epic : super.getEpicList()) {
                writer.write(epic.toCSVString() + "\n");
            }
            for (Subtask subtask : super.getSubtaskList()) {
                writer.write(subtask.toCSVString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Data file save error: " + e.getMessage());
        }
    }

    private static Task fromCSVString(String str) {
        //"id,type,status,name,description,start time,duration,epic,"
        String[] fields = str.split(",");

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        TaskStatus status = TaskStatus.valueOf(fields[2]);
        String name = fields[3];
        String description = fields[4];
        LocalDateTime startTime = (fields[5].equals("UNKNOWN")) ? null
                : LocalDateTime.parse(fields[5], Task.DATE_TIME_FORMATTER);
        Duration duration = Duration.ofMinutes(Integer.parseInt(fields[6]));
        int epicId = (fields.length == 8) ? Integer.parseInt(fields[7]) : Task.NULL_ID;

        return switch (type) {
            case TaskType.TASK -> new Task(id, name, description, status, startTime, duration);
            case TaskType.EPIC -> new Epic(id, name, description, status, startTime, duration,
                    startTime.plus(duration), new ArrayList<>());
            case TaskType.SUBTASK -> new Subtask(id, name, description, status, startTime, duration, epicId);
        };
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return loadFromFile(file, Managers.getDefaultHistory());
    }

    static FileBackedTaskManager loadFromFile(File file, HistoryManager historyManager) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file, historyManager);

        try (FileReader reader = new FileReader(file); BufferedReader br = new BufferedReader(reader)) {
            ArrayList<Task> tasks = new ArrayList<>();
            HashMap<Integer, Epic> epics = new HashMap<>(); // Map helps to bind epic and subtasks
            ArrayList<Subtask> subtasks = new ArrayList<>();

            br.readLine();
            while (br.ready()) {
                String str = br.readLine();
                Task task = fromCSVString(str);
                if (task instanceof Epic) {
                    epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    subtasks.add((Subtask) task);
                } else {
                    tasks.add(task);
                }
            }
            // bind epics and subtasks
            for (Subtask subtask : subtasks) {
                epics.get(subtask.getEpicId()).addSubtask(subtask.getId());
            }
            taskManager.load(tasks, new ArrayList<>(epics.values()), subtasks);
            return taskManager;
        } catch (IOException e) {
            throw new ManagerLoadException("Data file load error: " + e.getMessage());
        }
    }


    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        if (id != Task.NULL_ID) {
            save();
        }
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        if (id != Task.NULL_ID) {
            save();
        }
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        if (id != Task.NULL_ID) {
            save();
        }
        return id;
    }

    @Override
    public Task updateTask(Task task) {
        Task res = super.updateTask(task);
        if (res != null) {
            save();
        }
        return res;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic res = super.updateEpic(epic);
        if (res != null) {
            save();
        }
        return res;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask res = super.updateSubtask(subtask);
        if (res != null) {
            save();
        }
        return res;
    }

    @Override
    public void clear() {
        super.clear();
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public boolean removeTask(int id) {
        boolean res = super.removeTask(id);
        if (res) {
            save();
        }
        return res;
    }

    @Override
    public boolean removeEpic(int id) {
        boolean res = super.removeEpic(id);
        if (res) {
            save();
        }
        return res;

    }

    @Override
    public boolean removeSubtask(int id) {
        boolean res = super.removeSubtask(id);
        if (res) {
            save();
        }
        return res;
    }

    public static void main(String[] args) {
        File file = new File("data.csv");
        TaskManager tm1 = new FileBackedTaskManager(file, Managers.getDefaultHistory());
        LocalDateTime dt = LocalDateTime.of(2025, 1, 1, 0, 0);

        Random rnd = new Random();
        int rndInt = rnd.nextInt(3) + 3;
        for (int i = 0; i < rndInt; i++) {
            tm1.addTask(new Task("Simple task #" + i, "test task #" + i, dt, Duration.ofDays(1)));
            dt = dt.plusDays(1);
        }

        rndInt = rnd.nextInt(3) + 3;
        for (int i = 0; i < rndInt; i++) {
            int epicId = tm1.addEpic(new Epic("Epic #" + i, "test epic #" + i));
            int rndInt1 = rnd.nextInt(3) + 3;
            for (int j = 0; j < rndInt1; j++) {
                tm1.addSubtask(new Subtask("Subtask #" + j, "subtask #" + j
                        + " for epic #" + i, dt, Duration.ofDays(1), epicId));
                dt = dt.plusDays(1);
            }
        }

        TaskManager tm2 = FileBackedTaskManager.loadFromFile(file);
        if (tm1.getTaskList().equals(tm2.getTaskList())
                && tm1.getEpicList().equals(tm2.getEpicList())
                && tm1.getSubtaskList().equals(tm2.getSubtaskList())) {
            System.out.println("file backed task manager works fine");
        } else {
            System.out.println("file backed task manager should be fixed");
        }
    }
}
