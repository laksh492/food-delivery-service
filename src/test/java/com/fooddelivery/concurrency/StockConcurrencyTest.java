package com.fooddelivery.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import com.fooddelivery.dto.request.CreateMenuItemRequest;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.repository.inmemory.InMemoryMenuItemRepository;
import com.fooddelivery.service.RestaurantService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockConcurrencyTest {

    private InMemoryMenuItemRepository menuItemRepository;
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        menuItemRepository = new InMemoryMenuItemRepository();
        restaurantService = new RestaurantService(null, menuItemRepository, null);

        MenuItem item = new MenuItem(1, new CreateMenuItemRequest(
                "Limited Dish", "Only one left", new BigDecimal("99.00"), 1));
        menuItemRepository.save(item);
    }

    @Test
    void onlyOneThreadReservesWhenStockIsOne() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    restaurantService.reserveStock(1, 1);
                    return true;
                } catch (AppException ex) {
                    return false;
                }
            }));
        }

        start.countDown();
        executor.shutdown();

        long successes = 0;
        long failures = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successes++;
            } else {
                failures++;
            }
        }

        assertThat(successes).isEqualTo(1);
        assertThat(failures).isEqualTo(threadCount - 1);
        assertThat(menuItemRepository.findById(1).orElseThrow().getAvailableStock()).isZero();
    }

    @Test
    void reserveStockThrowsInsufficientStockOnFailure() {
        restaurantService.reserveStock(1, 1);

        try {
            restaurantService.reserveStock(1, 1);
        } catch (AppException ex) {
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
            return;
        }
        throw new AssertionError("Expected INSUFFICIENT_STOCK");
    }
}
