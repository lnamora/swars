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
        return "redirect:/listPeopleFilms";
    }

    @GetMapping (value = "/managefilms")
    public String loadfilms(Model model) {
        List<Film> filmList =  filmRepository.findAll();
        model.addAttribute("filmList",filmList);
        model.addAttribute("pilot","");
        return "/managefilms";
    }

    @GetMapping("/ajax/pilot")
    public String ajaxPilots(@RequestParam("film") String film, Model model) {
        List<Person> pilot =  personRepository.findPilotByMaxCountStarship(film);
        if(!pilot.isEmpty()){
            model.addAttribute("pilot", pilot.get(0).getName());
            log.info("pilot :" + pilot.get(0).getName());
        }
       // return "/managefilms";
       return "managefilms :: pilot";
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

                filmBean.setCharacters(new ArrayList<Person>(chars));
                filmBean.setStarships(new ArrayList<Starship>(sships));
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
                filmSet.addAll(filmAux);
              }
        } else {
            log.info("nothing here");
        }
        return filmSet;
    }

    /**
     * load for films the underlying api calls in the array of the original call.
     * @param jsonArray
     * @return Set<Person>
     */
    private Set<Person> loadSubCallPilots(JsonArray jsonArray) {
        Set<Person> pilotSet = new HashSet<>();
        if (jsonArray.size() != 0) {
            for (int j = 0; j < jsonArray.size(); j++) {
                JsonElement element = jsonArray.get(j);
                String uri = element.getAsString();
                JsonObject response = repository.innerRequest(uri);
                List<Person> pilotAux = personRepository.findByNameIs(response.get("name").getAsString());
                pilotSet.addAll(pilotAux);
            }
        } else {
            log.info("nothing here");
        }
        return pilotSet;
    }

    //prints the underlying api calls in the array of the original call.
    private Set<Person> loadSubCallPeople(JsonArray jsonArray) {
        log.info("loadSubCallPeople:");
        Set<Person> personList = new HashSet<>();
        if (jsonArray.size() != 0) {
            for (int j = 0; j < jsonArray.size(); j++) {
                JsonElement element = jsonArray.get(j);
                String uri = element.getAsString();
                log.info("people uri: "+uri);
                JsonObject response = repository.innerRequest(uri);
                log.info("people json:"+response.get("name").getAsString());
                List<Person> personAux = personRepository.findByNameIs(response.get("name").getAsString());
                Person personNew = new Person();
                if(personAux.isEmpty()){
                    personNew.setName(response.get("name").getAsString());
                    personRepository.save(personNew);
                }else{
                    personNew = personAux.get(0);
                }
                if(null == personNew.getFilms()){
                    log.info("personNew: "+uri);
                    Set<Film> films = loadSubCallFilms(response.getAsJsonArray("films"));
                    for (Film film: films) {
                        log.info("loading people film name :" + film.getTitle());
                    }
                    personNew.setFilms(new ArrayList<Film>(films));
                    personRepository.flush();
                }
                if(null == personNew.getStarships()){
                    Set<Starship> sships = loadSubCallStarships(response.getAsJsonArray("starships"));
                    for (Starship sship: sships) {
                        log.info("loading people sship name :" + sship.getName());
                    }
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
                if(null == starshipNew.getFilms()){
                    Set<Film> films = loadSubCallFilms(response.getAsJsonArray("films"));
                    for (Film film: films) {
                        log.info("loading people film name :" + film.getTitle());
                    }
                    starshipNew.setFilms(new ArrayList<Film>(films));
                    starshipRepository.flush();
                }
                if(null == starshipNew.getPilots()){
                    Set<Person> pilots = loadSubCallPilots(response.getAsJsonArray("pilots"));
                    for (Person pilot: pilots) {
                        log.info("loading people pilot name :" + pilot.getName());
                    }
                    starshipNew.setPilots(new ArrayList<Person>(pilots));
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
