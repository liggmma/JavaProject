package org.example;

import java.util.List;

public interface IStudentRepository {

    int addStudent(Student student);

    Student getStudentById(int id);
    List<Student> getAllStudents();

    boolean updateStudent(Student student);

    boolean deleteStudent(int id);
}

