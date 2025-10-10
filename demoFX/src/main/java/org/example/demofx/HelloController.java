package org.example.demofx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.demofx.dao.BookDao;
import org.example.demofx.dao.GenreDao;
import org.example.demofx.model.Book;
import org.example.demofx.model.Genre;
import javafx.stage.Modality;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class HelloController {

    @FXML private TextField txtTitle;
    @FXML private TextField txtAuthor;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<Genre> genreCombo;
    @FXML private ImageView bookCover;
    @FXML private TextField coverPathField;

    private final String DEFAULT_COVER = "/org/example/demofx/picture/default.png";
    private File selectedFile;

    @FXML private Label statusLabel;
    @FXML private Label priceErrorLabel;
    @FXML private Label updatingLabel;


    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, Integer> colId;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colGenre;
    @FXML private TableColumn<Book, Double> colPrice;
    @FXML private TableColumn<Book, String> colCategory;
    @FXML private TableColumn<Book, ImageView> colCover;
    private final BookDao bookDao = new BookDao();
    private final GenreDao genreDao = new GenreDao();

    private ObservableList<Book> bookList;
    private ObservableList<Genre> genreList;



    @FXML
    public void initialize() {
        // Mapping cột TableView
        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colTitle.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        colAuthor.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAuthor()));
        colGenre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getGenre() != null ? cellData.getValue().getGenre().getName() : ""
                ));
        colPrice.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());


        colCategory.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));

        colCover.setCellValueFactory(cellData -> {
            String path = cellData.getValue().getCoverImagePath();
            if (path != null && !path.isEmpty()) {
                Image img = new Image("file:" + path, 80, 80, true, true); // 80x80
                ImageView imgView = new ImageView(img);
                imgView.setFitHeight(80);
                imgView.setFitWidth(80);
                return new SimpleObjectProperty<>(imgView);
            } else {
                return new SimpleObjectProperty<>(null);
            }
        });

        // Load dữ liệu từ DB
        bookList = FXCollections.observableArrayList(bookDao.getAllBooks());
        bookTable.setItems(bookList);

        // Load Genre vào ComboBox
        genreList = FXCollections.observableArrayList(genreDao.getAllGenres());
        genreCombo.setItems(genreList);

        genreCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Genre item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        genreCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Genre item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        resetBookCover();

//        Circle clip = new Circle(50, 50, 50);
//        bookCover.setClip(clip);
    }


    @FXML
    public void saveBook() {
        try {
            priceErrorLabel.setText("");
            statusLabel.setText("");

            String title = txtTitle.getText();
            String author = txtAuthor.getText();
            Genre genre = genreCombo.getValue();
            double price;

            if (title == null || title.trim().isEmpty()) {
                statusLabel.setText("Please fill the title!");
                return;
            }
            if (author == null || author.trim().isEmpty()) {
                statusLabel.setText("Please fill the author!");
                return;
            }
            if (genre == null) {
                statusLabel.setText("Please select a genre!");
                return;
            }

            try {
                price = Double.parseDouble(txtPrice.getText().trim());
                if (price <= 0) {
                    priceErrorLabel.setText("Price must be a positive number!");
                    return;
                }
            } catch (NumberFormatException e) {
                priceErrorLabel.setText("Price must be a positive number!");
                return;
            }

            Book b = new Book(title, author, price, genre);

            Image coverImage = null;
            if (selectedFile != null) {
                coverImage = new Image(selectedFile.toURI().toString());
                b.setCoverImagePath(selectedFile.getAbsolutePath());
            } else {
                coverImage = new Image(getClass().getResourceAsStream("/picture/default.png"));
                b.setCoverImagePath(DEFAULT_COVER);
            }

            // --- Hiển thị VerifyBook-view ---
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/org/example/demofx/VerifyBook-view.fxml"));
            Parent root = loader.load();

            VerifyBookController verifyCtrl = loader.getController();
            verifyCtrl.setBookData(b, coverImage);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Verify Book");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // --- Nếu xác nhận thì mới lưu ---
            if (verifyCtrl.isConfirmed()) {
                if (selectedFile != null) {
                    File destDir = new File("covers");
                    if (!destDir.exists()) destDir.mkdirs();

                    String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                    File destFile = new File(destDir, fileName);

                    try {
                        Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        b.setCoverImagePath("covers/" + fileName);
                    } catch (IOException ioEx) {
                        ioEx.printStackTrace();
                        b.setCoverImagePath(DEFAULT_COVER);
                    }
                }

                // Lưu book vào DB
                bookDao.addBook(b);
                bookList.add(b);

                // Reset form
                txtTitle.clear();
                txtAuthor.clear();
                txtPrice.clear();
                genreCombo.getSelectionModel().clearSelection();
                resetBookCover();
                coverPathField.clear();
                selectedFile = null;

                statusLabel.setText("Saved Successfully");
            } else {
                statusLabel.setText("Save cancelled!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Error while saving: " + ex.getMessage());
        }
    }


    @FXML
    public void exitP() {
        System.exit(0);
    }

    @FXML
    public void openGenreView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demofx/Genre-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Genre");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload genre list
            genreList.setAll(genreDao.getAllGenres());
            genreCombo.setItems(genreList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void chooseBookCover() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedFile = file;

            // Hiển thị tạm thời
            Image image = new Image(file.toURI().toString(), 120, 120, true, true);
            bookCover.setImage(image);
        }
    }

    private void resetBookCover() {
        bookCover.setImage(new Image(getClass().getResourceAsStream("/picture/default.png")));
    }

    @FXML
    private void updateBook() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a book to update!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demofx/EditBook-view.fxml"));
            Parent root = loader.load();

            EditBookController editCtrl = loader.getController();
            editCtrl.setBookData(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit Book");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (editCtrl.isSaved()) {
                bookDao.updateBook(selected);
                bookTable.refresh();
                statusLabel.setText("Book updated successfully!");
            } else {
                statusLabel.setText("Update cancelled.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error while opening edit window: " + e.getMessage());
        }
    }




    @FXML
    private void deleteBook() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a book to delete!");
            return;
        }
        try {
            bookDao.deleteBook(selected.getId());

            bookList.remove(selected);
            statusLabel.setText("Book deleted successfully!");
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Error while deleting: " + ex.getMessage());
        }
    }

}
