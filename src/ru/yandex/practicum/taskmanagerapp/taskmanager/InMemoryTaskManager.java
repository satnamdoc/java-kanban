package ru.yandex.practicum.taskmanagerapp.taskmanager;

import ru.yandex.practicum.taskmanagerapp.history.HistoryManager;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.SubTask;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();

    private static final int START_ID = 100;
    private int lastId = START_ID;

    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int generateId() {
        return lastId++;
    }

    @Override
    public int addTask(Task task) {
        if (task == null) {
            return Task.NULL_ID;
        }
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        if (epic == null
                || !epic.getSubTaskIds().isEmpty()) {
            return Task.NULL_ID;
        }
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public int addSubTask(SubTask subTask) {
        if (subTask == null
                || !epics.containsKey(subTask.getEpicId())) {
            return Task.NULL_ID;
        }

        int id = generateId();
        subTask.setId(id);
        subTasks.put(id, subTask);
        epics.get(subTask.getEpicId()).addSubTask(id);
        updateEpicStatus(subTask.getEpicId());
        return id;
    }

    @Override
    public boolean updateTask(Task task) {
        if (task == null) {
            return false;
        }
        return tasks.replace(task.getId(), task) != null;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (epic == null) {
            return false;
        }
        int id = epic.getId();
        if (!epics.containsKey(id)
                || epic.getStatus() != epics.get(id).getStatus()) {  // статус эпика нельзя менять за пределами класса
            return false;
        }
        epics.put(id, epic);
        updateEpicStatus(id);   // статус эпика может измениться
        return true;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        if (subTask == null) {
            return false;
        }
        if (subTasks.replace(subTask.getId(), subTask) == null) {
            return false;
        }
        updateEpicStatus(subTask.getEpicId());
        return true;
    }

    // при получении списков объектов из TaskManager хорошо бы делать клонирование...
    @Override
    public List<Task> getTaskList() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpicList() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getSubTaskList() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public List<SubTask> getSubTasksOfEpic(int epicId) {
        if (!epics.containsKey(epicId)) {
            return null;
        }

        ArrayList<SubTask> subTasksOfEpic = new ArrayList<>();
        ArrayList<Integer> subTaskIds= epics.get(epicId).getSubTaskIds();
        for (int subTaskId : subTaskIds) {
            subTasksOfEpic.add(subTasks.get(subTaskId));
        }
        return  subTasksOfEpic;
    }

    @Override
    public void clear() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void clearTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void clearSubTasks() {
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
        }
        subTasks.clear();
        for(Epic epic : epics.values()) {
            epic.clearSubTasks();
            epic.setStatus(TaskStatus.NEW);
        }
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public boolean removeTask(int id) {
        historyManager.remove(id);
        return tasks.remove(id) != null;
    }

    @Override
    public boolean removeEpic(int id) {
        if (epics.containsKey(id)) {
            ArrayList <Integer> subTaskIds = epics.get(id).getSubTaskIds();
            for (int subTaskId : subTaskIds) {
                subTasks.remove(subTaskId);
                historyManager.remove(subTaskId);
            }
            epics.remove(id);
            historyManager.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeSubTask(int id) {
        if (subTasks.containsKey(id)) {
            // убираем подзадачу из эпика с проверкой статуса
            int bindingEpicId = subTasks.get(id).getEpicId();
            epics.get(bindingEpicId).removeSubTask(id);
            updateEpicStatus(bindingEpicId);
            subTasks.remove(id);
            historyManager.remove(id);
            return true;
        }
        return false;
    }

    private void updateEpicStatus(int id) {
        Epic epic = epics.get(id);

        for (int subTaskId : epic.getSubTaskIds()) {
            // Если есть хоть одна подзадача со статусом отличным от NEW,
            if (subTasks.get(subTaskId).getStatus() != TaskStatus.NEW) {
                // то проверяем есть ли подзадачи со статусом отличным от DONE
                for (int _subTaskId : epic.getSubTaskIds()) {
                    if (subTasks.get(_subTaskId).getStatus() != TaskStatus.DONE) {
                        epic.setStatus(TaskStatus.IN_PROGRESS);
                        return;
                    }
                }
                // все подзадачи имеют статус DONE
                epic.setStatus(TaskStatus.DONE);
                return;
            }
        }
        // все подзадачи имеют статус NEW
        epic.setStatus(TaskStatus.NEW);
    }

    public List<Task> getHistory(){
        return historyManager.getHistory();
    }

}