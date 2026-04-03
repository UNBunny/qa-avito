package com.avito.qa.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDataGenerator {

  public static final String NON_EXISTENT_UUID = "00000000-0000-0000-0000-000000000000";

  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

  public static int generateSellerId() {
    return ThreadLocalRandom.current().nextInt(111111, 999999 + 1);
  }

  public static Map<String, Object> generateItemPayload() {
    return generateItemPayload(new HashMap<>());
  }

  public static Map<String, Object> generateItemPayload(Map<String, Object> overrides) {
    Map<String, Object> stats = new HashMap<>();
    stats.put("contacts", 1);
    stats.put("likes", 1);
    stats.put("viewCount", 1);

    Map<String, Object> payload = new HashMap<>();
    payload.put("sellerID", generateSellerId());
    payload.put("name", "Тестовое объявление");
    payload.put("price", 1000);
    payload.put("statistics", stats);

    payload.putAll(overrides);
    return payload;
  }

  public static String extractIdFromCreateResponse(io.restassured.response.Response response) {
    try {
      String status = response.jsonPath().getString("status");
      if (status != null) {
        Matcher matcher = UUID_PATTERN.matcher(status);
        if (matcher.find()) {
          return matcher.group();
        }
      }
    } catch (Exception ignored) {
    }
    try {
      return response.jsonPath().getString("id");
    } catch (Exception ignored) {
    }
    return null;
  }
}
