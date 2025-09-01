package com.workshop.javaaiworkshop.tools.action;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TaskManagementTools {

    public record TaskResult(
            Long taskId,
            String title,
            String status,
            String assigne,
            String message
    ){}


    public enum TaskStatus{
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();

    private final AtomicLong taskIdGenerator = new AtomicLong(1);

    //clase que tiene el task como tal
    private record Task(Long id, String title, String description, String assignee, TaskStatus status){}

    @Tool(description = "Create a new Task with title, decription and assigne")
    public TaskResult createTask(String title, String description, String assignee){
        Long taskId = taskIdGenerator.getAndIncrement();
        Task task = new Task(taskId, title, description, assignee, TaskStatus.PENDING);
        tasks.put(taskId, task);

        //In a real IMplementation: save to the db and send a notification email
        return new TaskResult(taskId, title, "PENDING", assignee, "Task created successfully and assigned to " + assignee + "with Id " + taskId);

    }

    @Tool(description = "Update task status by task ID")
    public TaskResult updateStatus(Long taskId, TaskStatus status){
        Task existingTask = tasks.get(taskId);
        if(existingTask == null){
            return new TaskResult(taskId, "", "ERROR", "", "Task not Found");
        }

        Task updateTask = new Task(existingTask.id(), existingTask.title(), existingTask.description(), existingTask.assignee(), status);
        tasks.put(taskId, updateTask);

        //in a real implementation: update database, send notification ans trigger workflow

        return new TaskResult(taskId, updateTask.title(), status.toString(), updateTask.assignee(), "Task sttaus updated to " + status);
    }

    @Tool(description = "Assign or reassign a task to a different person")
    public TaskResult assignTask(Long taskId, String newAssignee){
        Task existingTask = tasks.get(taskId);
        if(existingTask == null){
            return new TaskResult(taskId, "", "ERROR", "", "Task not Found");
        }

        Task updateTask = new Task(existingTask.id(), existingTask.title(), existingTask.description(), newAssignee, existingTask.status());
        tasks.put(taskId, updateTask);

        //in a real implementation dan: update database, send notification to a new asssignee

        return new TaskResult(taskId, updateTask.title(), updateTask.status().toString(), newAssignee, "Task reassigned to " + newAssignee);


    }


}
