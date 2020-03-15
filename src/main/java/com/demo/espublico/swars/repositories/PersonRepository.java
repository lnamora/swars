package com.demo.espublico.swars.repositories;

import com.demo.espublico.swars.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByNameIs(String name);
/*
* select *
from person p
join film_person fp on fp.film_id = p.id
join film_starship fs on fp.film_id = fs.film_id
join starship_person sp on sp.person_id = fp.person_id
where fp.film_id in (1,28,73,93);
*/

    @Query(value = "select p.* from starship_person sp join person p on p.id = sp.person_id where sp.starship_id in( " +
            "select starship_id from film_starship fs where fs.film_id in (:ids)  group by fs.starship_id having count(starship_id)=( " +
            "select max(total_times_appeared) from (select starship_id, count(*) total_times_appeared from film_starship fs where fs.film_id in (:ids) group by fs.starship_id order by count(*) desc)data)) ", nativeQuery = true)
    List<Person> findPilotByMaxCountStarship( @Param("ids") Integer... ids);

}
