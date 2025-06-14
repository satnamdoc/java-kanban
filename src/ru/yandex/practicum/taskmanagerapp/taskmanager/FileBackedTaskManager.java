package ru.yandex.practicum.taskmanagerapp.taskmanager;

import ru.yandex.practicum.taskmanagerapp.exception.ManagerLoadException;
import ru.yandex.practicum.taskmanagerapp.exception.ManagerSaveException;
import ru.yandex.practicum.taskmanagerapp.history.HistoryManager;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.SubTask;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private File dataFile;
    static private final String CSVFILE_HEADER = "id,type,name,status,description,epic";


    public FileBackedTaskManager(File dataFile, HistoryManager historyManager) {
        super(historyManager);
        this.dataFile = dataFile;
    }

    private enum TaskType {
        TASK,
        EPIC,
        SUBTASK
    }

    private String toCSVString(Task task) {
        return String.join(",",
                Integer.toString(task.getId()),
                TaskType.TASK.toString(),
                task.getStatus().toString(),
                task.getName(),
                task.getDescription(),
                ""
        );
    }

    private String toCSVString(Epic epic) {
        return String.join(",",
                Integer.toString(epic.getId()),
                TaskType.EPIC.toString(),
                epic.getStatus().toString(),
                epic.getName(),
                epic.getDescription(),
                ""
        );
    }

    private String toCSVString(SubTask subTask) {
        return String.join(",",
                Integer.toString(subTask.getId()),
                TaskType.SUBTASK.toString(),
                subTask.getStatus().toString(),
                subTask.getName(),
                subTask.getDescription(),
                Integer.toString(subTask.getEpicId())
        );
    }

    private void  save()  {
        try (FileWriter fw = new FileWriter(dataFile); BufferedWriter writer = new BufferedWriter(fw)) {
            writer.write(CSVFILE_HEADER + "\n");
            for(Task task :  super.getTaskList()) {
                writer.write(toCSVString(task) + "\n");
            }
            for(Epic epic :  super.getEpicList()) {
                writer.write(toCSVString(epic) + "\n");
            }
            for(SubTask subTask :  super.getSubTaskList()) {
                writer.write(toCSVString(subTask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Data file save error: " + e.getMessage());
        }
    }

    private static Task fromCSVString(String str) {
        Task task = null;
        //"id,type,name,status,description,epic"
        String fields[] = str.split(",");

        switch (TaskType.valueOf(fields[1])) {
            case TaskType.TASK:
                task = new Task(Integer.parseInt(fields[0]), fields[3], fields[4], TaskStatus.valueOf(fields[2]));
                break;
            case TaskType.EPIC:
                task = new Epic(Integer.parseInt(fields[0]), fields[3], fields[4], TaskStatus.valueOf(fields[2]));
                break;
            case TaskType.SUBTASK:
                task = new SubTask(Integer.parseInt(fields[0]), fields[3], fields[4], TaskStatus.valueOf(fields[2]),
                        Integer.parseInt(fields[5]));
        }

        return task;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return loadFromFile(file, Managers.getDefaultHistory());
    }

    static FileBackedTaskManager loadFromFile(File file, HistoryManager historyManager) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file, historyManager);

        try (FileReader reader = new FileReader(file); BufferedReader br = new BufferedReader(reader)) {
            ArrayList<Task> tasks = new ArrayList<>();
            HashMap<Integer, Epic> epics = new HashMap<>(); // Map helps to bind epic and subtasks
            ArrayList<SubTask> subTasks = new ArrayList<>();

            br.readLine();
            while (br.ready()) {
                String str = br.readLine();
                Task task = fromCSVString(str);
                if (task instanceof Epic) {
                    epics.put(task.getId(), (Epic) task);
                } else if (task instanceof SubTask) {
                    subTasks.add((SubTask) task);
                } else {
                    tasks.add(task);
                }
            }
            //bind epics and subtasks
            for (SubTask subTask : subTasks) {
                epics.get(subTask.getEpicId()).addSubTask(subTask.getId());
            }
            taskManager.load(tasks, new ArrayList<Epic>(epics.values()), subTasks);
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
        return  id;
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
    public int addSubTask(SubTask subTask) {
        int id = super.addSubTask(subTask);
        if (id != Task.NULL_ID) {
            save();
        }
        return id;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean res = super.updateTask(task);
        if (res) {
            save();
        }
        return res;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean res = super.updateEpic(epic);
        if (res) {
            save();
        }
        return res;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        boolean res = super.updateSubTask(subTask);
        if (res) {
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
    public void clearSubTasks() {
        super.clearSubTasks();
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
    public boolean removeSubTask(int id) {
        boolean res = super.removeSubTask(id);
        if (res) {
            save();
        }
        return res;
    }
}
