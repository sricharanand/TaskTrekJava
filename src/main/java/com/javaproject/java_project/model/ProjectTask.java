package com.javaproject.java_project.model;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ProjectTask extends Task {

    @Override
    public void configureXp() {
        this.baseXP = 120;
        this.multiplier = 1.6;
    }
}

