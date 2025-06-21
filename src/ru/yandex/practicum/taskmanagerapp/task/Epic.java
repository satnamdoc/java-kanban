package ru.yandex.practicum.taskmanagerapp.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class Epic extends Task {
    private final ArrayList<Integer> subTaskIds  = new ArrayList<>();
    private LocalDateTime endTime = null;

    public Epic(String name, String description) {
        super(name, description, null, Duration.ZERO);
    }

    public Epic(int id, String name, String description, TaskStatus status,
                LocalDateTime startTime, Duration duration) {
        super(id, name, description, status, startTime, duration);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void addSubTask(int subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void removeSubTask(int subTaskId) {
        subTaskIds.remove((Integer)subTaskId);
    }

    public void clearSubTasks() {
        subTaskIds.clear();
    }

    public ArrayList<Integer> getSubTaskIds() {
        return new ArrayList<>(subTaskIds);
    }


    @Override
    public String toString() {
           return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description length=" + description.length() +
                ", start time=" + ((startTime == null)?"UNKNOWN":startTime.format(DATE_TIME_FORMATTER)) +
                ", duration=" + duration.toDaysPart() + "d " + duration.toHoursPart() + "h "
                    + duration.toMinutesPart() + "m" +
               ", subTaskIds=" + subTaskIds +
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
                ((startTime == null)?"UNKNOWN":startTime.format(DATE_TIME_FORMATTER)),
                Long.toString(duration.toMinutes()),
                ""
        );
    }

    @Override
    public Optional<LocalDateTime>  getEndTime() {
        return Optional.ofNullable(endTime);
    }

}
