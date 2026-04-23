package org.example.api.services;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.api.client.ApiClient;
import org.example.api.models.Card;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CardService {

    private static final Logger LOG = LogManager.getLogger(CardService.class);

    public Card createCard(String listId, String name) {
        LOG.info("Creating card '{}' in list {}", name, listId);
        Card card = given()
                .spec(ApiClient.spec())
                .queryParam("idList", listId)
                .queryParam("name", name)
                .when()
                .post("/cards")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .extract()
                .as(Card.class);
        LOG.info("Card created: id={}, name='{}'", card.getId(), card.getName());
        return card;
    }

    public Card updateCard(String cardId, String newName) {
        LOG.info("Updating card {} with new name '{}'", cardId, newName);
        Card card = given()
                .spec(ApiClient.spec())
                .pathParam("id", cardId)
                .queryParam("name", newName)
                .when()
                .put("/cards/{id}")
                .then()
                .statusCode(200)
                .body("name", equalTo(newName))
                .extract()
                .as(Card.class);
        LOG.info("Card updated: id={}, name='{}'", card.getId(), card.getName());
        return card;
    }

    public Response deleteCard(String cardId) {
        LOG.info("Deleting card {}", cardId);
        return given()
                .spec(ApiClient.spec())
                .pathParam("id", cardId)
                .when()
                .delete("/cards/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
    }
}
