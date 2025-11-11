/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.shop.main;

import com.cloudinary.utils.ObjectUtils;
import controller.web.profile.UserProfile;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Shops;
import model.Users;
import service.ShopService;
import util.CloudinaryConfig;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "ShopRegister", urlPatterns = {"/shop/register"})
@MultipartConfig
public class ShopRegister extends HttpServlet {

    private ShopService shopService = new ShopService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Users loggedUser = (Users) session.getAttribute("user");
        if (loggedUser.getShops() != null) {
            response.sendRedirect(request.getContextPath() + "/shop/dash-board");
        } else {
            request.getRequestDispatcher("shop_register.jsp").forward(request, response);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users loggedUser = (Users) session.getAttribute("user");

        String shopName = request.getParameter("shopName");
        String contactPhone = request.getParameter("contactPhone");
        String contactEmail = request.getParameter("contactEmail");
        String shopAddress = request.getParameter("shopAddress");
        String shopDescription = request.getParameter("shopDescription");

        Shops shop = new Shops();

        shop.setShopName(shopName);
        shop.setContactPhone(contactPhone);
        shop.setContactEmail(contactEmail);
        shop.setShopAddress(shopAddress);
        shop.setShopDescription(shopDescription);
        shop.setCreatedAt(new Date());
        shop.setSellerId(loggedUser);
        shop.setShopType("normal");
        shop.setStatus("active");

        Part shopLogoPart = request.getPart("shopLogo");
        Part shopBannerPart = request.getPart("shopBanner");

        String shopLogoPartName = Paths.get(shopLogoPart.getSubmittedFileName()).getFileName().toString();
        String shopBannerPartName = Paths.get(shopBannerPart.getSubmittedFileName()).getFileName().toString();

        String shopLogo = null;
        String shopBanner = null;

        if (shopLogoPartName != null && !shopLogoPartName.isEmpty() && shopLogoPart.getSize() > 0) {
            // Gọi hàm upload lên Cloudinary
            shopLogo = uploadAndGetLinkFromCloudinary(shopLogoPart);
            shop.setShopLogoUrl(shopLogo);
        }

        if (shopBannerPartName != null && !shopBannerPartName.isEmpty() && shopBannerPart.getSize() > 0) {
            // Gọi hàm upload lên Cloudinary
            shopBanner = uploadAndGetLinkFromCloudinary(shopBannerPart);
            shop.setShopBannerUrl(shopBanner);
        }

        shopService.add(shop);
        loggedUser.setShops(shop);
        response.sendRedirect(request.getContextPath() + "/shop/dash-board");
    }

    private String uploadAndGetLinkFromCloudinary(Part filePart) {
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

        // upload ảnh lên Cloudinary
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload-", "-" + fileName);
        } catch (IOException ex) {
            Logger.getLogger(UserProfile.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (InputStream input = filePart.getInputStream(); FileOutputStream out = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            Map uploadResult = CloudinaryConfig.getCloudinary().uploader().upload(tempFile, ObjectUtils.asMap(
                    "folder", "avatar"
            ));
            return (String) uploadResult.get("secure_url");

        } catch (Exception e) {
            e.printStackTrace(); // hoặc log ra server
        } finally {
            tempFile.delete();
        }
        return null;
    }

}
