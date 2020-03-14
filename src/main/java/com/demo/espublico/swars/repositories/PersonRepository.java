package com.demo.espublico.swars.repositories;

import com.demo.espublico.swars.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByNameIs(String name);

}
