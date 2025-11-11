/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 *
 * @author ADMIN
 */
public class EntityManagerFactoryInit {

    static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("demo2PU");

    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

}
