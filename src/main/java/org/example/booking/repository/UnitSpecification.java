package org.example.booking.repository;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.example.booking.dto.UnitSearchRequest;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.example.booking.model.Unit;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UnitSpecification {

    public Specification<Unit> findByCriteria(UnitSearchRequest criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getNumberOfRooms() != null) {
                predicates.add(cb.equal(root.get("numberOfRooms"), criteria.getNumberOfRooms()));
            }
            if (criteria.getAccommodationType() != null) {
                predicates.add(cb.equal(root.get("accommodationType"), criteria.getAccommodationType()));
            }
            if (criteria.getFloor() != null) {
                predicates.add(cb.equal(root.get("floor"), criteria.getFloor()));
            }
            if (criteria.getMinCost() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("baseCost"), criteria.getMinCost()));
            }
            if (criteria.getMaxCost() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("baseCost"), criteria.getMaxCost()));
            }

            // Find units *without* overlapping bookings
            if (query != null && criteria.getCheckInDate() != null && criteria.getCheckOutDate() != null) {
                Subquery<Booking> subquery = query.subquery(Booking.class);
                var bookingRoot = subquery.from(Booking.class);

                // Overlap condition: (StartA < EndB) AND (EndA > StartB)
                Predicate checkInOverlap = cb.lessThan(bookingRoot.get("checkInDate"), criteria.getCheckOutDate());
                Predicate checkOutOverlap = cb.greaterThan(bookingRoot.get("checkOutDate"), criteria.getCheckInDate());

                // Active statuses
                Predicate statusPredicate = bookingRoot.get("status").in(BookingStatus.BOOKED_STATUSES);

                subquery.select(bookingRoot)
                        .where(
                                cb.equal(bookingRoot.get("unit"), root), // Link to the outer Unit
                                statusPredicate,
                                checkInOverlap,
                                checkOutOverlap
                        );

                // We want units where *no* such booking exists
                predicates.add(cb.not(cb.exists(subquery)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
