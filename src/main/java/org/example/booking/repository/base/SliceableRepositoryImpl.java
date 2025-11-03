package org.example.booking.repository.base;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.List;

public class SliceableRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> implements SliceableRepository<T> {

    public SliceableRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public Slice<T> findAllSliced(Specification<T> specification, Pageable pageable) {
        TypedQuery<T> query = getQuery(specification, pageable);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize() + 1);
        List<T> results = query.getResultList();
        boolean hasNext = results.size() > pageable.getPageSize();
        List<T> content = hasNext ? results.subList(0, pageable.getPageSize()) : results;
        return new SliceImpl<>(content, pageable, hasNext);
    }
}