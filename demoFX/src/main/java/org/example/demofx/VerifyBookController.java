package org.example.demofx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.demofx.model.Book;
import org.example.demofx.model.Genre;

public class VerifyBookController {
    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblGenre;
    @FXML private Label lblPrice;
    @FXML private ImageView imgCover;

    private boolean confirmed = false;
    private Book book;

    public void setBookData(Book book, Image coverImage) {
        this.book = book;
        lblTitle.setText("Title: " + book.getTitle());
        lblAuthor.setText("Author: " + book.getAuthor());
        Genre g = book.getGenre();
        lblGenre.setText("Genre: " + (g != null ? g.getName() : "N/A"));
        lblPrice.setText("Price: " + book.getPrice());

        if (coverImage != null) {
            imgCover.setImage(coverImage);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Book getBook() {
        return book;
    }

    @FXML
    private void confirm() {
        confirmed = true;
        ((Stage) lblTitle.getScene().getWindow()).close();
    }

    @FXML
    private void cancel() {
        confirmed = false;
        ((Stage) lblTitle.getScene().getWindow()).close();
    }
}
