package com.javaproject.java_project.model;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class AssignmentTask extends Task {

    @Override
    public void configureXp() {
        this.baseXP = 80;
        this.multiplier = 1.2;
    }
}
