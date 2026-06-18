package com.fooddelivery.repository;

import com.fooddelivery.model.User;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Integer id);
}
