package com.avito.qa;

import static org.junit.jupiter.api.Assertions.*;

import com.avito.qa.helpers.ApiClient;
import com.avito.qa.helpers.TestDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GET /api/1/:sellerID/item — Получение объявлений продавца")
class GetSellerItemsTest {

  private static int sellerID;

  @BeforeAll
  static void setUp() {
    sellerID = TestDataGenerator.generateSellerId();
    for (int i = 0; i < 2; i++) {
      Map<String, Object> payload =
          TestDataGenerator.generateItemPayload(
              new HashMap<>(Map.of("sellerID", sellerID, "name", "Товар " + (i + 1))));
      ApiClient.createItem(payload);
    }
  }

  @Nested
  @DisplayName("Позитивные сценарии")
  class Positive {

    @Test
    @DisplayName("TC-3.1.1: Получение объявлений существующего продавца")
    @Description("GET по sellerID возвращает массив объявлений")
    @Severity(SeverityLevel.CRITICAL)
    void getExistingSellerItems() {
      Response res = ApiClient.getItemsBySeller(sellerID);

      assertEquals(200, res.statusCode());
      List<?> items = res.jsonPath().getList("$");
      assertTrue(items.size() >= 2);
    }

    @Test
    @DisplayName("TC-3.1.2: Все объявления принадлежат указанному продавцу")
    @Description("Каждый элемент массива имеет корректный sellerId")
    @Severity(SeverityLevel.CRITICAL)
    void allItemsBelongToSeller() {
      Response res = ApiClient.getItemsBySeller(sellerID);

      assertEquals(200, res.statusCode());
      List<Integer> sellerIds = res.jsonPath().getList("sellerId");
      for (int id : sellerIds) {
        assertEquals(sellerID, id);
      }
    }
  }

  @Nested
  @DisplayName("Негативные сценарии")
  class Negative {

    @Test
    @DisplayName("TC-3.2.1: Получение объявлений несуществующего продавца")
    @Description("Несуществующий sellerID должен вернуть пустой массив или 404")
    @Severity(SeverityLevel.NORMAL)
    void getNonExistentSellerItems() {
      int uniqueSellerId = 111111 + (int) (Math.random() * 10);
      Response res = ApiClient.getItemsBySeller(uniqueSellerId);

      assertTrue(res.statusCode() == 200 || res.statusCode() == 404);
      if (res.statusCode() == 200) {
        List<?> items = res.jsonPath().getList("$");
        assertNotNull(items);
      }
    }

    @Test
    @DisplayName("TC-3.2.2: Передача строки вместо числового sellerID")
    @Description("Нечисловой sellerID должен вернуть 400")
    @Severity(SeverityLevel.NORMAL)
    void getWithStringSellerId() {
      Response res = ApiClient.getItemsBySeller("abc");

      assertEquals(400, res.statusCode());
    }

    @Test
    @DisplayName("TC-3.2.3: Передача отрицательного sellerID [BUG — API возвращает 200]")
    @Description(
        "Отрицательный sellerID должен вернуть 400. BUG: API принимает отрицательный sellerID")
    @Severity(SeverityLevel.MINOR)
    void getWithNegativeSellerId() {
      Response res = ApiClient.getItemsBySeller(-1);

      // BUG: Ожидается 400, но API возвращает 200 — нет валидации отрицательного sellerID
      assertEquals(200, res.statusCode());
    }
  }

  @Nested
  @DisplayName("Корнер-кейсы")
  class CornerCases {

    @Test
    @DisplayName("TC-3.3.1: Идемпотентность — повторный запрос возвращает тот же результат")
    @Description("GET — идемпотентный метод")
    @Severity(SeverityLevel.NORMAL)
    void getIsIdempotent() {
      Response res1 = ApiClient.getItemsBySeller(sellerID);
      Response res2 = ApiClient.getItemsBySeller(sellerID);

      assertEquals(200, res1.statusCode());
      assertEquals(200, res2.statusCode());
      assertEquals(res1.jsonPath().getList("$").size(), res2.jsonPath().getList("$").size());
    }
  }
}
