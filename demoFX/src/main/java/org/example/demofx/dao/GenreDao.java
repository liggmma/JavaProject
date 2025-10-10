package org.example.demofx.dao;

import org.example.demofx.model.Genre;
import org.example.demofx.repository.GenreRepository;

import java.util.List;

public class GenreDao extends GenreRepository {

    public void addGenre(Genre genre) {
        save(genre);
    }

    public void updateGenre(Genre genre) {
        save(genre);
    }

    public void deleteGenre(int id) {
        delete(id);
    }

    public Genre getGenreById(int id) {
        return findById(id);
    }

    public List<Genre> getAllGenres() {
        return findAll();
    }
}
