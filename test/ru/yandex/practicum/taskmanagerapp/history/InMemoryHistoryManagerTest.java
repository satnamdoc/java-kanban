package ru.yandex.practicum.taskmanagerapp.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanagerapp.task.Task;
import ru.yandex.practicum.taskmanagerapp.task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest extends HistoryManagerTest{
    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager();
    }
}