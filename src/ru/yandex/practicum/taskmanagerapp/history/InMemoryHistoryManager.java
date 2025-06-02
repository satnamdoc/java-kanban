package ru.yandex.practicum.taskmanagerapp.history;

import ru.yandex.practicum.taskmanagerapp.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private class Node {
        private final Task task;
        private Node prev;
        private Node next;

        public Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;       // Согласно тз список
            this.next = next;       // должен быть двухсвязанным
        }

        public Node getPrev() {
            return prev;
        }

        public Node getNext() {
            return next;
        }

        public void setPrev(Node prev) {
            this.prev = prev;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }

    private Node head = null;
    private Node tail = null;
    private int listSize = 0;

    private final Map<Integer, Node> helperMap = new HashMap<>();

    private void linkLast(Task task) {
        listSize++;
        if (head == null) {
            head = new Node(task, null, null);
            tail = head;
            return;
        }
        tail.setNext(new Node(task, tail, null));
        tail = tail.getNext();
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        listSize--;
        if (node == head) {
            head = node.getNext();
            if (head == null) { //удаление последнего узла
                tail = null;
            }
        }
        else {
            node.getPrev().setNext(node.getNext());
            if (node == tail) {
                tail = node.prev;
            }
            else {
                node.getNext().setPrev(node.getPrev());
            }
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>(listSize);
        if(listSize == 0){
            return tasks;
        }

        Node node = head;
        while (node != null) {
            tasks.add(node.task);
            node = node.next;
        }

        return tasks;
    }

    @Override
    public void add(Task task){
        if (task == null) {
            return;
        }
        remove(task.getId());
        linkLast(task);
        helperMap.put(task.getId(), tail);
    }

    @Override
    public void remove(int id) {
        removeNode(helperMap.remove(id));
    }

    @Override
    public List<Task> getHistory(){
        return getTasks();
    }
}

