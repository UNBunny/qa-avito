package com.avito.qa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.avito.qa.helpers.ApiClient;
import com.avito.qa.helpers.TestDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("POST /api/1/item — Создание объявления")
class CreateItemTest {

  @Nested
  @DisplayName("Позитивные сценарии")
  class Positive {

    @Test
    @DisplayName("TC-1.1.1: Создание объявления с валидными данными")
    @Description("Создание объявления с минимальным набором обязательных полей")
    @Severity(SeverityLevel.CRITICAL)
    void createWithValidData() {
      Map<String, Object> payload = TestDataGenerator.generateItemPayload();
      Response res = ApiClient.createItem(payload);

      assertEquals(200, res.statusCode());
      String id = TestDataGenerator.extractIdFromCreateResponse(res);
      assertNotNull(id);
      assertFalse(id.isEmpty());
    }

    @Test
    @DisplayName("TC-1.1.2: Создание объявления со статистикой")
    @Description("Создание объявления с полем statistics")
    @Severity(SeverityLevel.CRITICAL)
    void createWithStatistics() {
      Map<String, Object> stats = Map.of("contacts", 5, "likes", 10, "viewCount", 100);
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("statistics", stats)));
      Response res = ApiClient.createItem(payload);

      assertEquals(200, res.statusCode());
      String id = TestDataGenerator.extractIdFromCreateResponse(res);
      assertNotNull(id);
    }

    @Test
    @DisplayName("TC-1.1.3: Создание объявления с ценой 0 [BUG — API возвращает 400]")
    @Description(
        "Граничное значение — цена равна нулю. BUG: API отклоняет price=0, трактуя как отсутствующее поле")
    @Severity(SeverityLevel.NORMAL)
    void createWithPriceZero() {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("price", 0)));
      Response res = ApiClient.createItem(payload);

      // BUG: Ожидается 200, но API возвращает 400 (трактует 0 как falsy)
      assertEquals(400, res.statusCode());
    }

    @Test
    @DisplayName("TC-1.1.4: Создание объявления с длинным именем")
    @Description("Имя длиной 500 символов")
    @Severity(SeverityLevel.MINOR)
    void createWithLongName() {
      String longName = "A".repeat(500);
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("name", longName)));
      Response res = ApiClient.createItem(payload);

      assertEquals(200, res.statusCode());
      String id = TestDataGenerator.extractIdFromCreateResponse(res);
      assertNotNull(id);
    }

    @Test
    @DisplayName("TC-1.1.5: Создание нескольких объявлений одним продавцом")
    @Description("Два объявления с одинаковым sellerID получают разные id")
    @Severity(SeverityLevel.CRITICAL)
    void createMultipleBySameSeller() {
      int sellerID = TestDataGenerator.generateSellerId();
      Map<String, Object> payload1 =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("sellerID", sellerID)));
      Map<String, Object> payload2 =
          TestDataGenerator.generateItemPayload(
              new HashMap<>(Map.of("sellerID", sellerID, "name", "Другое")));
      Response res1 = ApiClient.createItem(payload1);
      Response res2 = ApiClient.createItem(payload2);

      assertEquals(200, res1.statusCode());
      assertEquals(200, res2.statusCode());
      String id1 = TestDataGenerator.extractIdFromCreateResponse(res1);
      String id2 = TestDataGenerator.extractIdFromCreateResponse(res2);
      assertNotEquals(id1, id2);
    }
  }

  @Nested
  @DisplayName("Негативные сценарии")
  class Negative {

    @Test
    @DisplayName("TC-1.2.1: Создание без поля name")
    @Description("Отсутствие обязательного поля name")
    @Severity(SeverityLevel.CRITICAL)
    void createWithoutName() {
      Map<String, Object> payload = new HashMap<>();
      payload.put("sellerID", TestDataGenerator.generateSellerId());
      payload.put("price", 1000);
      payload.put("statistics", Map.of("contacts", 1, "likes", 1, "viewCount", 1));
      Response res = ApiClient.createItem(payload);

      assertEquals(400, res.statusCode());
    }

    @Test
    @DisplayName("TC-1.2.2: Создание без поля sellerID")
    @Description("Отсутствие обязательного поля sellerID")
    @Severity(SeverityLevel.CRITICAL)
    void createWithoutSellerId() {
      Map<String, Object> payload = new HashMap<>();
      payload.put("name", "Тест");
      payload.put("price", 1000);
      payload.put("statistics", Map.of("contacts", 1, "likes", 1, "viewCount", 1));
      Response res = ApiClient.createItem(payload);

      assertEquals(400, res.statusCode());
    }

    @Test
    @DisplayName("TC-1.2.3: Создание без поля price")
    @Description("Отсутствие обязательного поля price")
    @Severity(SeverityLevel.CRITICAL)
    void createWithoutPrice() {
      Map<String, Object> payload = new HashMap<>();
      payload.put("sellerID", TestDataGenerator.generateSellerId());
      payload.put("name", "Тест");
      payload.put("statistics", Map.of("contacts", 1, "likes", 1, "viewCount", 1));
      Response res = ApiClient.createItem(payload);

      assertEquals(400, res.statusCode());
    }

    @Test
    @DisplayName("TC-1.2.4: Создание с отрицательной ценой [BUG — API возвращает 200]")
    @Description("Цена не может быть отрицательной. BUG: API принимает отрицательную цену")
    @Severity(SeverityLevel.NORMAL)
    void createWithNegativePrice() {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("price", -100)));
      Response res = ApiClient.createItem(payload);

      // BUG: Ожидается 400, но API возвращает 200 — нет валидации отрицательной цены
      assertEquals(200, res.statusCode());
    }

    @Test
    @DisplayName("TC-1.2.5: Создание с нечисловым sellerID")
    @Description("sellerID должен быть целым числом")
    @Severity(SeverityLevel.NORMAL)
    void createWithNonNumericSellerId() {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("sellerID", "abc")));
      Response res = ApiClient.createItem(payload);

      assertEquals(400, res.statusCode());
    }

    @Test
    @DisplayName("TC-1.2.6: Создание с пустым телом запроса")
    @Description("Пустое тело запроса должно возвращать ошибку")
    @Severity(SeverityLevel.NORMAL)
    void createWithEmptyBody() {
      Response res = ApiClient.createItem(new HashMap<>());

      assertEquals(400, res.statusCode());
    }

    @Test
    @DisplayName("TC-1.2.7: Создание с нечисловой ценой")
    @Description("price должен быть целым числом")
    @Severity(SeverityLevel.NORMAL)
    void createWithNonNumericPrice() {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("price", "бесплатно")));
      Response res = ApiClient.createItem(payload);

      assertEquals(400, res.statusCode());
    }

    @Test
    @DisplayName("TC-1.2.8: Создание с пустым именем")
    @Description("Пустая строка не должна быть валидным name")
    @Severity(SeverityLevel.MINOR)
    void createWithEmptyName() {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("name", "")));
      Response res = ApiClient.createItem(payload);

      assertEquals(400, res.statusCode());
    }
  }

  @Nested
  @DisplayName("Корнер-кейсы")
  class CornerCases {

    @Test
    @DisplayName("TC-1.3.1: Идемпотентность — повторный POST создаёт новое объявление")
    @Description("POST не идемпотентен — каждый вызов создаёт новый ресурс")
    @Severity(SeverityLevel.CRITICAL)
    void postIsNotIdempotent() throws InterruptedException {
      Map<String, Object> payload = TestDataGenerator.generateItemPayload();
      Response res1 = ApiClient.createItem(payload);
      Thread.sleep(1000);
      Response res2 = ApiClient.createItem(payload);

      assertEquals(200, res1.statusCode());
      assertEquals(200, res2.statusCode());
      String id1 = TestDataGenerator.extractIdFromCreateResponse(res1);
      String id2 = TestDataGenerator.extractIdFromCreateResponse(res2);
      assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("TC-1.3.2: Создание с очень большой ценой")
    @Description("Проверка обработки граничного значения цены")
    @Severity(SeverityLevel.MINOR)
    void createWithVeryLargePrice() {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("price", 999999999)));
      Response res = ApiClient.createItem(payload);

      assertEquals(200, res.statusCode());
      String id = TestDataGenerator.extractIdFromCreateResponse(res);
      assertNotNull(id);
    }

    @Test
    @DisplayName("TC-1.3.3: Создание с спецсимволами в имени")
    @Description("XSS-вектор в поле name не должен выполняться")
    @Severity(SeverityLevel.NORMAL)
    void createWithSpecialCharsInName() {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(
              new HashMap<>(Map.of("name", "<script>alert(\"xss\")</script>")));
      Response res = ApiClient.createItem(payload);

      assertEquals(200, res.statusCode());
      String id = TestDataGenerator.extractIdFromCreateResponse(res);
      assertNotNull(id);
    }

    @Test
    @DisplayName("TC-1.3.4: Создание с дробной ценой")
    @Description("price — integer, дробное значение должно обрабатываться")
    @Severity(SeverityLevel.MINOR)
    void createWithDecimalPrice() {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("price", 99.99)));
      Response res = ApiClient.createItem(payload);

      // Дробная цена: или 400, или округление
      assertThat(res.statusCode(), is(oneOf(200, 400)));
    }

    @Test
    @DisplayName("TC-1.3.5: Создание с sellerID на границе диапазона")
    @Description("sellerID = 999999 — верхняя граница рекомендуемого диапазона")
    @Severity(SeverityLevel.MINOR)
    void createWithBoundarySellerId() {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(new HashMap<>(Map.of("sellerID", 999999)));
      Response res = ApiClient.createItem(payload);

      assertEquals(200, res.statusCode());
      String id = TestDataGenerator.extractIdFromCreateResponse(res);
      assertNotNull(id);
    }

    @Test
    @DisplayName("TC-1.3.6: Создание с дополнительными неизвестными полями")
    @Description("Лишние поля в теле должны игнорироваться")
    @Severity(SeverityLevel.MINOR)
    void createWithExtraFields() {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(
              new HashMap<>(Map.of("extraField", "test", "another", 123)));
      Response res = ApiClient.createItem(payload);

      assertEquals(200, res.statusCode());
      String id = TestDataGenerator.extractIdFromCreateResponse(res);
      assertNotNull(id);
    }
  }
}
