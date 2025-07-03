package ru.yandex.practicum.taskmanagerapp.task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, LocalDateTime startTime,
                   Duration duration, int epicId) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status,
                   LocalDateTime startTime, Duration duration, int epicId) {
        super(id, name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description length=" + description.length() +
                ", start time=" + ((startTime == null) ? "UNKNOWN" : startTime.format(DATE_TIME_FORMATTER)) +
                ", duration=" + duration.toDaysPart() + "d " + duration.toHoursPart() + "h "
                + duration.toMinutesPart() + "m" +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public String toCSVString() {
        return String.join(",",
                Integer.toString(getId()),
                TaskType.SUBTASK.toString(),
                getStatus().toString(),
                getName(),
                getDescription(),
                ((startTime == null) ? "UNKNOWN" : startTime.format(DATE_TIME_FORMATTER)),
                Long.toString(duration.toMinutes()),
                Integer.toString(getEpicId())
        );
    }
}
