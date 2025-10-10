package org.example.demofx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.demofx.dao.GenreDao;
import org.example.demofx.model.Book;
import org.example.demofx.model.Genre;

import java.util.List;

public class EditBookController {

    @FXML private TextField txtTitle;
    @FXML private TextField txtAuthor;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<Genre> cbGenre;

    private Book book;
    private boolean saved = false;

    private GenreDao genreDao = new GenreDao();

    public void setBookData(Book book) {
        this.book = book;

        // Load danh sách genre từ DB
        List<Genre> genres = genreDao.getAllGenres();
        cbGenre.setItems(FXCollections.observableArrayList(genres));

        // Hiển thị thông tin hiện tại
        txtTitle.setText(book.getTitle());
        txtAuthor.setText(book.getAuthor());
        txtPrice.setText(String.valueOf(book.getPrice()));

        if (book.getGenre() != null) {
            cbGenre.setValue(book.getGenre());
        }
    }

    @FXML
    private void handleSave() {
        if (book != null) {
            book.setTitle(txtTitle.getText());
            book.setAuthor(txtAuthor.getText());
            try {
                book.setPrice(Double.parseDouble(txtPrice.getText()));
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid price!").showAndWait();
                return;
            }

            Genre selectedGenre = cbGenre.getValue();
            if (selectedGenre == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a genre!").showAndWait();
                return;
            }
            book.setGenre(selectedGenre);
            saved = true;
            closeWindow();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    public boolean isSaved() {
        return saved;
    }

    private void closeWindow() {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }
}
