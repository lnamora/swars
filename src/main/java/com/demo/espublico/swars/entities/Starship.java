package com.demo.espublico.swars.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**
 * Starship
 * <p>
 * A Starship
 *
 */

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Starship {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * The name of this starship. The common name, such as Death Star.
     * (Required)
     *
     */
    private String name;

    /**
     * An array of Film URL Resources that this starship has appeared in.
     * (Required)
     *
     */
    @ManyToMany(mappedBy = "starships")
    private Set<Film> films;

    @ManyToMany
    @JoinTable(
            name = "starship_person",
            joinColumns = @JoinColumn(name = "starship_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id"))
    private Set<Person> pilots;

}
