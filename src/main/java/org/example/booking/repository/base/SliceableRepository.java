package org.example.booking.repository.base;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * A custom repository with a method to return a `Slice` and avoid the additional count query.
 */
@NoRepositoryBean
public interface SliceableRepository<T> {

    Slice<T> findAllSliced(Specification<T> specification, Pageable pageable);
}