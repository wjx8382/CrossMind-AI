package com.crossmind.ai.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.crossmind.ai.agent.MockProductCatalog;
import com.crossmind.ai.agent.MockProductData;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MockProductDataService {

    private final MockProductCatalog catalog;

    public MockProductDataService(
            ObjectMapper objectMapper,
            @Value("${mock-data.location:file:../mock-data/products.json}") Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            this.catalog = objectMapper.readValue(inputStream, MockProductCatalog.class);
        } catch (IOException exception) {
            throw new IllegalStateException("无法加载 Mock 商品数据: " + resource.getDescription(), exception);
        }
    }

    public MockProductData find(String product, String market) {
        String normalizedProduct = product.trim().toLowerCase(Locale.ROOT);
        String normalizedMarket = market.trim().toLowerCase(Locale.ROOT);
        return catalog.products().stream()
                .filter(item -> item.product().toLowerCase(Locale.ROOT).equals(normalizedProduct))
                .filter(item -> item.market().toLowerCase(Locale.ROOT).equals(normalizedMarket))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "当前 MVP 暂无该商品与市场的 Mock 数据: " + product + " / " + market));
    }
}

