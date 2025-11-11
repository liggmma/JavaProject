/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.web.profile;

import com.cloudinary.utils.ObjectUtils;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Addresses;
import model.Users;
import service.AddressService;
import service.UserService;
import util.CloudinaryConfig;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "UserProfile", urlPatterns = {"/profile"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1, // 1MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 15 // 15MB
)
public class UserProfile extends HttpServlet {

    AddressService addressService = new AddressService();
    UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        List<Addresses> addressList = user.getAddressesList();
        Collections.sort(addressList, new Comparator<Addresses>() {
            @Override
            public int compare(Addresses addr1, Addresses addr2) {
                // Sắp xếp theo trường isDefault (true lên trước, false sau)
                return Boolean.compare(addr2.getIsDefault(), addr1.getIsDefault());
            }
        });

        request.setAttribute("user", user);
        request.getRequestDispatcher("user_profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        String action = request.getParameter("id");
        if (action.equals("updateProfile")) {
            updateProfile(request, user);
        } else if (action.equals("updateAddress")) {
            updateAddress(request, user);
        } else if (action.equals("deleteAddress")) {
            deleteAddress(request, user);
        }

        request.setAttribute("success", "Profile updated successfully!");
        session.setAttribute("user", user);
        request.getRequestDispatcher("user_profile.jsp").forward(request, response);
    }

    public void updateProfile(HttpServletRequest request, Users user) throws ServletException, IOException {
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String dobStr = request.getParameter("dateOfBirth");
        Date sqlDate = null;

        try {
            sqlDate = Date.valueOf(dobStr); // chuyển String -> java.sql.Date
        } catch (IllegalArgumentException e) {
            sqlDate = null; // hoặc xử lý lỗi ngày tháng không hợp lệ
        }

        // Avatar
        Part filePart = request.getPart("avatar");
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

        String avatarUrl = user.getAvatarUrl(); // giữ ảnh cũ nếu không có ảnh mới

        if (fileName != null && !fileName.isEmpty() && filePart.getSize() > 0) {
            // Gọi hàm upload lên Cloudinary
            avatarUrl = uploadAndGetLinkFromCloudinary(filePart);
        }

        // Cập nhật thông tin người dùng
        user.setEmail(email);
        user.setPhone(phone);
        user.setDateOfBirth(sqlDate);
        user.setAvatarUrl(avatarUrl);

        userService.editUser(user.getUserId(), user.getUsername(), user.getPasswordHash(), email, phone, sqlDate, avatarUrl, user.getRole());
    }

    public void updateAddress(HttpServletRequest request, Users user) throws ServletException, IOException {
        String addressId = request.getParameter("addressId");
        String fullName = request.getParameter("fullName");
        String phoneNumber = request.getParameter("phoneNumber");
        String addressLine = request.getParameter("addressLine");
        String isDefaultStr = request.getParameter("isDefault");

        boolean isDefault = isDefaultStr != null;
        int id = Integer.parseInt(addressId);

        List<Addresses> addressList = user.getAddressesList();
        if (id == -1) {
            // Add new address
            Addresses a = new Addresses(fullName, phoneNumber, addressLine, isDefault, user);
            addressService.add(a);
            id = a.getAddressId();
            addressList.add(a);
        } else {
            // Update address
            addressService.update(id, fullName, phoneNumber, addressLine, isDefault);
            for (Addresses a : addressList) {
                if (a.getAddressId() == id) {
                    a.setFullName(fullName);
                    a.setPhoneNumber(phoneNumber);
                    a.setAddressLine(addressLine);
                    a.setIsDefault(isDefault);
                }
            }
        }

        // Change the other default to false
        if (isDefault) {
            // update in database
            for (Addresses a : addressList) {
                if (a.getAddressId() == id) {
                    continue;
                }
                addressService.update(a.getAddressId(), a.getFullName(), a.getPhoneNumber(), a.getAddressLine(), false);
            }

            // Update in session
            for (Addresses a : addressList) {
                if (a.getAddressId() == id) {
                    continue;
                }
                a.setIsDefault(false);
            }
        }

        Collections.sort(addressList, new Comparator<Addresses>() {
            @Override
            public int compare(Addresses addr1, Addresses addr2) {
                // Sắp xếp theo trường isDefault (true lên trước, false sau)
                return Boolean.compare(addr2.getIsDefault(), addr1.getIsDefault());
            }
        });
    }

    public void deleteAddress(HttpServletRequest request, Users user) throws ServletException, IOException {
        String addressId = request.getParameter("addressId");
        int id = Integer.parseInt(addressId);

        List<Addresses> addressList = user.getAddressesList();
        addressService.delete(id);
        for (Addresses a : addressList) {
            if (a.getAddressId() == id) {
                addressList.remove(a);
                break;
            }
        }
        user.setAddressesList(addressList);
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
