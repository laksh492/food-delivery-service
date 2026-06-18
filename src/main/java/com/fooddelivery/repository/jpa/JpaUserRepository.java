package com.fooddelivery.repository.jpa;

import com.fooddelivery.model.User;
import com.fooddelivery.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaUserRepository implements UserRepository {

    private final UserSpringDataRepository delegate;

    @Override
    @Transactional
    public User save(User user) {
        return delegate.save(user);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return delegate.findById(id);
    }
}
