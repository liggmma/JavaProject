/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import service.ProductService;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import model.ProductImages;
import model.Products;

/**
 *
 * @author ADMIN
 */
public class VectorConvert {

    private static void exportProductData(List<Products> products) throws IOException {
        FileWriter productWriter = new FileWriter("products.csv");
        FileWriter imageWriter = new FileWriter("product_images.csv");

        productWriter.write("product_id|name|description|category\n");
        imageWriter.write("product_id|image_url\n");

        for (Products product : products) {
            int id = product.getProductId();
            String name = safe(product.getName());
            String desc = safe(product.getDescription());
            String category = product.getCategoryId() != null ? safe(product.getCategoryId().getName()) : "";

            productWriter.write(String.format("%d|%s|%s|%s\n", id, name, desc, category));

            if (product.getProductImagesList() != null) {
                for (ProductImages img : product.getProductImagesList()) {
                    if (img.getImageUrl() != null && !img.getImageUrl().isBlank()) {
                        imageWriter.write(String.format("%d|%s\n", id, img.getImageUrl().trim()));
                    }
                }
            }
        }

        productWriter.close();
        imageWriter.close();

        System.out.println("✅ Xuất dữ liệu thành công: products.csv và product_images.csv");
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("|", " ").replace("\n", " ").replace("\r", "").trim();
    }

    private static void runPython() throws IOException {
        String command = "python";
        String script = "generate_vectors.py";

        ProcessBuilder pb = new ProcessBuilder(command, script);
        pb.directory(new File("."));
        pb.inheritIO();
        pb.start();
    }

    public static void vectorConvert() throws IOException {
        ProductService ps = new ProductService();
        exportProductData(ps.getAll());
        runPython();
    }

    public static void main(String[] args) throws IOException {
        ProductService ps = new ProductService();
        exportProductData(ps.getAll());
    }
}
