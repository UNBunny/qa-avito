package com.avito.qa.helpers;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;

public class ApiClient {

  private static final String BASE_URL = "https://qa-internship.avito.com";

  private static void throttle(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Step("POST /api/1/item — создать объявление")
  public static Response createItem(Map<String, Object> body) {
    throttle(600);
    return RestAssured.given()
        .filter(new AllureRestAssured())
        .baseUri(BASE_URL)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .body(body)
        .when()
        .post("/api/1/item");
  }

  @Step("GET /api/1/item/{id} — получить объявление по ID")
  public static Response getItemById(String id) {
    throttle(200);
    return RestAssured.given()
        .filter(new AllureRestAssured())
        .baseUri(BASE_URL)
        .accept(ContentType.JSON)
        .when()
        .get("/api/1/item/" + id);
  }

  @Step("GET /api/1/{sellerId}/item — получить объявления продавца")
  public static Response getItemsBySeller(Object sellerId) {
    throttle(200);
    return RestAssured.given()
        .filter(new AllureRestAssured())
        .baseUri(BASE_URL)
        .accept(ContentType.JSON)
        .when()
        .get("/api/1/" + sellerId + "/item");
  }

  @Step("GET /api/1/statistic/{id} — получить статистику (v1)")
  public static Response getStatisticV1(String id) {
    throttle(200);
    return RestAssured.given()
        .filter(new AllureRestAssured())
        .baseUri(BASE_URL)
        .accept(ContentType.JSON)
        .when()
        .get("/api/1/statistic/" + id);
  }

  @Step("GET /api/2/statistic/{id} — получить статистику (v2)")
  public static Response getStatisticV2(String id) {
    throttle(200);
    return RestAssured.given()
        .filter(new AllureRestAssured())
        .baseUri(BASE_URL)
        .accept(ContentType.JSON)
        .when()
        .get("/api/2/statistic/" + id);
  }

  @Step("DELETE /api/2/item/{id} — удалить объявление")
  public static Response deleteItem(String id) {
    throttle(600);
    return RestAssured.given()
        .filter(new AllureRestAssured())
        .baseUri(BASE_URL)
        .accept(ContentType.JSON)
        .when()
        .delete("/api/2/item/" + id);
  }
}
