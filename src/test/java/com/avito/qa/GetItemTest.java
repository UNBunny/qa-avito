package com.avito.qa;

import static org.junit.jupiter.api.Assertions.*;

import com.avito.qa.helpers.ApiClient;
import com.avito.qa.helpers.TestDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GET /api/1/item/:id — Получение объявления по ID")
class GetItemTest {

  private static String createdItemId;
  private static Map<String, Object> createdPayload;

  @BeforeAll
  static void setUp() {
    createdPayload = TestDataGenerator.generateItemPayload();
    Response res = ApiClient.createItem(createdPayload);
    createdItemId = TestDataGenerator.extractIdFromCreateResponse(res);
  }

  @Nested
  @DisplayName("Позитивные сценарии")
  class Positive {

    @Test
    @DisplayName("TC-2.1.1: Получение существующего объявления")
    @Description("GET по валидному ID возвращает объявление")
    @Severity(SeverityLevel.CRITICAL)
    void getExistingItem() {
      Response res = ApiClient.getItemById(createdItemId);

      assertEquals(200, res.statusCode());
      // API возвращает массив
      var items = res.jsonPath().getList("$");
      assertFalse(items.isEmpty());
      var item = res.jsonPath().getMap("[0]");
      assertNotNull(item.get("id"));
      assertNotNull(item.get("name"));
      assertNotNull(item.get("price"));
      assertNotNull(item.get("sellerId"));
      assertNotNull(item.get("createdAt"));
    }

    @Test
    @DisplayName("TC-2.1.2: Данные совпадают с созданными")
    @Description("Поля полученного объявления совпадают с данными при создании")
    @Severity(SeverityLevel.CRITICAL)
    void dataMatchesCreated() {
      Response res = ApiClient.getItemById(createdItemId);

      assertEquals(200, res.statusCode());
      String name = res.jsonPath().getString("[0].name");
      int price = res.jsonPath().getInt("[0].price");
      int sellerId = res.jsonPath().getInt("[0].sellerId");
      String id = res.jsonPath().getString("[0].id");

      assertEquals(createdPayload.get("name"), name);
      assertEquals(createdPayload.get("price"), price);
      assertEquals(createdPayload.get("sellerID"), sellerId);
      assertEquals(createdItemId, id);
    }
  }

  @Nested
  @DisplayName("Негативные сценарии")
  class Negative {

    @Test
    @DisplayName("TC-2.2.1: Получение несуществующего объявления")
    @Description("Запрос с несуществующим UUID должен вернуть 404")
    @Severity(SeverityLevel.CRITICAL)
    void getNonExistentItem() {
      Response res = ApiClient.getItemById(TestDataGenerator.NON_EXISTENT_UUID);

      assertEquals(404, res.statusCode());
    }

    @Test
    @DisplayName("TC-2.2.2: Получение с невалидным форматом ID")
    @Description("Невалидный формат ID должен вернуть 400")
    @Severity(SeverityLevel.NORMAL)
    void getWithInvalidIdFormat() {
      Response res = ApiClient.getItemById("invalid-id-format");

      assertEquals(400, res.statusCode());
    }

    @Test
    @DisplayName("TC-2.2.3: Получение с пустым ID")
    @Description("Пустой ID в пути должен вернуть ошибку")
    @Severity(SeverityLevel.MINOR)
    void getWithEmptyId() {
      Response res = ApiClient.getItemById("");

      assertTrue(res.statusCode() == 400 || res.statusCode() == 404);
    }
  }

  @Nested
  @DisplayName("Корнер-кейсы")
  class CornerCases {

    @Test
    @DisplayName("TC-2.3.1: Идемпотентность GET-запроса")
    @Description("Повторный GET возвращает тот же результат")
    @Severity(SeverityLevel.NORMAL)
    void getIsIdempotent() {
      Response res1 = ApiClient.getItemById(createdItemId);
      Response res2 = ApiClient.getItemById(createdItemId);

      assertEquals(200, res1.statusCode());
      assertEquals(200, res2.statusCode());
      assertEquals(res1.asString(), res2.asString());
    }
  }
}
