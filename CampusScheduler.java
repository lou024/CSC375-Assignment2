package com.assignment2.jmh;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CampusScheduler{
    private enum Subject { PSY, CSC, MAT }
    private class Course implements Comparable<Course>{
        final int num;
        final Subject subject;
        Course(int num, Subject subject) {
            this.num = num;
            this.subject = subject;
        }

        @Override
        public String toString(){ return subject.toString() + num; }

        @Override
        public int compareTo(Course o) {
            return toString().compareTo(o.toString());
        }
    }
    private class Student {
        final int id;
        final String name;
        final ConcurrentSkipListMap<Integer, Course> courses;
        final AtomicInteger count;
        Student(int id, String name) {
            this.id = id;
            this.name = name;
            this.courses = new ConcurrentSkipListMap<>();
            this.count = new AtomicInteger(0);
        }

        @Override
        public String toString() { return name + " Courses: " + courses.toString(); }
        boolean addCourse(Course c) {
            if(courses.containsValue(c))return false;
            courses.put(count.getAndIncrement(), c);
            return true;
        }
    }

    private final ConcurrentSkipListMap<String, Course> classes;
    private final ExecutorService e;
    private final int numberOfStudents;
    private final Student[] students;

    public CampusScheduler(int numberOfStudents) {
        this.classes = new ConcurrentSkipListMap<>();
        // 600 total classes
        for(int i = 100; i < 250; i++) {
            Course t = new Course(i, Subject.PSY);
            classes.put(t.toString(), t);
        }
        for(int i = 100; i < 250; i++) {
            Course t = new Course(i, Subject.CSC);
            classes.put(t.toString(), t);
        }
        for(int i = 100; i < 250; i++) {
            Course t = new Course(i, Subject.MAT);
            classes.put(t.toString(), t);
        }

        //System.out.println(classes.size());
        this.numberOfStudents = numberOfStudents;
        this.students = new Student[numberOfStudents];
        for(int i = 0; i < numberOfStudents; i++){
            students[i] = new Student(i,"Student_" + i);
        }
        this.e = Executors.newFixedThreadPool(numberOfStudents);

    }

    public void run(){
        for(int i = 0; i < numberOfStudents; i++){
            final int s = i;

            e.submit(() -> {
                // add 6 classes and remove 1 class
                for(int t = 0; t < 6; t++) {
                    int courseNum = ThreadLocalRandom.current().nextInt(100, 250);
                    Subject sub = Subject.values()[ThreadLocalRandom.current().nextInt(3)];
                    //students[s].addCourse(classes.get());
                    students[s].addCourse(classes.get(sub.toString() + courseNum));
                }


            });
        }
        e.shutdown();
        try {
            e.awaitTermination(2, TimeUnit.MINUTES);
        }catch (InterruptedException e){ e.printStackTrace(); }

    }

}
