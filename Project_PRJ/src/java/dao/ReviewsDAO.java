/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import model.Reviews;

/**
 *
 * @author ADMIN
 */
public class ReviewsDAO extends GenericDAO<Reviews> {

    private final EntityManagerFactory emf;

    public ReviewsDAO() {
        super(Reviews.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

}
