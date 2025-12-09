package com.javaproject.java_project.service;

import com.javaproject.java_project.model.*;
import com.javaproject.java_project.repositories.CoursesRepository;
import com.javaproject.java_project.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class TaskService
{
    @Autowired
    UsersRepository usersRepository;
    CoursesRepository coursesRepository;

    // In memory list of tasks
    // Going to have courseID in the task
    private List<Task> tasks;

    private int nextID = 1; // autoincrement this for next tasks

    // we use AuthService to know which user is currently logged in
    private final AuthService authService;
    private final CourseService courseService;
    private final SkillProgressService skillProgressService;


    public TaskService(AuthService authService, CourseService courseService, SkillProgressService skillProgressService)
    {
        this.authService = authService;
        this.courseService = courseService;
        this.skillProgressService = skillProgressService;
    }

    /*
    public List<Task> getTasksForCourse(int courseId)
    {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null)
            return new ArrayList<>();

        List<Task> result = new ArrayList<>();
        for (Task task : tasks)
        {
            if (task.getCourseID() == courseId)
                result.add(task);
        }
        return result;
    }
    */

    public Task createTask(int courseId, String taskType, String title, String description, LocalDateTime deadline)
    {
        // create the task object using the model, PASS THE COURSE ID TOO!
        // push to array and return the task

        User currentUser = authService.getCurrentUser();

        if(currentUser == null)
            return null;

        Course currentCourse = courseService.getCourseByID(courseId);

        Task task = switch (taskType.toUpperCase()) {

            case "ASSIGNMENT":
                task = AssignmentTask.builder()
                        .taskID(nextID++)
                        .courseID(courseId)
                        .title(title)
                        .description(description)
                        .deadline(deadline)
                        .completed(false)
                        .build();
                break;

            case "PROJECT":
                task = ProjectTask.builder()
                        .taskID(nextID++)
                        .courseID(courseId)
                        .title(title)
                        .description(description)
                        .deadline(deadline)
                        .completed(false)
                        .build();
                break;

            case "QUIZPREP":
                task = QuizPrepTask.builder()
                        .taskID(nextID++)
                        .courseID(courseId)
                        .title(title)
                        .description(description)
                        .deadline(deadline)
                        .completed(false)
                        .build();
                break;

            case "EXAMPREP":
                task = ExamPrepTask.builder()
                        .taskID(nextID++)
                        .courseID(courseId)
                        .title(title)
                        .description(description)
                        .deadline(deadline)
                        .completed(false)
                        .build();
                break;

            default:
                task = Task.builder()
                        .taskID(nextID++)
                        .courseID(courseId)
                        .title(title)
                        .description(description)
                        .deadline(deadline)
                        .completed(false)
                        .build();
        }
        task.configureXp();
        currentCourse.getTasks().add(task);
        return task;
    }

    public Task getTaskByID(int taskID)
    {
        User currentUser = authService.getCurrentUser();

        // no logged-in user
        if (currentUser == null)
            return null;

        // check if taskID matches any of the tasks list IDs
        // if not, return null (task not found)
        // else, return the task
        for (Task task : tasks)
        {
            if(task.getTaskID() == taskID)
                return task;
        }
        return null;
    }

    public Task editTask(int courseID, int taskID, String newTitle, String newDesc, LocalDateTime newDeadline)
    {
        // check if taskID matches any of the taskToEdit list IDs
        // if not, return NULL (taskToEdit not found)
        // Else, edit the taskToEdit
        // return the taskToEdit
        User currentUser = authService.getCurrentUser();

        if (currentUser == null)
            return null;

        Course currentCourse = courseService.getCourseByID(courseID);

        Task taskToEdit = getTaskByID(taskID);

        // checking if the same courseID is used
        if (taskToEdit == null || courseID != taskToEdit.getCourseID())
            return null;

        if (newTitle != null)
            taskToEdit.setTitle(newTitle);
        if (newDesc != null)
            taskToEdit.setDescription(newDesc);
        if (newDeadline != null)
            taskToEdit.setDeadline(newDeadline);

        return taskToEdit;
    }

    public boolean deleteTask(int courseID, int taskID)
    {
        // check if taskID matches any of the taskToDelete list IDs
        // if not, return NULL (taskToDelete not found)
        // Else, delete from the taskToDelete array
        // True if deleted
        User currentUser = authService.getCurrentUser();
        if(currentUser == null)
            return false;

        Course currentCourse = courseService.getCourseByID(courseID);

        Task taskToDelete = getTaskByID(taskID);

        // checking if the same courseID is used
        if (taskToDelete == null || courseID != taskToDelete.getCourseID())
            return false;

        tasks.remove(taskToDelete);
        return true;
    }

    public boolean completeTask(int courseID, int taskID)
    {
        // check if taskID matches any of the task list IDs
        // if not, return NULL (task not found)
        // add xp, level up etc etc etc
        User currentUser = authService.getCurrentUser();
        if(currentUser == null)
            return false;

        if (courseService.getCourseByID(courseID) == null)
            return false;

        Task task = getTaskByID(taskID);
        if (task == null)
            return false;

        if (task.isCompleted())
            return false; // already finished, prevents double XP

        task.setCompleted(true);

        // get course name for XP mapping
        Course course = courseService.getCourseByID(courseID);
        String courseName = course.getCourseName();

        // Penalty of 0.5x if the task is done late
        if (LocalDateTime.now().isAfter(task.getDeadline()))
            task.setMultiplier(task.getMultiplier() / 2);

        // award XP based on task difficulty computed earlier
        skillProgressService.awardTaskCompletionXp(courseName, task.getBaseXP(), task.getMultiplier());


        return true;

    }
}


