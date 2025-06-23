package ru.yandex.practicum.taskmanagerapp.history;

import org.junit.jupiter.api.BeforeEach;

class InMemoryHistoryManagerTest extends HistoryManagerTest<InMemoryHistoryManager> {
    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager();
    }
}