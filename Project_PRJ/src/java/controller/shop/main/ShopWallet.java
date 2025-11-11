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
@WebServlet(name = "ShopWallet", urlPatterns = {"/shop/wallet"})
@MultipartConfig
public class ShopWallet extends HttpServlet {

    private ShopService shopService = new ShopService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Users loggedUser = (Users) session.getAttribute("user");
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login"); // hoặc thông báo lỗi
            return;
        }

        if (loggedUser.getShops() != null) {
            request.setAttribute("shop", loggedUser.getShops());
            request.getRequestDispatcher("shop_wallet.jsp").forward(request, response);

        } else {
            response.sendRedirect(request.getContextPath() + "/shop/register");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy thông tin người dùng và cửa hàng hiện tại
        HttpSession session = request.getSession();
        Users loggedUser = (Users) session.getAttribute("user");

        // Đảm bảo người dùng đã đăng nhập
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Shops existingShop = loggedUser.getShops();

        if (existingShop == null) {
            // Xử lý lỗi: người dùng này không có cửa hàng để cập nhật
            response.sendRedirect(request.getContextPath() + "/shop/register");
            return;
        }

        // 2. Lấy dữ liệu mới từ form
        String shopName = request.getParameter("shopName");
        String contactPhone = request.getParameter("contactPhone");
        String contactEmail = request.getParameter("contactEmail");
        String shopAddress = request.getParameter("shopAddress");
        String shopDescription = request.getParameter("shopDescription");

        // Lấy các file được tải lên
        Part shopLogoPart = request.getPart("logo"); // Tên input trong form là "logo"
        Part shopBannerPart = request.getPart("banner"); // Tên input trong form là "banner"

        // 3. Cập nhật các thuộc tính cho đối tượng `existingShop`
        // Chỉ cập nhật những trường được phép thay đổi từ form
        existingShop.setShopName(shopName);
        existingShop.setContactPhone(contactPhone);
        existingShop.setContactEmail(contactEmail);
        existingShop.setShopAddress(shopAddress);
        existingShop.setShopDescription(shopDescription);
        // Không cập nhật createdAt, sellerId, shopType, status từ form này

        // 4. Xử lý upload ảnh (chỉ khi có file mới được chọn)
        // Xử lý logo
        if (shopLogoPart != null && shopLogoPart.getSize() > 0) {
            // Gọi hàm upload lên Cloudinary và lấy link
            String newShopLogoUrl = uploadAndGetLinkFromCloudinary(shopLogoPart);
            existingShop.setShopLogoUrl(newShopLogoUrl);
        }

        // Xử lý banner
        if (shopBannerPart != null && shopBannerPart.getSize() > 0) {
            // Gọi hàm upload lên Cloudinary và lấy link
            String newShopBannerUrl = uploadAndGetLinkFromCloudinary(shopBannerPart);
            existingShop.setShopBannerUrl(newShopBannerUrl);
        }

        // 5. Gọi service để lưu các thay đổi vào cơ sở dữ liệu
        shopService.update(existingShop);

        // 6. Cập nhật lại thông tin trong session và chuyển hướng
        // Cập nhật đối tượng shop trong đối tượng user của session
        loggedUser.setShops(existingShop);
        session.setAttribute("user", loggedUser); // Đặt lại để session được làm mới

        // Thêm một thông báo thành công (tùy chọn nhưng nên có)
        session.setAttribute("success", "Hồ sơ cửa hàng đã được cập nhật thành công!");

        // Chuyển hướng về trang profile của shop để xem thay đổi
        response.sendRedirect(request.getContextPath() + "/shop/profile");
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
