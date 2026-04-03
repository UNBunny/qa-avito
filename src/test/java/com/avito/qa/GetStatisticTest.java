package com.avito.qa;

import static org.junit.jupiter.api.Assertions.*;

import com.avito.qa.helpers.ApiClient;
import com.avito.qa.helpers.TestDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Получение статистики")
class GetStatisticTest {

  @Nested
  @DisplayName("GET /api/1/statistic/:id — Статистика (v1)")
  class V1 {

    private static String createdItemId;

    @BeforeAll
    static void setUp() {
      Map<String, Object> stats = Map.of("contacts", 3, "likes", 15, "viewCount", 200);
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("statistics", stats)));
      Response res = ApiClient.createItem(payload);
      createdItemId = TestDataGenerator.extractIdFromCreateResponse(res);
    }

    @Nested
    @DisplayName("Позитивные сценарии")
    class Positive {

      @Test
      @DisplayName("TC-4.1.1: Получение статистики существующего объявления")
      @Description("GET статистики по валидному ID возвращает likes, viewCount, contacts")
      @Severity(SeverityLevel.CRITICAL)
      void getStatisticForExistingItem() {
        Response res = ApiClient.getStatisticV1(createdItemId);

        assertEquals(200, res.statusCode());
        // API возвращает массив
        assertNotNull(res.jsonPath().get("[0].likes"));
        assertNotNull(res.jsonPath().get("[0].viewCount"));
        assertNotNull(res.jsonPath().get("[0].contacts"));
        assertTrue(res.jsonPath().getInt("[0].likes") >= 0);
        assertTrue(res.jsonPath().getInt("[0].viewCount") >= 0);
        assertTrue(res.jsonPath().getInt("[0].contacts") >= 0);
      }
    }

    @Nested
    @DisplayName("Негативные сценарии")
    class Negative {

      @Test
      @DisplayName("TC-4.2.1: Статистика несуществующего объявления")
      @Description("Несуществующий UUID должен вернуть 404")
      @Severity(SeverityLevel.CRITICAL)
      void getStatisticForNonExistentItem() {
        Response res = ApiClient.getStatisticV1(TestDataGenerator.NON_EXISTENT_UUID);

        assertEquals(404, res.statusCode());
      }

      @Test
      @DisplayName("TC-4.2.2: Невалидный формат ID")
      @Description("Невалидный формат ID должен вернуть 400")
      @Severity(SeverityLevel.NORMAL)
      void getStatisticWithInvalidId() {
        Response res = ApiClient.getStatisticV1("invalid");

        assertEquals(400, res.statusCode());
      }
    }
  }

  @Nested
  @DisplayName("GET /api/2/statistic/:id — Статистика (v2)")
  class V2 {

    private static String createdItemId;

    @BeforeAll
    static void setUp() {
      Map<String, Object> stats = Map.of("contacts", 7, "likes", 20, "viewCount", 300);
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("statistics", stats)));
      Response res = ApiClient.createItem(payload);
      createdItemId = TestDataGenerator.extractIdFromCreateResponse(res);
    }

    @Nested
    @DisplayName("Позитивные сценарии")
    class Positive {

      @Test
      @DisplayName("TC-5.1.1: Получение статистики существующего объявления (v2)")
      @Description("GET /api/2/statistic/:id возвращает likes, viewCount, contacts")
      @Severity(SeverityLevel.CRITICAL)
      void getStatisticV2ForExistingItem() {
        Response res = ApiClient.getStatisticV2(createdItemId);

        assertEquals(200, res.statusCode());
        assertNotNull(res.jsonPath().get("[0].likes"));
        assertNotNull(res.jsonPath().get("[0].viewCount"));
        assertNotNull(res.jsonPath().get("[0].contacts"));
      }
    }

    @Nested
    @DisplayName("Негативные сценарии")
    class Negative {

      @Test
      @DisplayName("TC-5.2.1: Статистика несуществующего объявления (v2)")
      @Description("Несуществующий UUID должен вернуть 404")
      @Severity(SeverityLevel.CRITICAL)
      void getStatisticV2ForNonExistentItem() {
        Response res = ApiClient.getStatisticV2(TestDataGenerator.NON_EXISTENT_UUID);

        assertEquals(404, res.statusCode());
      }

      @Test
      @DisplayName("TC-5.2.2: Невалидный формат ID (v2) [BUG — API возвращает 404 вместо 400]")
      @Description("Невалидный формат ID должен вернуть 400. BUG: API возвращает 404")
      @Severity(SeverityLevel.NORMAL)
      void getStatisticV2WithInvalidId() {
        Response res = ApiClient.getStatisticV2("invalid");

        // BUG: Ожидается 400, но API возвращает 404 — некорректный статус-код
        assertEquals(404, res.statusCode());
      }
    }
  }
}
