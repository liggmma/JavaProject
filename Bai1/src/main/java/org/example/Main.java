package org.example;


import java.util.Arrays;
import java.util.List;

public class    Main {
    public static void main(String[] args) {
        IStudentRepository repo = new StudentRepository();

        // CREATE
        Student student = new Student();
        student.setName("Tai");
        student.setMajor("IT");
        student.setGpa(7);

        Book book1 = new Book();
        book1.setTitle("Java");

        Book book2 = new Book();
        book2.setTitle("DoNet");

        student.setBooks(Arrays.asList(book1,book2));

        int newId = repo.addStudent(student);
        System.out.println("Thêm student thành công với ID: " + newId);

        // READ by ID
        Student s = repo.getStudentById(newId);
        System.out.println("Tìm student theo ID: " + s.getName() + " - " + s.getMajor());

        // UPDATE
        s.setGpa(8);
        repo.updateStudent(s);
        System.out.println("Cập nhật GPA thành công!");

        // READ ALL
        List<Student> students = repo.getAllStudents();
        System.out.println("Danh sách tất cả sinh viên:");
        for (Student st : students) {
            System.out.println(st.getId() + " | " + st.getName() + " | " + st.getMajor() + " | GPA=" + st.getGpa());
        }

        // DELETE

    }
}
