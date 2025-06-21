package ru.yandex.practicum.taskmanagerapp.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class Task {
    protected int id;
    protected String name;
    protected TaskStatus status;
    protected String description;
    protected LocalDateTime startTime;
    protected Duration duration;

    public static final int NULL_ID = 0;
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this.id = NULL_ID;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(int id, String name, String description, TaskStatus status,
                LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Optional<LocalDateTime>  getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        if (task.id == Task.NULL_ID) return false;                  //задачи с непроинициализированным id несравнимы
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description length=" + description.length() +
                ", start time=" + ((startTime == null)?"UNKNOWN":startTime.format(DATE_TIME_FORMATTER)) +
                ", duration=" + duration.toDaysPart() + "d " + duration.toHoursPart() + "h "
                + duration.toMinutesPart() + "m" +
                '}';
    }

    public String toCSVString() {
        return String.join(",",
                Integer.toString(getId()),
                TaskType.TASK.toString(),
                getStatus().toString(),
                getName(),
                getDescription(),
                startTime.format(DATE_TIME_FORMATTER),
                Long.toString(duration.toMinutes()),
                ""
        );
    }

    public Optional<LocalDateTime>  getEndTime() {
        return getStartTime().map(st -> st.plus(duration));
    }
}
