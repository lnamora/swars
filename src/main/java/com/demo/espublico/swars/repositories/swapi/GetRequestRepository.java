package com.demo.espublico.swars.repositories.swapi;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetRequestRepository {

    private API api;

    public GetRequestRepository(API api) {
        this.api = api;
    }

    public JsonObject getAll(String path, String searchquery) {
        JsonObject jsonObject = null;
        try {
            jsonObject = api.getBuilder(path, searchquery);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public JsonObject innerRequest(String uri) {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject = api.innerRequest(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
