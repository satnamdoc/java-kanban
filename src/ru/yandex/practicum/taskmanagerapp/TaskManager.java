package ru.yandex.practicum.taskmanagerapp;

import ru.yandex.practicum.taskmanagerapp.core.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();

    private static final int START_ID = 100;
    private int lastId = START_ID;

    public TaskManager() {}

    private int generateId() {
        return lastId++;
    }

    public int addTask(Task task) {
        if (task == null) {
            return Task.NULL_ID;
        }
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

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

    public boolean updateTask(Task task) {
        if (task == null) {
            return false;
        }
        int id = task.getId();
        if (!tasks.containsKey(id)) {   // как здесь использовать putIfAbsent не придумал (,
            return false;               // скорее нужно что-то типа putIfNotAbsent
        }
        tasks.put(id, task);
        return true;
    }

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

    public boolean updateSubTask(SubTask subTask) {
        if (subTask == null) {
            return false;
        }
        int id = subTask.getId();
        if (!subTasks.containsKey(id)) {
            return false;
        }
        subTasks.put(id, subTask);
        updateEpicStatus(subTask.getEpicId());
        return true;
    }

    // при получении списков объектов из TaskManager хорошо бы делать клонирование...
    public ArrayList<Task> getTaskList() {
        return new ArrayList<Task>(tasks.values());
    }

    public ArrayList<Epic> getEpicList() {
        return new ArrayList<Epic>(epics.values());
    }

    public ArrayList<SubTask> getSubTaskList() {
        return new ArrayList<SubTask>(subTasks.values());
    }

    public ArrayList<SubTask> getSubTasksOfEpic(int epicId) {
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

    public void clear() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
       epics.clear();
       subTasks.clear();
    }

    public void clearSubTasks() {
        subTasks.clear();
        for(Epic epic : epics.values()) {
            epic.clearSubTasks();
            epic.setStatus(TaskStatus.NEW);
        }
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Task getEpic(int id) {
        return epics.get(id);
    }

    public Task getSubTask(int id) {
        return subTasks.get(id);
    }

    public boolean removeTask(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            return true;
        }
        return false;
    }

    public boolean removeEpic(int id) {
        if (epics.containsKey(id)) {
            ArrayList <Integer> subTaskIds = epics.get(id).getSubTaskIds();
            for (int subTaskId : subTaskIds) {
                subTasks.remove(subTaskId);
            }
            epics.remove(id);
            return true;
        }
        return false;
    }

    public boolean removeSubTask(int id) {
        if (subTasks.containsKey(id)) {
            // убираем подзадачу из эпика с проверкой статуса
            int bindingEpicId = subTasks.get(id).getEpicId();
            epics.get(bindingEpicId).removeSubTask(id);
            updateEpicStatus(bindingEpicId);
            subTasks.remove(id);
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

    // Вспомогательная функция для отладочной печати
    public void printStatuses() {
        if (tasks.isEmpty()){
            System.out.println("null");
        }
        else {
            ArrayList<Integer> ids = new ArrayList<>(tasks.keySet());
            System.out.print("[" + ids.getFirst() + ":" + tasks.get(ids.getFirst()).getStatus());
            for (int i = 1; i < ids.size(); i++) {
                System.out.print(", " + ids.get(i) + ":" + tasks.get(ids.get(i)).getStatus());
            }
            System.out.println("]");
        }

        if (epics.isEmpty()){
            System.out.println("null");
        }
        else {
            ArrayList<Integer> ids = new ArrayList<>(epics.keySet());
            System.out.print("[" + ids.getFirst() + ":" + epics.get(ids.getFirst()).getStatus());
            for (int i = 1; i < ids.size(); i++) {
                System.out.print(", " + ids.get(i) + ":" + epics.get(ids.get(i)).getStatus());
            }
            System.out.println("]");
        }

        if (subTasks.isEmpty()){
            System.out.println("null");
        }
        else {
            ArrayList<Integer> ids = new ArrayList<>(subTasks.keySet());
            System.out.print("[" + ids.getFirst() + ":" + subTasks.get(ids.getFirst()).getStatus());
            for (int i = 1; i < ids.size(); i++) {
                System.out.print(", " + ids.get(i) + ":" + subTasks.get(ids.get(i)).getStatus());
            }
            System.out.println("]");
        }
    }
}
