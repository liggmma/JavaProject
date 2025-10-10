package org.example;

import java.util.List;

public class StudentRepository implements IStudentRepository {
    private final StudentDAO dao = new StudentDAO();

    @Override
    public int addStudent(Student student) {
        return dao.addStudent(student);
    }

    @Override
    public Student getStudentById(int id) {
        return dao.getStudentById(id);
    }

    @Override
    public List<Student> getAllStudents() {
        return dao.getAllStudents();
    }

    @Override
    public boolean updateStudent(Student student) {
        return dao.updateStudent(student);
    }

    @Override
    public boolean deleteStudent(int id) {
        return dao.deleteStudent(id);
    }
}
