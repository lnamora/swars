package com.demo.espublico.swars.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * People
 * <p>
 * A person within the Star Wars universe
 *
 */

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Person {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * The name of this person.
     * (Required)
     *
     */
    @NotNull
    private String name;
    /**
     * An array of urls of film resources that this person has been in.
     * (Required)
     *
     */
    @ManyToMany(mappedBy = "characters")
    private List<Film> films;

    /**
     * An array of starship resources that this person has piloted
     * (Required)
     *
     */
    @ManyToMany(mappedBy = "pilots")
    private List<Starship> starships;

    public Person(String asString) {
    }
}
