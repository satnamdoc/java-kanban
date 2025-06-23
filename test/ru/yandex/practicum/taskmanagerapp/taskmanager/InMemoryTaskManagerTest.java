package ru.yandex.practicum.taskmanagerapp.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Epic;
import ru.yandex.practicum.taskmanagerapp.task.SubTask;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.taskmanagerapp.task.Task.NULL_ID;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
    }
}