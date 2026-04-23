package org.example.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("idList")
    private String idList;

    @JsonProperty("idBoard")
    private String idBoard;

    public Card() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIdList() {
        return idList;
    }

    public String getIdBoard() {
        return idBoard;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdList(String idList) {
        this.idList = idList;
    }

    public void setIdBoard(String idBoard) {
        this.idBoard = idBoard;
    }
}
