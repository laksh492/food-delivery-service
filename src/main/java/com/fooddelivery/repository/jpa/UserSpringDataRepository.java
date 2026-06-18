package com.fooddelivery.repository.jpa;

import com.fooddelivery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSpringDataRepository extends JpaRepository<User, Integer> {
}
