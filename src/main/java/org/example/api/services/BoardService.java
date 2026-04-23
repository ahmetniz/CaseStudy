package org.example.api.services;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.api.client.ApiClient;
import org.example.api.models.Board;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class BoardService {

    private static final Logger LOG = LogManager.getLogger(BoardService.class);

    public Board createBoard(String name) {
        LOG.info("Creating board with name='{}'", name);
        Board board = given()
                .spec(ApiClient.spec())
                .queryParam("name", name)
                .when()
                .post("/boards/")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .extract()
                .as(Board.class);
        LOG.info("Board created: id={}", board.getId());
        return board;
    }

    public String getDefaultListId(String boardId) {
        LOG.info("Fetching lists for board {}", boardId);
        List<String> ids = given()
                .spec(ApiClient.spec())
                .pathParam("id", boardId)
                .when()
                .get("/boards/{id}/lists")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("id", String.class);
        if (ids.isEmpty()) {
            throw new IllegalStateException("Board " + boardId + " has no lists");
        }
        String listId = ids.get(0);
        LOG.info("Using list id={}", listId);
        return listId;
    }

    public Response deleteBoard(String boardId) {
        LOG.info("Deleting board {}", boardId);
        return given()
                .spec(ApiClient.spec())
                .pathParam("id", boardId)
                .when()
                .delete("/boards/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
    }
}
