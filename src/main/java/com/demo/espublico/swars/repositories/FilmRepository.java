package com.demo.espublico.swars.repositories;

import com.demo.espublico.swars.entities.Film;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilmRepository extends JpaRepository<Film, Long> {
    List<Film> findByTitleIs(String name);
}
