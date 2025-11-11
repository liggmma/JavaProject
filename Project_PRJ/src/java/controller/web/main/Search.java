/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller.web.main;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import model.Products;
import agentAI.SemanticTextImageSearch;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hc.core5.http.ParseException;

@WebServlet("/search")
@MultipartConfig
public class Search extends HttpServlet {

    private SemanticTextImageSearch search;

    @Override
    public void init() {
        this.search = new SemanticTextImageSearch();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String searchText = request.getParameter("search");
        List<Products> productList = null;

        productList = search.searchProductsByText(searchText);

        request.setAttribute("products", productList);
        request.getRequestDispatcher("/search.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Part imagePart = request.getPart("image");
        List<Products> productList = null;
        if (imagePart != null && imagePart.getSize() > 0) {
            File tempFile = File.createTempFile("upload_", ".jpg");
            try (InputStream input = imagePart.getInputStream()) {
                Files.copy(input, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                productList = search.searchProductsByImage(tempFile.getAbsolutePath());
            } catch (ParseException ex) {
                Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        request.setAttribute("products", productList);
        request.getRequestDispatcher("/search.jsp").forward(request, response);
    }

}
