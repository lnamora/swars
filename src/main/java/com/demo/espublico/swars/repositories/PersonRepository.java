package com.demo.espublico.swars.repositories;

import com.demo.espublico.swars.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByNameIs(String name);

    @Query(value = "SELECT * FROM people p WHERE p.id = 1", nativeQuery = true)
    Person findPilotByMaxCountStarship(String films);

}
