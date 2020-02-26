package com.backend;

import static com.backend.MySQLMethods.addStudentHours;

public class Event extends Student {
    private String eventName;
    private double hours;
    private int year;
    private int month;
    private int day;

    public Event(String firstName, String lastName, int studentID, String eventName, double hours,
                 int year, int month, int day) {
        super(firstName, lastName, studentID);
        this.eventName = eventName;
        this.hours = hours;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public Event() {
        //Just Creates an Event Object
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public void setDate(Date date) {
        this.year = date.getYear();
        this.month = date.getMonth();
        this.day = date.getDay();
    }

    public Student getStudent() {
        return super.getStudent();
    }

    public void setStudent(Student student) {
        super.setStudent(student);
    }

    public void addEvent() {
        try {
            addStudentHours(super.getFirstName(), super.getLastName(), super.getStudentID(), eventName, hours,
                    year, month, day);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
