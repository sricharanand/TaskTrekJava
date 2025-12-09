package com.javaproject.java_project.service;

import com.javaproject.java_project.model.Course;
import com.javaproject.java_project.model.User;
import com.javaproject.java_project.repositories.CoursesRepository;
import com.javaproject.java_project.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    UsersRepository usersRepository;
    CoursesRepository coursesRepository;

    private int nextID = 1; // autoincrement this for next courses

    // we use AuthService to know which user is currently logged in
    private final AuthService authService;

    List<Course> courses;

    public CourseService(AuthService authService)
    {
        this.authService = authService; // @Autowired will inject the same instance from before
    }

    public List<Course> getCourses()
    {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null)
            return new ArrayList<>();

        int currentUserID = currentUser.getId();
        Optional<User> temp_user = usersRepository.findById(currentUserID);

        temp_user.ifPresent(user -> courses = user.getUserCourses());

        return courses;
    }

    public Course createCourse(String courseName)
    {
        User currentUser = authService.getCurrentUser();

        // no logged-in user
        if (currentUser == null)
            return null;

        // check if name already exists in the courses, if so, return NULL (course name taken)
        for(Course course : courses)
        {
            if (courseName.equals(course.getCourseName()) && course.getUserId() == currentUser.getId())
                return null;
        }

        Course newcourse = Course.builder()
                .courseId(nextID++)
                .userId(currentUser.getId())
                .courseName(courseName)
                .build();

        courses.add(newcourse);

        if (currentUser.getUserCourses() != null)
        {
            currentUser.getUserCourses().add(newcourse);
            usersRepository.save(currentUser);
        }

        nextID++;
        return newcourse;
    }

    // Tasks of a specific course
    public Course getCourseByID(int courseID)
    {
        User currentUser = authService.getCurrentUser();

        // no logged-in user
        if (currentUser == null)
            return null;
        
        courses = currentUser.getUserCourses();

        for (Course course : courses)
        {
            if (course.getCourseId() == courseID)
                return course;
        }

        return null;
        // check if courseID matches any of the course list IDs (userIDs should match too)
        // if not, return null (course not found)
        // else, return the course
    }

    public Course renameCourse(int courseID, String newCourseName)
    {
        User currentUser = authService.getCurrentUser();

        // no logged-in user
        if (currentUser == null)
            return null;

        courses = currentUser.getUserCourses();

        for (Course course : courses)
        {
            // name clash
            if (course.getCourseId() != courseID && course.getCourseName().equals(newCourseName))
                return null;
        }

        Course course = getCourseByID(courseID);
        if (course == null)
            return null;

        course.setCourseName(newCourseName);
        usersRepository.save(currentUser);

        return course;
        // check if courseID matches any of the course list IDs
        // if not, return NULL (course not found)
        // Else, rename the course
        // return the course
    }

    public boolean deleteCourse(int courseID)
    {
        User currentUser = authService.getCurrentUser();

        // no logged-in user
        if (authService.getCurrentUser() == null)
            return false;

        courses = currentUser.getUserCourses();

        Course course = getCourseByID(courseID);
        if (course == null)
            return false;

        courses.remove(course);

        currentUser.getUserCourses().remove(course);
        usersRepository.save(currentUser);
        return true;

        // if not, return NULL (course not found)
        // Else, delete from the courses array
        // True if deleted
    }
}

