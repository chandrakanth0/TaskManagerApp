package com.example.apptodo;

public class Task {
    private String id; // Firestore document ID (consider using documentId consistently)
    private String title;
    private String description;
    private String startDate;
    private String dueDate;
    private String time;
    private boolean completed;
    private String documentId; // Consistent field for Firestore document ID
    private Long remainingDays; // Add this field

    // Required empty constructor for Firebase
    public Task() {}

    public Task(String title, String description, String startDate, String dueDate, String time, boolean completed) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.time = time;
        this.completed = completed;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getTime() {
        return time;
    }

    public String getDocumentId() {
        return documentId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Long getRemainingDays() { // Add this getter
        return remainingDays;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setRemainingDays(Long remainingDays) { // Add this setter
        this.remainingDays = remainingDays;
    }
}