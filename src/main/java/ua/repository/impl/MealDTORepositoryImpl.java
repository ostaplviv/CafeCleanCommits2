package ua.repository.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import ua.dto.MealDTO;
import ua.dto.MealIndexDTO;
import ua.model.entity.Component;
import ua.model.entity.Cuisine;
import ua.model.entity.Meal;
import ua.model.entity.Meal_;
import ua.model.filter.MealFilter;
import ua.repository.MealDTORepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

@Repository
public class MealDTORepositoryImpl implements MealDTORepository {

    private static final String WEIGHT = "weight";
    private static final String PRICE = "price";
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<MealIndexDTO> findAllMealIndexDTOs(MealFilter filter, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MealIndexDTO> criteriaQuery = criteriaBuilder.createQuery(MealIndexDTO.class);
        Root<Meal> root = criteriaQuery.from(Meal.class);
        criteriaQuery.multiselect(root.get(Meal_.id), root.get("photoUrl"), root.get("version"), root.get("rate"),
                root.get(PRICE), root.get(WEIGHT), root.get("name"), root.get("shortDescription"));
        Predicate predicate = new PredicateBuilder(criteriaBuilder, root, filter).toPredicate();
        if (predicate != null) criteriaQuery.where(predicate);
        criteriaQuery.orderBy(toOrders(pageable.getSort(), root, criteriaBuilder));
        List<MealIndexDTO> content = getContent(criteriaQuery, pageable);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Meal> countRoot = countQuery.from(Meal.class);
        countQuery.select(criteriaBuilder.count(countRoot));
        Predicate countPredicate = new PredicateBuilder(criteriaBuilder, countRoot, filter).toPredicate();
        if (countPredicate != null) countQuery.where(countPredicate);
        return PageableExecutionUtils.getPage(content, pageable, () -> entityManager.createQuery(countQuery).getSingleResult());
    }

    @Override
    public Page<MealDTO> findAllMealDTOs(MealFilter filter, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MealDTO> criteriaQuery = criteriaBuilder.createQuery(MealDTO.class).distinct(true);
        Root<Meal> root = criteriaQuery.from(Meal.class);
        Join<Meal, Cuisine> join = root.join(Meal_.cuisine);
        criteriaQuery.multiselect(root.get(Meal_.id), root.get("photoUrl"), root.get("version"), root.get("name"),
                root.get("fullDescription"), root.get(PRICE), root.get(WEIGHT), join.get("name"), root.get("rate"));
        Predicate predicate = new PredicateBuilder(criteriaBuilder, root, filter).toPredicate();
        if (predicate != null) criteriaQuery.where(predicate);
        criteriaQuery.orderBy(toOrders(pageable.getSort(), root, criteriaBuilder));
        List<MealDTO> content = getContent(criteriaQuery, pageable);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Meal> countRoot = countQuery.from(Meal.class);
        countQuery.select(criteriaBuilder.count(countRoot));
        Predicate countPredicate = new PredicateBuilder(criteriaBuilder, countRoot, filter).toPredicate();
        if (countPredicate != null) countQuery.where(countPredicate);
        return PageableExecutionUtils.getPage(content, pageable, () -> entityManager.createQuery(countQuery).getSingleResult());
    }

    private <T> List<T> getContent(CriteriaQuery<T> criteriaQuery, Pageable pageable) {
        return entityManager.createQuery(criteriaQuery)
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    private static class PredicateBuilder {

        final CriteriaBuilder cb;

        final Root<Meal> root;

        final MealFilter filter;

        final List<Predicate> predicates = new ArrayList<>();

        PredicateBuilder(CriteriaBuilder cb, Root<Meal> root, MealFilter filter) {
            this.cb = cb;
            this.root = root;
            this.filter = filter;
        }

        void findByMinRate() {
            if (!filter.getMinRate().isEmpty()) {
                predicates.add(cb.ge(root.get("rate"), new BigDecimal(filter.getMinRate().replace(',', '.'))));
            }
        }

        void findByMaxRate() {
            if (!filter.getMaxRate().isEmpty()) {
                predicates.add(cb.le(root.get("rate"), new BigDecimal(filter.getMaxRate().replace(',', '.'))));
            }
        }

        void findBySearch() {
            if (!filter.getSearch().isEmpty()) {
                predicates.add(cb.like(root.get("name"), filter.getSearch() + "%"));
            }
        }

        void findByCusinesId() {
            if (!filter.getCuisineName().isEmpty()) {
                Join<Meal, Cuisine> join = root.join(Meal_.cuisine);
                predicates.add(join.get("name").in(filter.getCuisineName()));
            }
        }

        void findByMinPrice() {
            if (!filter.getMinPrice().isEmpty()) {
                predicates.add(cb.ge(root.get(PRICE), new BigDecimal(filter.getMinPrice().replace(',', '.'))));
            }
        }

        void findByMaxPrice() {
            if (!filter.getMaxPrice().isEmpty()) {
                predicates.add(cb.le(root.get(PRICE), new BigDecimal(filter.getMaxPrice().replace(',', '.'))));
            }
        }

        void findByMinWeight() {
            if (!filter.getMinWeight().isEmpty()) {
                predicates.add(cb.ge(root.get(WEIGHT), new Integer(filter.getMinWeight())));
            }
        }

        void findByMaxWeight() {
            if (!filter.getMaxWeight().isEmpty()) {
                predicates.add(cb.le(root.get(WEIGHT), new Integer(filter.getMaxWeight())));
            }
        }

        void findByComponentList() {
            if (!filter.getComponentsId().isEmpty()) {
                Join<Meal, Component> join = root.join(Meal_.components);
                predicates.add(join.get("id").in(filter.getComponentsId()));
            }
        }

        Predicate toPredicate() {
            findByMinRate();
            findByMaxRate();
            findBySearch();
            findByCusinesId();
            findByMinPrice();
            findByMaxPrice();
            findByMinWeight();
            findByMaxWeight();
            findByComponentList();
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        }

    }

}
