package com.avito.qa;

import static org.junit.jupiter.api.Assertions.*;

import com.avito.qa.helpers.ApiClient;
import com.avito.qa.helpers.TestDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("E2E-сценарии")
class E2eTest {

  @Test
  @DisplayName("TC-7.1: Полный жизненный цикл объявления")
  @Description("Создание → получение по ID → получение по sellerID → статистика → удаление")
  @Severity(SeverityLevel.CRITICAL)
  void fullLifecycle() {
    int sellerID = TestDataGenerator.generateSellerId();
    Map<String, Object> stats = Map.of("contacts", 1, "likes", 2, "viewCount", 3);
    Map<String, Object> payload =
        TestDataGenerator.generateItemPayload(
            new HashMap<>(
                Map.of(
                    "sellerID",
                    sellerID,
                    "name",
                    "E2E Товар",
                    "price",
                    7777,
                    "statistics",
                    stats)));

    String itemId = stepCreate(payload);
    stepGetById(itemId, sellerID);
    stepGetBySeller(sellerID, itemId);
    stepGetStatistic(itemId);
    stepDelete(itemId);
    stepVerifyDeleted(itemId);
  }

  @Test
  @DisplayName("TC-7.2: Создание и получение нескольких объявлений продавца")
  @Description("Создаём 3 объявления и проверяем что все возвращаются по sellerID")
  @Severity(SeverityLevel.CRITICAL)
  void multipleItemsBySeller() {
    int sellerID = TestDataGenerator.generateSellerId();
    List<String> createdIds = new ArrayList<>();

    for (int i = 1; i <= 3; i++) {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(
              new HashMap<>(
                  Map.of("sellerID", sellerID, "name", "Товар E2E #" + i, "price", i * 1000)));
      Response res = ApiClient.createItem(payload);
      assertEquals(200, res.statusCode());
      createdIds.add(TestDataGenerator.extractIdFromCreateResponse(res));
    }

    Response sellerRes = ApiClient.getItemsBySeller(sellerID);
    assertEquals(200, sellerRes.statusCode());

    List<String> returnedIds = sellerRes.jsonPath().getList("id");
    for (String id : createdIds) {
      assertTrue(returnedIds.contains(id));
    }
  }

  @Test
  @DisplayName("TC-7.3: Удаление не влияет на другие объявления продавца")
  @Description("После удаления одного объявления остальные остаются доступными")
  @Severity(SeverityLevel.CRITICAL)
  void deleteDoesNotAffectOthers() {
    int sellerID = TestDataGenerator.generateSellerId();

    Response res1 =
        ApiClient.createItem(
            TestDataGenerator.generateItemPayload(
                new HashMap<>(Map.of("sellerID", sellerID, "name", "Останется", "price", 100))));
    Response res2 =
        ApiClient.createItem(
            TestDataGenerator.generateItemPayload(
                new HashMap<>(
                    Map.of("sellerID", sellerID, "name", "Будет удалено", "price", 200))));
    assertEquals(200, res1.statusCode());
    assertEquals(200, res2.statusCode());
    String id1 = TestDataGenerator.extractIdFromCreateResponse(res1);
    String id2 = TestDataGenerator.extractIdFromCreateResponse(res2);

    // Удаляем второе
    ApiClient.deleteItem(id2);

    // Проверяем что первое осталось
    Response sellerRes = ApiClient.getItemsBySeller(sellerID);
    assertEquals(200, sellerRes.statusCode());

    List<String> remainingIds = sellerRes.jsonPath().getList("id");
    assertTrue(remainingIds.contains(id1));
    assertFalse(remainingIds.contains(id2));
  }

  @Step("Шаг 1: Создать объявление и получить ID")
  private String stepCreate(Map<String, Object> payload) {
    Response res = ApiClient.createItem(payload);
    assertEquals(200, res.statusCode());
    String itemId = TestDataGenerator.extractIdFromCreateResponse(res);
    assertNotNull(itemId);
    return itemId;
  }

  @Step("Шаг 2: Получить объявление по ID = {itemId}, проверить данные")
  private void stepGetById(String itemId, int expectedSellerId) {
    Response res = ApiClient.getItemById(itemId);
    assertEquals(200, res.statusCode());
    assertEquals("E2E Товар", res.jsonPath().getString("[0].name"));
    assertEquals(7777, res.jsonPath().getInt("[0].price"));
    assertEquals(expectedSellerId, res.jsonPath().getInt("[0].sellerId"));
  }

  @Step("Шаг 3: Получить объявления продавца {sellerId}, убедиться что ID = {itemId} присутствует")
  private void stepGetBySeller(int sellerId, String itemId) {
    Response res = ApiClient.getItemsBySeller(sellerId);
    assertEquals(200, res.statusCode());
    List<String> ids = res.jsonPath().getList("id");
    assertTrue(ids.contains(itemId));
  }

  @Step("Шаг 4: Получить статистику для ID = {itemId}")
  private void stepGetStatistic(String itemId) {
    Response res = ApiClient.getStatisticV1(itemId);
    assertEquals(200, res.statusCode());
  }

  @Step("Шаг 5: Удалить объявление ID = {itemId}")
  private void stepDelete(String itemId) {
    Response res = ApiClient.deleteItem(itemId);
    assertEquals(200, res.statusCode());
  }

  @Step("Шаг 6: Проверить что объявление ID = {itemId} недоступно после удаления")
  private void stepVerifyDeleted(String itemId) {
    Response res = ApiClient.getItemById(itemId);
    assertEquals(404, res.statusCode());
  }
}
