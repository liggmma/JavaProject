/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import model.Categories;

/**
 *
 * @author ADMIN
 */
public class CategoriesDAO extends GenericDAO<Categories> {

    private final EntityManagerFactory emf;

    public CategoriesDAO() {
        super(Categories.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

    public List<Categories> getMainCategory() {
        EntityManager em = emf.createEntityManager();
        List<Categories> result = null;
        try {
            result = em.createQuery(
                    "SELECT e FROM Categories e WHERE e.parentId IS NULL", Categories.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return result;
    }

    public List<Integer> getChildIdList(int id) {
        EntityManager em = emf.createEntityManager();
        List<Integer> result = new ArrayList<>();

        try {
            String jpql = "SELECT c FROM Categories c LEFT JOIN FETCH c.categoriesList WHERE c.categoryId = :id";
            Categories root = em.createQuery(jpql, Categories.class)
                    .setParameter("id", id)
                    .getSingleResult();
            if (root != null) {
                collectCategoryIds(root, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return result;
    }

    private void collectCategoryIds(Categories category, List<Integer> result) {
        result.add(category.getCategoryId());

        // Đảm bảo children được fetch nếu lazy
        if (category.getCategoriesList() != null) {
            for (Categories child : category.getCategoriesList()) {
                collectCategoryIds(child, result);
            }
        }
    }

}
