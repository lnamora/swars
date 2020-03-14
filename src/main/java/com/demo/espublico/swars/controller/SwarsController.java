package com.demo.espublico.swars.controller;

import com.demo.espublico.swars.entities.Film;
import com.demo.espublico.swars.entities.Person;
import com.demo.espublico.swars.entities.Starship;
import com.demo.espublico.swars.repositories.FilmRepository;
import com.demo.espublico.swars.repositories.PersonRepository;
import com.demo.espublico.swars.repositories.StarshipRepository;
import com.demo.espublico.swars.repositories.swapi.API;
import com.demo.espublico.swars.repositories.swapi.GetRequestRepository;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@Slf4j
class SwarsController {
    private final API api = new API();
    private final GetRequestRepository repository  = new GetRequestRepository(api);

    private final FilmRepository filmRepository;
    private final PersonRepository personRepository;
    private final StarshipRepository starshipRepository;

    @Autowired
    public SwarsController(FilmRepository filmRepository,
                           PersonRepository personRepository,
                           StarshipRepository starshipRepository) {
        this.filmRepository = filmRepository;
        this.personRepository = personRepository;
        this.starshipRepository = starshipRepository;

    }

    @GetMapping("/error")
    public String showError() {
        return "error";
    }

    @GetMapping("/index")
    public String showIndex() {
        return "index";
    }

    @GetMapping("/listPeopleFilms")
    public String buscarTodos(Model model) {
        List<Person> personList =  personRepository.findAll();
        model.addAttribute("personList",personList);
        return  "listPeopleFilms";
    }

    @PostMapping (value = "/load")
    public String load(Model model) {
        loadFilms();
/*
        log.info("Retrieving stored info");
        List<Person> personList =  personRepository.findAll();

        for (Person person: personList) {
            log.info("person name :"+person.getName());
            for (Film film: person.getFilms()) {
                log.info("film title :" + film.getTitle());
            }
        }


        model.addAttribute("personList",personList);
        */
        return "redirect:/listPeopleFilms";
    }

    private void loadFilms() {
        log.info("Load Films :");
        JsonObject jsonObject = repository.getAll("films", null);
        JsonArray results = jsonObject.getAsJsonArray("results");

        if (results.size() != 0) {

            for (int i = 0; i < results.size(); i++) {
                Film filmBean = new Film();
                JsonObject film = results.get(i).getAsJsonObject();
                filmBean.setTitle(film.get("title").getAsString());
                log.info("Film Title :"+film.get("title"));
                filmRepository.save(filmBean);
                Set<Person> chars = loadSubCallPeople(film.getAsJsonArray("characters"));
                Set<Starship> sships = loadSubCallStarships(film.getAsJsonArray("starships"));

                for (Person person: chars) {
                    log.info("loading films person name :" + person.getName());
                }
                for (Starship sship: sships) {
                    log.info("loading films sship name :" + sship.getName());
                }

                filmBean.setCharacters(chars);
                filmBean.setStarships(sships);
                filmRepository.flush();
            }
        } else {
            log.info("Your search didn't get any results");
        }

    }

    /**
     * load for films the underlying api calls in the array of the original call.
     * @param jsonArray
     * @return Set<Film>
     */
    private Set<Film> loadSubCallFilms(JsonArray jsonArray) {
        Set<Film> filmSet = new HashSet<>();
        if (jsonArray.size() != 0) {
            for (int j = 0; j < jsonArray.size(); j++) {
                JsonElement element = jsonArray.get(j);
                String uri = element.getAsString();
                JsonObject response = repository.innerRequest(uri);
                List<Film> filmAux = filmRepository.findByTitleIs(response.get("title").getAsString());
                Film filmNew = new Film();
                if(filmAux.isEmpty()){
                    filmNew.setTitle(response.get("title").getAsString());
                    filmRepository.save(filmNew);
                }else{
                    filmNew = filmAux.get(0);
                }

                if(null != filmNew.getCharacters()){
                    Set<Person> chars = loadSubCallPeople(response.getAsJsonArray("characters"));
                    for (Person character: chars) {
                        log.info("loading people film name :" + character.getName());
                    }
                    filmNew.setCharacters(chars);
                    filmRepository.flush();
                }
                if(null != filmNew.getStarships()){
                    Set<Starship> sships = loadSubCallStarships(response.getAsJsonArray("starships"));
                    for (Starship sship: sships) {
                        log.info("loading people sship name :" + sship.getName());
                    }
                    filmNew.setStarships(sships);
                    filmRepository.flush();
                }

                filmSet.add(filmNew);

            }
        } else {
            log.info("nothing here");
        }
        return filmSet;
    }

    //prints the underlying api calls in the array of the original call.
    private Set<Person> loadSubCallPeople(JsonArray jsonArray) {
        Set<Person> personList = new HashSet<>();
        if (jsonArray.size() != 0) {
            for (int j = 0; j < jsonArray.size(); j++) {
                JsonElement element = jsonArray.get(j);
                String uri = element.getAsString();
                JsonObject response = repository.innerRequest(uri);
                List<Person> personAux = personRepository.findByNameIs(response.get("name").getAsString());
                Person personNew = new Person();
                if(personAux.isEmpty()){
                    personNew.setName(response.get("name").getAsString());
                    personRepository.save(personNew);
                }else{
                    personNew = personAux.get(0);
                }
                if(null != personNew.getFilms()){
                    Set<Film> films = loadSubCallFilms(response.getAsJsonArray("films"));
                    Set<Starship> sships = loadSubCallStarships(response.getAsJsonArray("starships"));
                    for (Film film: films) {
                        log.info("loading people film name :" + film.getTitle());
                    }
                    for (Starship sship: sships) {
                        log.info("loading people sship name :" + sship.getName());
                    }

                    personNew.setFilms(new ArrayList<Film>(films));
                    personNew.setStarships(new ArrayList<Starship>(sships));
                    personRepository.flush();
                }
                personList.add(personNew);
            }
        } else {
            log.info("nothing here person");
        }
        return personList;
    }

    //prints the underlying api calls in the array of the original call.
    private Set<Starship> loadSubCallStarships(JsonArray jsonArray) {
        log.info("loadSubCallStarships:");
        Set<Starship> starshipList = new HashSet<>();
        if (jsonArray.size() != 0) {
            for (int j = 0; j < jsonArray.size(); j++) {
                JsonElement element = jsonArray.get(j);
                String uri = element.getAsString();
                log.info("Starship uri: "+uri);
                JsonObject response = repository.innerRequest(uri);
                log.info("Starship json:"+response.get("name").getAsString());
                List<Starship> starshipAux = starshipRepository.findByNameIs(response.get("name").getAsString());
                Starship starshipNew = new Starship();
                if(starshipAux.isEmpty()){
                    starshipNew.setName(response.get("name").getAsString());
                    starshipRepository.save(starshipNew);

                }else{
                    starshipNew = starshipAux.get(0);
                }
                if(null != starshipNew.getFilms()){
                    Set<Person> pilots = loadSubCallPeople(response.getAsJsonArray("pilots"));
                    Set<Film> films = loadSubCallFilms(response.getAsJsonArray("films"));

                    for (Film film: films) {
                        log.info("loading people film name :" + film.getTitle());
                    }
                    for (Person pilot: pilots) {
                        log.info("loading people pilot name :" + pilot.getName());
                    }

                    starshipNew.setFilms(films);
                    starshipNew.setPilots(pilots);
                    starshipRepository.flush();
                }

                starshipList.add(starshipNew);
            }
        } else {
            log.info("nothing here starship");
        }
        return starshipList;
    }

}
