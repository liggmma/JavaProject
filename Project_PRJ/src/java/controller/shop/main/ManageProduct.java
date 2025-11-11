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
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import model.Categories;
import model.ProductImages;
import model.Products;
import model.Users;
import service.CategoryService;
import service.ImagesService;
import service.ShopService;
import service.ProductService;
import util.CloudinaryConfig;

/**
 *
 * @author HUNG
 */
@WebServlet(name = "ManageProduct", urlPatterns = {"/shop/manageProducts"})
@MultipartConfig
public class ManageProduct extends HttpServlet {

    private ShopService shopService;
    private ProductService productService;
    private CategoryService categoryService;
    private ImagesService imagesService;

    @Override
    public void init() {
        this.shopService = new ShopService();
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.imagesService = new ImagesService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if (action == null) {
            action = "view";
        }

        HttpSession session = request.getSession();
        Users loggedUser = (Users) session.getAttribute("user");
        switch (action) {
            case "view":
                List<Categories> allCategory = new ArrayList<>();
                allCategory = categoryService.getAll();
                request.setAttribute("categories", allCategory);

                List<Products> allProduct = shopService.findById(loggedUser.getShops().getShopId()).getProductsList();

                List<Products> notDeleteProduct = new ArrayList<>();
                for (Products product : allProduct) {
                    if (!"deleted".equalsIgnoreCase(product.getStatus())) {
                        notDeleteProduct.add(product);
                    }
                }

                request.setAttribute("totalProducts", notDeleteProduct);
                request.getRequestDispatcher("/shop/shop_products.jsp").forward(request, response);
                break;

            case "add":
                List<Categories> categoryList = categoryService.getAll();
                request.setAttribute("categoryList", categoryList);
                request.getRequestDispatcher("/shop/shop_addProduct.jsp").forward(request, response);
                break;

            case "edit":
                String idStr = request.getParameter("id");
                try {
                    int id = Integer.parseInt(idStr);
                    Products product = productService.findById(id);

                    List<ProductImages> imgList = product.getProductImagesList();

                    request.setAttribute("imgList", imgList);
                    request.setAttribute("product", product);
                    request.setAttribute("categories", categoryService.getAll());
                    request.getRequestDispatcher("/shop/shop_editProduct.jsp").forward(request, response);
                } catch (NumberFormatException | NullPointerException e) {
                    response.sendRedirect("manageProducts?action=view");
                }
                break;

            case "delete":
                idStr = request.getParameter("id");
                try {
                    int id = Integer.parseInt(idStr);
                    Products product = productService.findById(id);
                    product.setStatus("deleted");
                    productService.update(product);

                    response.sendRedirect("manageProducts?action=view");

                } catch (NumberFormatException | NullPointerException e) {
                    response.sendRedirect("manageProducts?action=view");
                }
                break;
            default:
                response.sendRedirect("manageProducts?action=view");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("update".equals(action)) {
            try {
                // Lấy thông tin cơ bản của sản phẩm
                int productId = Integer.parseInt(request.getParameter("productId"));
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                BigDecimal basePrice = BigDecimal.valueOf(Double.parseDouble(request.getParameter("basePrice")));
                BigDecimal weight = BigDecimal.valueOf(Double.parseDouble(request.getParameter("weight")));
                int stockQuantity = Integer.parseInt(request.getParameter("stockQuantity"));
                String status = request.getParameter("status");
                int categoryId = Integer.parseInt(request.getParameter("categoryId"));

                // Lấy dữ liệu quản lý ảnh từ các trường ẩn
                String deletedImageIdsStr = request.getParameter("deletedImageIds");
                String imageOrderStr = request.getParameter("imageOrder");

                // Lấy danh sách các file ảnh mới được tải lên
                List<Part> newImageParts = request.getParts().stream()
                        .filter(part -> "newImages".equals(part.getName()) && part.getSize() > 0)
                        .collect(Collectors.toList());

                // LẤY ĐỐI TƯỢNG SẢN PHẨM TỪ DB 
                Products product = productService.findById(productId);
                if (product == null) {
                    // Xử lý trường hợp không tìm thấy sản phẩm
                    response.sendRedirect("404.jsp");
                    return;
                }

                product.setName(name);
                product.setDescription(description);
                product.setBasePrice(basePrice);
                product.setWeight(weight);
                product.setStockQuantity(stockQuantity);
                product.setStatus(status);
                product.setCategoryId(categoryService.findById(categoryId));

                // Xử lí xóa ảnh
                if (deletedImageIdsStr != null && !deletedImageIdsStr.isEmpty()) {
                    List<Integer> idsToDelete = Arrays.stream(deletedImageIdsStr.split(","))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());

                    // Xóa khỏi DB và khỏi danh sách trong đối tượng product
                    product.getProductImagesList().removeIf(img -> idsToDelete.contains(img.getImageId()));
                    for (Integer imgId : idsToDelete) {
                        imagesService.delete(imgId); // Cần có productImageService
                    }
                }

                // Xử lí upload ảnh mới
                if (newImageParts != null && !newImageParts.isEmpty()) {
                    for (Part filePart : newImageParts) {
                        String newImageUrl = uploadAndGetLinkFromCloudinary(filePart);
                        if (newImageUrl != null) {
                            ProductImages newImage = new ProductImages();
                            newImage.setImageUrl(newImageUrl);
                            newImage.setProductId(product);
                            // Thêm ảnh mới vào danh sách của sản phẩm
                            product.getProductImagesList().add(newImage);
                        }
                    }
                }

                // XỬ LÝ SẮP XẾP LẠI THỨ TỰ ẢNH 
                if (imageOrderStr != null && !imageOrderStr.isEmpty()) {
                    // Tạo một map để dễ dàng tìm kiếm ProductImage theo ID
                    Map<Integer, ProductImages> imageMap = product.getProductImagesList().stream()
                            .filter(img -> img.getImageId() != null) // Chỉ xét các ảnh đã có trong DB
                            .collect(Collectors.toMap(ProductImages::getImageId, img -> img));

                    String[] orderedIds = imageOrderStr.split(",");

                    int orderIndex = 0;
                    for (String idStr : orderedIds) {
                        if (idStr.startsWith("new_")) {
                            // Bỏ qua ảnh mới vì chúng chưa có ID, sẽ được sắp xếp sau khi lưu
                            continue;
                        }
                        int imageId = Integer.parseInt(idStr);
                        ProductImages imgToUpdate = imageMap.get(imageId);
                        if (imgToUpdate != null) {
                            imgToUpdate.setDisplayOrder(orderIndex++); // Giả sử có trường displayOrder
                        }
                    }

                    // Sắp xếp lại danh sách trong đối tượng product để nhất quán
                    product.getProductImagesList().sort(Comparator.comparing(ProductImages::getDisplayOrder,
                            Comparator.nullsLast(Comparator.naturalOrder())));
                }

                // LƯU TẤT CẢ THAY ĐỔI VÀO DB 
                productService.update(product);

                // CHUYỂN HƯỚNG VÀ THÔNG BÁO THÀNH CÔNG 
                HttpSession session = request.getSession();
                session.setAttribute("successMessage", "Cập nhật sản phẩm thành công!");
                response.sendRedirect("manageProducts");

            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("manageProducts?action=view");
            }
        } else if ("addSubmit".equals(action)) {
            try {
                String sku = request.getParameter("sku");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                BigDecimal basePrice = new BigDecimal(request.getParameter("basePrice"));
                String tags = request.getParameter("tags");
                BigDecimal weight = new BigDecimal(request.getParameter("weight"));
                int stockQuantity = Integer.parseInt(request.getParameter("stockQuantity"));
                String status = request.getParameter("status");
                int categoryId = Integer.parseInt(request.getParameter("categoryId"));

                HttpSession session = request.getSession();
                Users loggedUser = (Users) session.getAttribute("user");

                Products newProduct = new Products();
                newProduct.setSku(sku);
                newProduct.setName(name);
                newProduct.setDescription(description);
                newProduct.setBasePrice(basePrice);
                newProduct.setTags(null);
                newProduct.setWeight(weight);
                newProduct.setStockQuantity(stockQuantity);
                newProduct.setSoldQuantity(0);
                newProduct.setStatus(status);
                newProduct.setCreatedAt(new java.util.Date());
                newProduct.setUpdatedAt(new java.util.Date());
                newProduct.setCategoryId(categoryService.findById(categoryId));
                newProduct.setShopId(loggedUser.getShops());

                productService.add(newProduct);

                response.sendRedirect("manageProducts?action=view");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("manageProducts?action=view");
            }
        } else {
            doGet(request, response);
        }
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

    @Override
    public String getServletInfo() {
        return "Servlet quản lý sản phẩm với action: view, add, edit";
    }
}
