package ru.yandex.practicum.taskmanagerapp.taskmanager;

import ru.yandex.practicum.taskmanagerapp.exception.*;
import ru.yandex.practicum.taskmanagerapp.exception.NullItemException;
import ru.yandex.practicum.taskmanagerapp.history.HistoryManager;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.Subtask;
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
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private static final int START_ID = 100;
    private int lastId = START_ID;

    private final HistoryManager historyManager;

    // Structure to sort task and subtasks by start time for task priority management
    private final TreeSet<? super Task> tasksSortedByStartTime =
            new TreeSet<>(Comparator.comparing(
                    t -> t.getStartTime()
                            .orElseThrow(() -> new java.lang.NullPointerException("Start time attribute is null"))
            )
            );
    // Structure stores 1-hour intervals and task ids
    private final HashMap<Long, HashSet<Integer>> taskSchedule = new HashMap<>();

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int generateId() {
        return lastId++;
    }

    @Override
    public int addTask(Task task) {
        if (task == null) {
            throw new NullItemException();
        }
        if (isTimeConflictQ(task)) {
            throw new TimeConflictException();
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
        if (epic == null) {
            throw new NullItemException();
        }
        if (!epic.getSubtaskIds().isEmpty()) {
            throw new InconsistentDataException();
        }
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new NullItemException();
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new InconsistentDataException();
        }
        if (isTimeConflictQ(subtask)) {
            throw new TimeConflictException();
        }
        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epics.get(subtask.getEpicId()).addSubtask(id);
        updateEpicInternalState(epics.get(subtask.getEpicId()));
        if (subtask.getStartTime().isPresent()) {
            tasksSortedByStartTime.add(subtask);
            addToTaskShedule(subtask);
        }
        return id;
    }

    @Override
    public Task updateTask(Task task) {
        if (task == null) {
            throw new NullItemException();
        }
        Task oldTask = tasks.get(task.getId());
        if (oldTask == null) {
            throw new NotFoundException();
        }
        removeFromTaskShedule(oldTask);
        if (isTimeConflictQ(task)) {
            addToTaskShedule(oldTask);
            throw new TimeConflictException();
        }

        tasks.replace(task.getId(), task);

        tasksSortedByStartTime.remove(oldTask);
        if (task.getStartTime().isPresent()) {
            tasksSortedByStartTime.add(task);
            addToTaskShedule(task);
        }

        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epic == null) {
            throw new NullItemException();
        }
        if (epics.replace(epic.getId(), epic) == null) {
            throw new NotFoundException();
        }
        return updateEpicInternalState(epic);
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new NullItemException();
        }
        Subtask oldSubtask;
        if ((oldSubtask = subtasks.get(subtask.getId())) == null) {
            throw new NotFoundException();
        }

        removeFromTaskShedule(oldSubtask);
        if (isTimeConflictQ(subtask)) {
            addToTaskShedule(oldSubtask);
            throw new TimeConflictException();
        }

        subtasks.replace(subtask.getId(), subtask);
        updateEpicInternalState(epics.get(subtask.getEpicId()));

        tasksSortedByStartTime.remove(oldSubtask);
        if (subtask.getStartTime().isPresent()) {
            tasksSortedByStartTime.add(subtask);
            addToTaskShedule(subtask);
        }
        return subtask;
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
    public List<Subtask> getSubtaskList() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        if (!epics.containsKey(epicId)) {
            throw new NotFoundException();
        }

        ArrayList<Subtask> subtasksOfEpic = new ArrayList<>();
        epics.get(epicId).getSubtaskIds().stream().map(subtasks::get).forEach(subtasksOfEpic::add);

        return subtasksOfEpic;
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
        clearSubtasks();
        epics.keySet().forEach(historyManager::remove);
        epics.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().stream()
                .filter(subtask -> subtask.getStartTime().isPresent())
                .peek(tasksSortedByStartTime::remove)
                .forEach(this::removeFromTaskShedule);
        subtasks.clear();
        epics.values().stream()
                .peek(Epic::clearSubtasks)
                .forEach(this::updateEpicInternalState);
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException();
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException();
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException();
        }
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Task removeTask(int id) {
        historyManager.remove(id);
        Task task = tasks.remove(id);
        if (task == null)
            throw new NotFoundException();
        tasksSortedByStartTime.remove(task);
        removeFromTaskShedule(task);
        return task;
    }

    @Override
    public Epic removeEpic(int id) {
        if (!epics.containsKey(id)) {
            throw new NotFoundException();
        }
        epics.get(id).getSubtaskIds().forEach(this::removeSubtask);
        historyManager.remove(id);
        return epics.remove(id);
    }

    @Override
    public Subtask removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException();
        }
        Epic bindingEpic = epics.get(subtask.getEpicId());
        bindingEpic.removeSubtask(id);
        updateEpicInternalState(bindingEpic);
        tasksSortedByStartTime.remove(subtask);
        removeFromTaskShedule(subtask);
        historyManager.remove(id);
        return subtasks.remove(id);
    }

    private Epic updateEpicStatus(Epic epic) {
        if (epic == null) {
            return null;
        }
        Set<TaskStatus> statuses = epic.getSubtaskIds().stream()
                .map(subtasks::get).map(Subtask::getStatus)
                .collect((Collectors.toSet()));

        TaskStatus newStatus = TaskStatus.IN_PROGRESS;
        if (statuses.isEmpty() || statuses.equals(Set.of(TaskStatus.NEW))) {
            newStatus = TaskStatus.NEW;
        } else if (statuses.equals(Set.of(TaskStatus.DONE))) {
            newStatus = TaskStatus.DONE;
        }

        if (epic.getStatus() != newStatus) {
            Epic newEpic = new Epic(epic.getId(), epic.getName(), epic.getDescription(), newStatus,
                    epic.getStartTime().orElse(null), epic.getDuration(), epic.getEndTime().orElse(null),
                    epic.getSubtaskIds());
            epics.replace(epic.getId(), newEpic);
            return newEpic;
        }
        return epic;
    }

    private Epic updateEpicTiming(Epic epic) {
        if (epic == null) {
            return null;
        }
        LocalDateTime newStartTime = epic.getSubtaskIds().stream()
                .map(id -> subtasks.get(id).getStartTime())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime newEndTime = epic.getSubtaskIds().stream()
                .map(id -> subtasks.get(id).getEndTime())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (!epic.getStartTime().equals(Optional.ofNullable(newStartTime))
                || !epic.getEndTime().equals(Optional.ofNullable(newEndTime))) {
            Epic newEpic = new Epic(epic.getId(), epic.getName(), epic.getDescription(), epic.getStatus(),
                    newStartTime,
                    (newStartTime != null) ? Duration.between(newStartTime, newEndTime) : Duration.ZERO,
                    newEndTime,
                    epic.getSubtaskIds());
            epics.replace(epic.getId(), newEpic);
            return newEpic;
        }
        return epic;
    }

    private Epic updateEpicInternalState(Epic epic) {
        return updateEpicTiming(updateEpicStatus(epic));
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void load(List<Task> tasks, List<Epic> epics, List<Subtask> subtasks) {
        if (lastId != START_ID) {
            throw new ManagerLoadException("Task manager object must be empty");
        }

        tasks.forEach(task -> this.tasks.put(task.getId(), task));
        subtasks.forEach(subtask -> this.subtasks.put(subtask.getId(), subtask));
        epics.forEach(epic -> {
            this.epics.put(epic.getId(), epic);
            updateEpicInternalState(epic);
        });

        Stream.of(this.tasks.keySet(), this.epics.keySet(), this.subtasks.keySet()).flatMap(Set::stream)
                .mapToInt(Integer::intValue).max()
                .ifPresentOrElse(id -> lastId = id, () -> lastId = 0);

        tasks.stream().filter(t -> t.getStartTime().isPresent())
                .peek(tasksSortedByStartTime::add)
                .forEach(this::addToTaskShedule);
        subtasks.stream().filter(t -> t.getStartTime().isPresent())
                .peek(tasksSortedByStartTime::add)
                .forEach(this::addToTaskShedule);
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
                    return subtasks.get(id);
                })
                .anyMatch(t -> isTimeConflict(task, t));
    }
}