package ru.yandex.practicum.taskmanagerapp.taskmanager;

import ru.yandex.practicum.taskmanagerapp.exception.ManagerLoadException;
import ru.yandex.practicum.taskmanagerapp.history.HistoryManager;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.SubTask;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();

    private static final int START_ID = 100;
    private int lastId = START_ID;

    private final HistoryManager historyManager;

    private final TreeSet<? super Task> tasksSortedByStartTime =
            new TreeSet<>(Comparator.comparing(
                    t -> t.getStartTime()
                            .orElseThrow(() -> new NullPointerException("Start time attribute is null"))
                    )
                );

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
        if (task.getStartTime().isPresent()) {
            tasksSortedByStartTime.add(task);
        }
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
        updateEpicTiming(subTask.getEpicId());
        if (subTask.getStartTime().isPresent()){
            tasksSortedByStartTime.add(subTask);
        }
        return id;
    }

    @Override
    public boolean updateTask(Task task) {
        if (task == null) {
            return false;
        }
        Task oldTask = tasks.replace(task.getId(), task);
        if (oldTask == null) {
            return false;
        }
        tasksSortedByStartTime.remove(oldTask);
        if (task.getStartTime().isPresent()){
            tasksSortedByStartTime.add(task);
        }
        return true;
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
        updateEpicTiming(id);
        return true;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        if (subTask == null) {
            return false;
        }
        SubTask oldSubTask = subTasks.replace(subTask.getId(), subTask);
        if (oldSubTask == null) {
            return false;
        }
        updateEpicStatus(subTask.getEpicId());
        updateEpicTiming(subTask.getEpicId());
        tasksSortedByStartTime.remove(oldSubTask);
        if (subTask.getStartTime().isPresent()){
            tasksSortedByStartTime.add(subTask);
        }
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
        epics.get(epicId).getSubTaskIds().stream().map(subTasks::get).forEach(subTasksOfEpic::add);

        return subTasksOfEpic;
    }

    @Override
    public void clear() {
        clearTasks();
        clearEpics();
    }

    @Override
    public void clearTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().stream()
                .filter(task -> task.getStartTime().isPresent())
                .forEach(tasksSortedByStartTime::remove);
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        clearSubTasks();
        epics.keySet().forEach(historyManager::remove);
        epics.clear();
    }

    @Override
    public void clearSubTasks() {
        subTasks.keySet().forEach(historyManager::remove);
        subTasks.values().stream()
                .filter(subTask -> subTask.getStartTime().isPresent())
                .forEach(tasksSortedByStartTime::remove);
        subTasks.clear();
        epics.values().stream()
                .peek(Epic::clearSubTasks)
                .forEach(epic -> {
                    epic.setStatus(TaskStatus.NEW);
                    epic.setStartTime(null);
                    epic.setDuration(Duration.ZERO);
                    epic.setEndTime(null);
                });
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
        Task task = tasks.remove(id);
        if (task == null)
            return false;
        tasksSortedByStartTime.remove(task);
        return true;
    }

    @Override
    public boolean removeEpic(int id) {
        if (!epics.containsKey(id)) {
            return false;
        }
        epics.get(id).getSubTaskIds().forEach(this::removeSubTask);
        epics.remove(id);
        historyManager.remove(id);
        return true;
    }

    @Override
    public boolean removeSubTask(int id) {
        if (!subTasks.containsKey(id)) {
            return false;
        }
        int bindingEpicId = subTasks.get(id).getEpicId();
        epics.get(bindingEpicId).removeSubTask(id);
        updateEpicStatus(bindingEpicId);
        updateEpicTiming(bindingEpicId);
        tasksSortedByStartTime.remove(subTasks.remove(id));
        historyManager.remove(id);
        return true;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        Set<TaskStatus> statuses = epic.getSubTaskIds().stream()
                .map(this::getSubTask).map(SubTask::getStatus)
                .collect((Collectors.toSet()));

        if (statuses.isEmpty() || statuses.equals(Set.of(TaskStatus.NEW))) {
            epic.setStatus(TaskStatus.NEW);
        } else if (statuses.equals(Set.of(TaskStatus.DONE))) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void updateEpicTiming(int epicId) {
        final Epic epic = epics.get(epicId);

        epic.getSubTaskIds().stream()
                .map(id -> subTasks.get(id).getStartTime())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(LocalDateTime::compareTo)
                .ifPresentOrElse(epic::setStartTime, () -> epic.setStartTime(null));

        epic.getSubTaskIds().stream()
                .map(id -> subTasks.get(id).getEndTime())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(LocalDateTime::compareTo)
                .ifPresentOrElse(epic::setEndTime, () -> epic.setEndTime(null));

        epic.setDuration(epic.getSubTaskIds().stream()
                .map(id -> subTasks.get(id).getDuration())
                .reduce(Duration.ZERO, Duration::plus));
    }


    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void load(List<Task> tasks, List<Epic> epics, List<SubTask> subTasks) {
        if (lastId != START_ID) {
            throw new ManagerLoadException("Task manager object must be empty");
        }

        tasks.forEach(task -> this.tasks.put(task.getId(), task));
        subTasks.forEach(subTask -> this.subTasks.put(subTask.getId(), subTask));
        epics.forEach(epic -> this.epics.put(epic.getId(), epic));

        Stream.of(this.tasks.keySet(), this.epics.keySet(), this.subTasks.keySet()).flatMap(Set::stream)
                .mapToInt(Integer::intValue).max()
                .ifPresentOrElse(id -> lastId = id, () -> lastId = 0);

        tasksSortedByStartTime.addAll(tasks.stream().filter(t -> t.getStartTime().isPresent()).toList());
        tasksSortedByStartTime.addAll(subTasks.stream().filter(st -> st.getStartTime().isPresent()).toList());
    }

    @Override
    public List<? super Task> getPrioritizedTasks() {
        return new ArrayList<>(tasksSortedByStartTime);
    }
}