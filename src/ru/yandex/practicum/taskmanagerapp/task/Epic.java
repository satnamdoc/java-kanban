package ru.yandex.practicum.taskmanagerapp.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime = null;

    public Epic(String name, String description) {
        super(name, description, null, Duration.ZERO);
    }

    public Epic(int id, String name, String description, TaskStatus status, LocalDateTime startTime,
                Duration duration, LocalDateTime endTime, List<Integer> subtaskIds) {
        super(id, name, description, status, startTime, duration);
        this.endTime = endTime;
        this.subtaskIds.addAll(subtaskIds);
    }

    protected void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description length=" + description.length() +
                ", start time=" + ((startTime == null) ? "UNKNOWN" : startTime.format(DATE_TIME_FORMATTER)) +
                ", duration=" + duration.toDaysPart() + "d " + duration.toHoursPart() + "h "
                + duration.toMinutesPart() + "m" +
                ", subtaskIds=" + subtaskIds +
                '}';
    }

    @Override
    public String toCSVString() {
        return String.join(",",
                Integer.toString(getId()),
                TaskType.EPIC.toString(),
                getStatus().toString(),
                getName(),
                getDescription(),
                ((startTime == null) ? "UNKNOWN" : startTime.format(DATE_TIME_FORMATTER)),
                Long.toString(duration.toMinutes()),
                ""
        );
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.ofNullable(endTime);
    }
}

