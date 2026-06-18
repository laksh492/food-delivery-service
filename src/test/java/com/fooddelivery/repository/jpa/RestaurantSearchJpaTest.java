package com.fooddelivery.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.RestaurantSearchCriteria;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@EntityScan("com.fooddelivery.model")
@ActiveProfiles("jpa-test")
@TestPropertySource(locations = "classpath:application-jpa-test.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        TransactionAutoConfiguration.class
})
@EnableJpaRepositories(basePackages = "com.fooddelivery.repository.jpa")
class RestaurantSearchJpaTest {

    @Autowired
    private RestaurantSpringDataRepository repository;

    private Restaurant spiceHub;
    private Restaurant curryPalace;
    private Restaurant inactivePlace;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        spiceHub = saveRestaurant("Spice Hub", City.BANGALORE, Set.of(Cuisine.INDIAN), true, 20, 4);
        curryPalace = saveRestaurant("Curry Palace", City.BANGALORE, Set.of(Cuisine.CHINESE), true, 6, 3);
        inactivePlace = saveRestaurant("Closed Kitchen", City.MUMBAI, Set.of(Cuisine.INDIAN), false, 10, 2);
    }

    @Test
    void filterByCity_returnsOnlyMatchingCity() {
        RestaurantSearchCriteria criteria = RestaurantSearchCriteria.builder()
                .city(City.BANGALORE)
                .build();

        Page<Restaurant> page = repository.findAll(
                RestaurantSpecifications.fromCriteria(criteria), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Restaurant::getName)
                .containsExactlyInAnyOrder("Spice Hub", "Curry Palace");
    }

    @Test
    void filterByName_returnsPartialMatch() {
        RestaurantSearchCriteria criteria = RestaurantSearchCriteria.builder()
                .name("spice")
                .build();

        Page<Restaurant> page = repository.findAll(
                RestaurantSpecifications.fromCriteria(criteria), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Spice Hub");
    }

    @Test
    void filterByCuisine_returnsRestaurantsWithCuisine() {
        RestaurantSearchCriteria criteria = RestaurantSearchCriteria.builder()
                .cuisine(Cuisine.CHINESE)
                .build();

        Page<Restaurant> page = repository.findAll(
                RestaurantSpecifications.fromCriteria(criteria), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Curry Palace");
    }

    @Test
    void filterByMinRating_excludesLowRatedRestaurants() {
        RestaurantSearchCriteria criteria = RestaurantSearchCriteria.builder()
                .minRating(4.0)
                .active(true)
                .build();

        Page<Restaurant> page = repository.findAll(
                RestaurantSpecifications.fromCriteria(criteria), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Spice Hub");
    }

    @Test
    void filterByActive_returnsOnlyActiveRestaurants() {
        RestaurantSearchCriteria criteria = RestaurantSearchCriteria.builder()
                .active(true)
                .build();

        Page<Restaurant> page = repository.findAll(
                RestaurantSpecifications.fromCriteria(criteria), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Restaurant::getName)
                .doesNotContain("Closed Kitchen");
    }

    @Test
    void pagination_returnsCorrectPage() {
        RestaurantSearchCriteria criteria = RestaurantSearchCriteria.builder()
                .city(City.BANGALORE)
                .build();

        Page<Restaurant> page = repository.findAll(
                RestaurantSpecifications.fromCriteria(criteria), PageRequest.of(0, 1));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void combinedFilters_narrowsResults() {
        RestaurantSearchCriteria criteria = RestaurantSearchCriteria.builder()
                .city(City.BANGALORE)
                .cuisine(Cuisine.INDIAN)
                .active(true)
                .minRating(4.0)
                .build();

        Page<Restaurant> page = repository.findAll(
                RestaurantSpecifications.fromCriteria(criteria), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Spice Hub");
    }

    private Restaurant saveRestaurant(String name, City city, Set<Cuisine> cuisines,
                                      boolean active, long ratingSum, int reviewCount) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setCity(city);
        restaurant.setOwnerId(1);
        restaurant.setCuisines(cuisines);
        restaurant.setActive(active);
        restaurant.setRatingSum(ratingSum);
        restaurant.setReviewCount(reviewCount);
        return repository.save(restaurant);
    }
}
