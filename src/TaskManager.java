import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, SubTask> subTasks;

    private int lastId;
    private static final int START_ID = 100;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        lastId = START_ID;
    }

    private int generateId() {
        return lastId++;
    }

    // проверка корректной связанности эпика и подзадач
    private boolean isEpicValid(Epic epic) {
        for (int subTaskId : epic.getSubTaskIds()) {
            if (!subTasks.containsKey(subTaskId)
                || subTasks.get(subTaskId).getEpicId() != epic.getId()) {
                return false;
            }
        }
        return true;
    }

    // проверка корректной связанности подзадачи и её эпика
    private boolean isSubTaskValid(SubTask subTask) {
        return epics.containsKey(subTask.getEpicId())
                && epics.get(subTask.getEpicId()).getSubTaskIds().contains(subTask.getId());
    }

    public int addTask(Task task) {
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    public int addTask(Epic epic) {
        if(!epic.getSubTaskIds().isEmpty())
            return Task.NULL_ID;

        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    public int addTask(SubTask subTask) {
        if (!epics.containsKey(subTask.getEpicId()))
            return Task.NULL_ID;

        int id = generateId();
        subTask.setId(id);
        subTasks.put(id, subTask);
        epics.get(subTask.getEpicId()).addSubTask(id);
        return id;
    }

    public boolean updateTask(Task task) {
        int id = task.getId();
        if (!tasks.containsKey(id)) {
            return false;
        }
        tasks.put(id, task);
        return true;
    }

    public boolean updateTask(Epic epic) {
        int id = epic.getId();
        if (!epics.containsKey(id)
            || epic.getStatus() != epics.get(id).getStatus()  // статус эпика нельзя менять за пределами класса
            || !isEpicValid(epic)) {                          // проверка целостности структуры эпик-подзадачи
            return false;
        }
        epics.put(id, epic);
        updateEpicStatus(id);   // статус эпика может измениться
        return true;
    }

    public boolean updateTask(SubTask subTask) {
        int id = subTask.getId();
        if (!subTasks.containsKey(id) || !isSubTaskValid(subTask)) {
            return false;
        }
        subTasks.put(id, subTask);
        updateEpicStatus(subTask.getEpicId());
        return true;
    }

    // при получении списков объектов из TaskManager хорошо бы делать клонирование...
    public ArrayList<Task> getTaskList() {
        return new ArrayList<Task>(tasks.values());
    }

    public ArrayList<Epic> getEpicList() {
        return new ArrayList<Epic>(epics.values());
    }

    public ArrayList<SubTask> getSubTaskList() {
        return new ArrayList<SubTask>(subTasks.values());
    }

    public ArrayList<SubTask> getSubTasksOfEpic(int epicId) {
        if (!epics.containsKey(epicId)) {
            return null;
        }

        ArrayList<SubTask> subTasksOfEpic = new ArrayList<>();
        for (int subTaskId : epics.get(epicId).getSubTaskIds()) {
            subTasksOfEpic.add(subTasks.get(subTaskId));
        }
        return  subTasksOfEpic;
    }

    public void clear() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
       epics.clear();
       subTasks.clear();
    }

    public void clearSubTasks() {
        subTasks.clear();
        for(Epic epic : epics.values()) {
            epic.clearSubTasks();
            epic.setStatus(TaskStatus.NEW);
        }
    }

    public Task getTask(int id) {
        if (tasks.containsKey(id))
            return tasks.get(id);
        if (epics.containsKey(id))
            return epics.get(id);
        if (subTasks.containsKey(id))
            return subTasks.get(id);

        return null;
    }

    public boolean removeTask(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            return true;
        }
        if (epics.containsKey(id)) {
            for(int subTaskId : epics.get(id).getSubTaskIds()) {
                subTasks.remove(subTaskId);
            }
            epics.remove(id);
            return true;
        }
        if (subTasks.containsKey(id)) {
            // убираем подзадачу из эпика с проверкой статуса
            int bindingEpicId = subTasks.get(id).getEpicId();
            epics.get(bindingEpicId).removeSubTask(id);
            updateEpicStatus(bindingEpicId);

            subTasks.remove(id);
            return true;
        }
        return false;
    }

    private void updateEpicStatus(int id) {
        Epic epic = epics.get(id);

        //если нет подзадач, то статус NEW
        if (epic.getSubTaskIds().isEmpty()) {
           epic.setStatus(TaskStatus.NEW);
        }

        for (int subTaskId : epic.getSubTaskIds()) {
           // Если есть хоть одна подзадача со статусом отличным от NEW,
           if (subTasks.get(subTaskId).getStatus() != TaskStatus.NEW) {
           // то проверяем есть ли задачи со статусом отличным от DONE
              for (int _subTasksId : epic.getSubTaskIds()) {
                  if (subTasks.get(subTaskId).getStatus() != TaskStatus.DONE) {
                      epic.setStatus(TaskStatus.IN_PROGRESS);
                      return;
                  }
              }
              // все подзадачи имеют статус DONE
              epic.setStatus(TaskStatus.DONE);
              return;
           }
        }
        // все подзадачи имеют статус NEW
        epic.setStatus(TaskStatus.NEW);
    }

    // Вспомогательная функция для отладочной печати
    public void printStatuses() {
        if (tasks.isEmpty()){
            System.out.println("null");
        }
        else {
            ArrayList<Integer> ids = new ArrayList<>(tasks.keySet());
            System.out.print("[" + ids.getFirst() + ":" + tasks.get(ids.getFirst()).getStatus());
            for (int i = 1; i < ids.size(); i++) {
                System.out.print(", " + ids.get(i) + ":" + tasks.get(ids.get(i)).getStatus());
            }
            System.out.println("]");
        }

        if (epics.isEmpty()){
            System.out.println("null");
        }
        else {
            ArrayList<Integer> ids = new ArrayList<>(epics.keySet());
            System.out.print("[" + ids.getFirst() + ":" + epics.get(ids.getFirst()).getStatus());
            for (int i = 1; i < ids.size(); i++) {
                System.out.print(", " + ids.get(i) + ":" + epics.get(ids.get(i)).getStatus());
            }
            System.out.println("]");
        }

        if (subTasks.isEmpty()){
            System.out.println("null");
        }
        else {
            ArrayList<Integer> ids = new ArrayList<>(subTasks.keySet());
            System.out.print("[" + ids.getFirst() + ":" + subTasks.get(ids.getFirst()).getStatus());
            for (int i = 1; i < ids.size(); i++) {
                System.out.print(", " + ids.get(i) + ":" + subTasks.get(ids.get(i)).getStatus());
            }
            System.out.println("]");
        }
    }
}
