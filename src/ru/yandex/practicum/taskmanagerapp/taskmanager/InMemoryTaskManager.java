package ru.yandex.practicum.taskmanagerapp.taskmanager;

import ru.yandex.practicum.taskmanagerapp.exception.ManagerLoadException;
import ru.yandex.practicum.taskmanagerapp.history.HistoryManager;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.SubTask;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();

    private static final int START_ID = 100;
    private int lastId = START_ID;

    private final HistoryManager historyManager;

    // Structure to sort task and subtasks by start time for task priority management
    private final TreeSet<? super Task> tasksSortedByStartTime =
            new TreeSet<>(Comparator.comparing(
                    t -> t.getStartTime()
                            .orElseThrow(() -> new NullPointerException("Start time attribute is null"))
            )
            );
    // Structure stores a 1-hour intervals and task ids
    private final HashMap<Long, HashSet<Integer>> taskSchedule = new HashMap<>();

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int generateId() {
        return lastId++;
    }

    @Override
    public int addTask(Task task) {
        if (task == null || isTimeConflictQ(task)) {
            return Task.NULL_ID;
        }
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        if (task.getStartTime().isPresent()) {
            tasksSortedByStartTime.add(task);
            addToTaskShedule(task);
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
                || !epics.containsKey(subTask.getEpicId())
                || isTimeConflictQ(subTask)) {
            return Task.NULL_ID;
        }
        int id = generateId();
        subTask.setId(id);
        subTasks.put(id, subTask);
        epics.get(subTask.getEpicId()).addSubTask(id);
        updateEpicStatus(epics.get(subTask.getEpicId()));
        updateEpicTiming(epics.get(subTask.getEpicId()));
        if (subTask.getStartTime().isPresent()) {
            tasksSortedByStartTime.add(subTask);
            addToTaskShedule(subTask);
        }
        return id;
    }

    @Override
    public boolean updateTask(Task task) {
        if (task == null) {
            return false;
        }
        Task oldTask = tasks.get(task.getId());
        if (oldTask == null) {
            return false;
        }
        removeFromTaskShedule(oldTask);
        if (isTimeConflictQ(task)) {
            addToTaskShedule(oldTask);
            return false;
        }
        tasks.replace(task.getId(), task);
        tasksSortedByStartTime.remove(oldTask);
        if (task.getStartTime().isPresent()) {
            tasksSortedByStartTime.add(task);
            addToTaskShedule(task);
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
        updateEpicStatus(epic);   // статус эпика может измениться
        updateEpicTiming(epic);
        return true;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        if (subTask == null) {
            return false;
        }
        SubTask oldSubTask = subTasks.get(subTask.getId());
        if (oldSubTask == null) {
            return false;
        }
        removeFromTaskShedule(oldSubTask);
        if (isTimeConflictQ(subTask)) {
            addToTaskShedule(oldSubTask);
            return false;
        }

        subTasks.replace(subTask.getId(), subTask);
        updateEpicStatus(epics.get(subTask.getEpicId()));
        updateEpicTiming(epics.get(subTask.getEpicId()));
        tasksSortedByStartTime.remove(oldSubTask);
        if (subTask.getStartTime().isPresent()) {
            tasksSortedByStartTime.add(subTask);
            addToTaskShedule(subTask);
        }
        return true;
    }

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
    public List<SubTask> getEpicSubTasks(int epicId) {
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
                .peek(tasksSortedByStartTime::remove)
                .forEach(this::removeFromTaskShedule);
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
                .peek(tasksSortedByStartTime::remove)
                .forEach(this::removeFromTaskShedule);
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
    public Optional<Task> getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return Optional.ofNullable(epic);
    }

    @Override
    public Optional<SubTask> getSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            historyManager.add(subTask);
        }
        return Optional.ofNullable(subTask);
    }

    @Override
    public boolean removeTask(int id) {
        historyManager.remove(id);
        Task task = tasks.remove(id);
        if (task == null)
            return false;
        tasksSortedByStartTime.remove(task);
        removeFromTaskShedule(task);
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
        SubTask subTask = subTasks.get(id);
        if (subTask == null) {
            return false;
        }
        Epic bindingEpic = epics.get(subTask.getEpicId());
        bindingEpic.removeSubTask(id);
        updateEpicStatus(bindingEpic);
        updateEpicTiming(bindingEpic);
        subTasks.remove(id);
        tasksSortedByStartTime.remove(subTask);
        removeFromTaskShedule(subTask);
        historyManager.remove(id);
        return true;
    }

    private void updateEpicStatus(Epic epic) {
        Set<TaskStatus> statuses = epic.getSubTaskIds().stream()
                .map(subTasks::get).map(SubTask::getStatus)
                .collect((Collectors.toSet()));

        if (statuses.isEmpty() || statuses.equals(Set.of(TaskStatus.NEW))) {
            epic.setStatus(TaskStatus.NEW);
        } else if (statuses.equals(Set.of(TaskStatus.DONE))) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void updateEpicTiming(Epic epic) {
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
        epics.forEach(epic -> {
            this.epics.put(epic.getId(), epic);
            updateEpicTiming(epic);
        });

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

    /// //////////////////////////////////
    /// 1st implementation of time conflict check
    private static boolean isTimeConflict(Task task1, Task task2) {
        Optional<LocalDateTime> ost1 = task1.getStartTime();
        Optional<LocalDateTime> ost2 = task2.getStartTime();

        if (ost1.isEmpty() || ost2.isEmpty()) {
            return false;
        }
        LocalDateTime st1 = ost1.get();
        LocalDateTime st2 = ost2.get();
        LocalDateTime et1 = task1.getEndTime().get();
        LocalDateTime et2 = task2.getEndTime().get();

        return (st1.isBefore(st2) && et1.isAfter(st2))
                || (st2.isBefore(st1) && et2.isAfter(st1))
                || st1.equals(st2);
    }

    // complexity O(n)
    private boolean isTimeConflict(Task task) {
        return getPrioritizedTasks().stream().anyMatch(t -> isTimeConflict((Task) t, task));
    }

    /// //////////////////////////////////
    /// 2nd implementation of time conflict check
    private LongStream getTaskSheduleIntervals(Task task) {
        final long intervalLength = 60 * 60;
        long firstInterval = task.getStartTime().get().toEpochSecond(ZoneOffset.UTC) / intervalLength;
        long lastInterval = (task.getEndTime().get().toEpochSecond(ZoneOffset.UTC) - 1) / intervalLength;
        return LongStream.range(firstInterval, lastInterval + 1);
    }

    private void addToTaskShedule(Task task) {
        if (task.getStartTime().isEmpty()) {
            return;
        }
        Integer id = task.getId();
        getTaskSheduleIntervals(task)
                .forEach(i -> taskSchedule.computeIfAbsent(i, k -> new HashSet<>()).add(id));
    }

    private void removeFromTaskShedule(Task task) {
        if (task.getStartTime().isEmpty()) {
            return;
        }
        Integer id = task.getId();
        getTaskSheduleIntervals(task)
                .peek(i -> taskSchedule.get(i).remove(id))    // remove id from set
                .filter(i -> taskSchedule.get(i).isEmpty())
                .forEach(taskSchedule::remove);               // remove empty sets
    }

    // complexity O(1)
    private boolean isTimeConflictQ(Task task) {
        if (task.getStartTime().isEmpty()) {
            return false;
        }
        return getTaskSheduleIntervals(task)
                .mapToObj(taskSchedule::get)
                .filter(Objects::nonNull)
                .flatMap(HashSet::stream)
                .map(id -> {
                    if (tasks.get(id) != null) {
                        return tasks.get(id);
                    }
                    return subTasks.get(id);
                })
                .anyMatch(t -> isTimeConflict(task, t));
    }
}