package com.demo.espublico.swars.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Set;
/**
 * Film
 * <p>
 * A Star Wars film
 *
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Film {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * The title of this film.
     * (Required)
     *
     */
    private String title;
    /**
     * The people resources featured within this film.
     * (Required)
     *
     */
    @ManyToMany
    @JoinTable(
        name = "film_person",
        joinColumns = @JoinColumn(name = "film_id"),
        inverseJoinColumns = @JoinColumn(name = "person_id"))
    private Set<Person> characters;

    /**
     * The starship resources featured within this film.
     * (Required)
     *
     */
    @ManyToMany
    @JoinTable(
            name = "film_starship",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "starship_id"))
    private Set<Starship> starships;
}
