package org.example.demofx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.demofx.dao.GenreDao;

import org.example.demofx.model.Genre;


public class GenreController {
    @FXML private TextField txtGenreName;
    @FXML private Label lblStatus;

    private final GenreDao GenreDao = new GenreDao();

    @FXML
    public void saveGenre() {
        String deptName = txtGenreName.getText().trim();
        if (deptName.isEmpty()) {
            lblStatus.setText("Genre cannot be empty!");
            return;
        }

        Genre d = new Genre();
        d.setName(deptName);
        GenreDao.addGenre(d);

        lblStatus.setText("Genre saved!");

        // đóng cửa sổ sau khi lưu
        Stage stage = (Stage) txtGenreName.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) txtGenreName.getScene().getWindow();
        stage.close();
    }
}

