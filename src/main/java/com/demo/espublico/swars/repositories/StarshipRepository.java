package com.demo.espublico.swars.repositories;

import com.demo.espublico.swars.entities.Starship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StarshipRepository extends JpaRepository<Starship, Long> {
    List<Starship> findByNameIs(String name);
}
