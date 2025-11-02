package com.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;

@SpringBootApplication
public class TaskNotesApiApplication {

    @Autowired
    private Environment env; // ✅ 注入 Environment 对象

    public static void main(String[] args) {
        SpringApplication.run(TaskNotesApiApplication.class, args);
    }

    @PostConstruct
    public void printActiveProfiles() {
        System.out.println("Active profiles: " + Arrays.toString(env.getActiveProfiles()));
    }
}