/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.CategoriesDAO;
import dao.GenericDAO;
import dao.ProductImagesDAO;
import dao.ProductsDAO;
import dao.ReviewsDAO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.Categories;
import model.ProductImages;
import model.Products;
import model.Reviews;

/**
 *
 * @author ADMIN
 */
public class ProductService {

    ProductsDAO productDAO = new ProductsDAO();
    CategoriesDAO categoriesDAO = new CategoriesDAO();

    public List<Products> getAll() {
        return productDAO.getAll();
    }

    public Products findById(int id) {
        return productDAO.findById(id);
    }

    public void editProduct(int id, String name, double price, String description) {
        Products p = productDAO.findById(id);
        p.setName(name);
        p.setDescription(description);
        productDAO.update(p);
    }

    public void update(Products p) {
        productDAO.update(p);
    }

    public List<Products> getProductByCategory(Categories cate) {
        List<Products> products = productDAO.getProductByCategory(categoriesDAO.getChildIdList(cate.getCategoryId()));
        return products;
    }

    public List<Products> getTotalProductByShop(int id) {
        return productDAO.getTotalProductByShop(id);
    }

    public void add(Products p) {
        productDAO.add(p);
    }

    public void delete(int id) {
        productDAO.delete(id);
    }

    public Products getProduct(int id) {
        return productDAO.findById(id);
    }
}
