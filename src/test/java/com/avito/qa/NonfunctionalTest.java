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
import org.junit.jupiter.api.Test;

@DisplayName("Нефункциональные проверки")
class NonfunctionalTest {

  private static String createdItemId;

  @BeforeAll
  static void setUp() {
    Map<String, Object> payload = TestDataGenerator.generateItemPayload();
    Response res = ApiClient.createItem(payload);
    createdItemId = TestDataGenerator.extractIdFromCreateResponse(res);
  }

  @Test
  @DisplayName("TC-8.1: Время ответа создания объявления < 3 с")
  @Description("POST /api/1/item должен отвечать быстрее 3 секунд")
  @Severity(SeverityLevel.NORMAL)
  void createResponseTime() {
    long start = System.currentTimeMillis();
    Response res = ApiClient.createItem(TestDataGenerator.generateItemPayload());
    long duration = System.currentTimeMillis() - start;

    assertEquals(200, res.statusCode());
    assertTrue(duration < 3000, "Время ответа " + duration + " мс превышает 3000 мс");
  }

  @Test
  @DisplayName("TC-8.2: Время ответа получения объявления < 3 с")
  @Description("GET /api/1/item/:id должен отвечать быстрее 3 секунд")
  @Severity(SeverityLevel.NORMAL)
  void getResponseTime() {
    long start = System.currentTimeMillis();
    Response res = ApiClient.getItemById(createdItemId);
    long duration = System.currentTimeMillis() - start;

    assertEquals(200, res.statusCode());
    assertTrue(duration < 3000, "Время ответа " + duration + " мс превышает 3000 мс");
  }

  @Test
  @DisplayName("TC-8.3: Content-Type ответа — application/json")
  @Description("Все ответы API должны иметь Content-Type: application/json")
  @Severity(SeverityLevel.NORMAL)
  void contentTypeIsJson() {
    Response res = ApiClient.getItemById(createdItemId);

    assertEquals(200, res.statusCode());
    assertTrue(res.contentType().contains("application/json"));
  }
}
