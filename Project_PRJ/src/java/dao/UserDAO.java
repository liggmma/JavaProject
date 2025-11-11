/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.Users;

/**
 *
 * @author ADMIN
 */
public class UserDAO extends GenericDAO<Users> {

    private final EntityManagerFactory emf;

    public UserDAO() {
        super(Users.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

}
