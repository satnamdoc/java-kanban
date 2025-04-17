package ru.yandex.practicum.taskmanagerapp.core;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subTaskIds  = new ArrayList<>();;

    public Epic(String name, String description) {
        super(name, description);
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
}
