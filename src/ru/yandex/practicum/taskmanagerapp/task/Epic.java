package ru.yandex.practicum.taskmanagerapp.task;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subTaskIds  = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(int id, String name, String description, TaskStatus status) {
        super(id, name, description, status);
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
                ""
        );
    }
}
