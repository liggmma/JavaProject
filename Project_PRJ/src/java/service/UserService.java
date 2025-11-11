/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.UserDAO;
import java.sql.Date;
import java.util.List;
import model.Users;

/**
 *
 * @author ADMIN
 */
public class UserService {

    UserDAO userDAO = new UserDAO();

    public List<Users> getAll() {
        return userDAO.getAll();
    }

    public Users findById(int id) {
        return userDAO.findById(id);
    }

    public void add(Users u) {
        userDAO.add(u);
    }

    public void editUser(int id, String userName, String password, String email, String phone, Date dateOfBirth, String avatarUrl, String role) {
        Users u = userDAO.findById(id);
        u.setUsername(userName);
        u.setPasswordHash(password);
        u.setEmail(email);
        u.setPhone(phone);
        u.setDateOfBirth(dateOfBirth);
        u.setAvatarUrl(avatarUrl);
        u.setRole(role);
        userDAO.update(u);
    }

}
