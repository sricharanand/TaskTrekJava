package com.javaproject.java_project.request;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EditedTaskRequest
{
    private String name;
    private String description;
    private LocalDateTime deadline;

}
