package com.javaproject.java_project.controller;

import com.javaproject.java_project.model.Task;
import com.javaproject.java_project.model.User;
import com.javaproject.java_project.repositories.UsersRepository;
import com.javaproject.java_project.repositories.CoursesRepository;
import com.javaproject.java_project.request.EditedTaskRequest;
import com.javaproject.java_project.request.NewTaskRequest;
import com.javaproject.java_project.service.AuthService;
import com.javaproject.java_project.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses/{courseID}/tasks")
public class TaskController 
{
    private final AuthService authService;
    private final TaskService taskService;

    // don't really have the notion of a "Current Course"

    public TaskController(AuthService authService, TaskService taskService) {
        this.authService = authService;
        this.taskService = taskService;
    }

    /*
    @GetMapping
    public ResponseEntity<?> getTasks(@PathVariable int courseId)
    {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null)
            return new ResponseEntity<>("Log-In First", HttpStatus.UNAUTHORIZED);

        List<Task> tasks = taskService.getTasksForCourse(courseId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }
    */

    @PostMapping
    public ResponseEntity<?> createTask(@PathVariable int courseID, @RequestBody NewTaskRequest newTaskDetails)
    {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null)
            return new ResponseEntity<>("Log-In First", HttpStatus.UNAUTHORIZED);

        // trim will take care of strings with just spaces (they are empty)
        if (newTaskDetails.getTitle() == null || newTaskDetails.getTitle().trim().isEmpty())
            return new ResponseEntity<>("Task Name cannot be empty", HttpStatus.BAD_REQUEST);

        Task created = taskService.createTask(
                courseID,
                newTaskDetails.getTaskType(),
                newTaskDetails.getTitle(),
                newTaskDetails.getDescription(),
                newTaskDetails.getDeadline()

        );

        if (created == null)
            return new ResponseEntity<>("Task could not be created", HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<?> editTask(@PathVariable int courseID, @PathVariable int taskId, @RequestBody EditedTaskRequest editedTaskDetails)
    {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null)
            return new ResponseEntity<>("Log-In First", HttpStatus.UNAUTHORIZED);

        // trim will take care of strings with just spaces (they are empty)
        if (editedTaskDetails.getName() == null && editedTaskDetails.getDescription() == null && editedTaskDetails.getDeadline() == null)
            return new ResponseEntity<>("No fields to update", HttpStatus.BAD_REQUEST);

        Task updated = taskService.editTask(
                courseID,
                taskId,
                editedTaskDetails.getName(),
                editedTaskDetails.getDescription(),
                editedTaskDetails.getDeadline()
        );


        if (updated == null)
            return new ResponseEntity<>("Task could not be created", HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(updated, HttpStatus.CREATED);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable int courseID, @PathVariable int taskId)
    {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null)
            return new ResponseEntity<>("Log-In First", HttpStatus.UNAUTHORIZED);

        boolean deleted = taskService.deleteTask(courseID, taskId);
        if (!deleted)
            return new ResponseEntity<>("Task not found", HttpStatus.NOT_FOUND);

        return new ResponseEntity<>("Task deleted", HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<?> completeTask(@PathVariable int courseID, @PathVariable int taskId)
    {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null)
            return new ResponseEntity<>("Log-In First", HttpStatus.UNAUTHORIZED);

        boolean completed = taskService.completeTask(courseID, taskId);
        if (!completed)
            return new ResponseEntity<>("Task not found or already completed", HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>("Task completed", HttpStatus.OK);
    }
}
