/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.CategoriesDAO;
import java.util.List;
import model.Categories;

/**
 *
 * @author ADMIN
 */
public class CategoryService {

    CategoriesDAO categoriesDAO = new CategoriesDAO();

    public List<Categories> getMainCate() {
        return categoriesDAO.getMainCategory();
    }

    public Categories findById(int id) {
        return categoriesDAO.findById(id);
    }

    public List<Categories> getAll() {
        return categoriesDAO.getAll();
    }

}
