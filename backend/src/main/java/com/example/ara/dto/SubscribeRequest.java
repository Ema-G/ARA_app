package com.example.ara.dto;

import java.util.Map;

public record SubscribeRequest(
    String endpoint,
    Map<String, String> keys
) {}
