/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import model.Products;

/**
 *
 * @author ADMIN
 */
public class ProductsDAO extends GenericDAO<Products> {

    private final EntityManagerFactory emf;

    public ProductsDAO() {
        super(Products.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

    public List<Products> getProductByCategory(List<Integer> ids) {
        EntityManager em = emf.createEntityManager();
        List<Products> products;

        try {
            String jpql = "SELECT p FROM Products p WHERE p.categoryId.categoryId IN :ids";
            products = em.createQuery(jpql, Products.class)
                    .setParameter("ids", ids)
                    .getResultList();
        } finally {
            em.close();
        }

        return products;
    }

    public List<Products> getTotalProductByShop(int id) {
        EntityManager em = emf.createEntityManager();
        List<Products> products;

        try {
            String jpql = "SELECT p FROM Products p WHERE p.shopId.shopId = :id";
            products = em.createQuery(jpql, Products.class)
                    .setParameter("id", id)
                    .getResultList();
        } finally {
            em.close();
        }

        return products;
    }

    @Override
    public Products findById(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Products.class, id);
        } finally {
            em.close();
        }
    }

}
