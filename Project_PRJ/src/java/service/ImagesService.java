/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.GenericDAO;
import model.ProductImages;

/**
 *
 * @author ADMIN
 */
public class ImagesService {

    GenericDAO<ProductImages> imagesDAO = new GenericDAO<>(ProductImages.class);

    public void editImage(int id, String altText, int displayOrder, String imageUrl) {
        ProductImages pi = imagesDAO.findById(id);
        pi.setAltText(altText);
        pi.setDisplayOrder(displayOrder);
        pi.setImageUrl(imageUrl);
        imagesDAO.update(pi);
    }

    public void delete(int id) {
        imagesDAO.delete(id);
    }

}
