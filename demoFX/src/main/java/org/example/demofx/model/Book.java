package org.example.demofx.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;

    private String author;

    private double price;

    private String coverImagePath;

    // Một cuốn sách thuộc về một thể loại
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    // ✅ Category không lưu DB, tự tính dựa vào price
    @Transient
    private String category;

    public Book() {}

    public Book(String title, String author, double price, Genre genre) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.genre = genre;
    }

    // Getter - Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getCategory() {
        if (price < 50000) return "Cheap";
        else if (price < 200000 && price >= 50000) return "Medium";
        else return "Expensive";
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", category=" + getCategory() +
                ", genre=" + (genre != null ? genre.getName() : "null") +
                '}';
    }
}
