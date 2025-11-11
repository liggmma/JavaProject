/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.ProductViewHistoryDAO;
import java.util.List;
import model.ProductViewHistory;
import model.Products;

/**
 *
 * @author ADMIN
 */
public class ProductViewHistoryService {

    ProductViewHistoryDAO productViewHistoryDAO = new ProductViewHistoryDAO();

    public ProductViewHistory findById(int id) {
        return productViewHistoryDAO.findById(id);
    }

    public void add(ProductViewHistory a) {
        productViewHistoryDAO.add(a);
    }

    public void delete(int id) {
        productViewHistoryDAO.delete(id);
    }

    public int getShopNumberOfView(int shopId) {
        return productViewHistoryDAO.getShopNumberOfView(shopId);
    }

    public List<Products> getUserProductView(int userId) {
        return productViewHistoryDAO.getUserProductView(userId);
    }

}
