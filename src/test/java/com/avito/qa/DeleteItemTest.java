package com.avito.qa;

import static org.junit.jupiter.api.Assertions.*;

import com.avito.qa.helpers.ApiClient;
import com.avito.qa.helpers.TestDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DELETE /api/2/item/:id — Удаление объявления")
class DeleteItemTest {

  @Nested
  @DisplayName("Позитивные сценарии")
  class Positive {

    @Test
    @DisplayName("TC-6.1.1: Удаление существующего объявления")
    @Description("DELETE по валидному ID возвращает 200")
    @Severity(SeverityLevel.CRITICAL)
    void deleteExistingItem() {
      Map<String, Object> payload = TestDataGenerator.generateItemPayload();
      Response created = ApiClient.createItem(payload);
      assertEquals(200, created.statusCode());
      String itemId = TestDataGenerator.extractIdFromCreateResponse(created);

      Response res = ApiClient.deleteItem(itemId);

      assertEquals(200, res.statusCode());
    }

    @Test
    @DisplayName("TC-6.1.2: Объявление недоступно после удаления")
    @Description("После DELETE объявление не должно находиться по GET")
    @Severity(SeverityLevel.CRITICAL)
    void itemNotFoundAfterDelete() {
      Map<String, Object> payload = TestDataGenerator.generateItemPayload();
      Response created = ApiClient.createItem(payload);
      assertEquals(200, created.statusCode());
      String itemId = TestDataGenerator.extractIdFromCreateResponse(created);

      ApiClient.deleteItem(itemId);
      Response res = ApiClient.getItemById(itemId);

      assertEquals(404, res.statusCode());
    }
  }

  @Nested
  @DisplayName("Негативные сценарии")
  class Negative {

    @Test
    @DisplayName("TC-6.2.1: Удаление несуществующего объявления")
    @Description("DELETE по несуществующему UUID должен вернуть 404")
    @Severity(SeverityLevel.NORMAL)
    void deleteNonExistentItem() {
      Response res = ApiClient.deleteItem(TestDataGenerator.NON_EXISTENT_UUID);

      assertEquals(404, res.statusCode());
    }

    @Test
    @DisplayName("TC-6.2.2: Удаление с невалидным ID")
    @Description("DELETE с невалидным ID должен вернуть 400")
    @Severity(SeverityLevel.NORMAL)
    void deleteWithInvalidId() {
      Response res = ApiClient.deleteItem("invalid-id");

      assertEquals(400, res.statusCode());
    }
  }

  @Nested
  @DisplayName("Корнер-кейсы")
  class CornerCases {

    @Test
    @DisplayName("TC-6.3.1: Повторное удаление уже удалённого объявления")
    @Description("Повторный DELETE должен вернуть 404")
    @Severity(SeverityLevel.NORMAL)
    void deleteAlreadyDeletedItem() {
      Map<String, Object> payload = TestDataGenerator.generateItemPayload();
      Response created = ApiClient.createItem(payload);
      assertEquals(200, created.statusCode());
      String itemId = TestDataGenerator.extractIdFromCreateResponse(created);

      Response del1 = ApiClient.deleteItem(itemId);
      assertEquals(200, del1.statusCode());

      Response del2 = ApiClient.deleteItem(itemId);
      assertEquals(404, del2.statusCode());
    }
  }
}
