/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import model.ProductImages;

/**
 *
 * @author ADMIN
 */
public class ProductImagesDAO extends GenericDAO<ProductImages> {

    private final EntityManagerFactory emf;

    public ProductImagesDAO() {
        super(ProductImages.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

}
