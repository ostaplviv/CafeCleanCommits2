package ua.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.model.entity.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {

    User findUserByEmail(String email);

    boolean existsUserByEmail(String email);

    @Query("SELECT m.id FROM User u JOIN u.meals m WHERE u.id=?1")
    List<String> findUserMealsIds(String userId);

    @Query("SELECT new ua.dto.MealDTO(m.id, m.photoUrl, m.version, m.name, m.fullDescription, m.price, m.weight, c.name, m.rate) FROM User u JOIN u.meals m JOIN m.cuisine c WHERE u.id=?1")
    List<Integer> findUserMealDTOs(String userId);

    Page<User> findAll(Specification<User> filter, Pageable pageable);
}
