package org.example.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paged response")
public class PagedResponse<T> {

    @Schema(description = "Paginated content")
    private List<T> content;

    @Schema(description = "Zero-based page index", example = "0")
    private int page;

    @Schema(description = "Indicates if there are more results available", example = "false")
    private boolean hasNext;

    @Schema(description = "Number of elements in the response", example = "10")
    private int numberOfElements;

    public static <T> PagedResponse<T> fromSlice(Slice<T> slice) {
        return new PagedResponse<>(slice.getContent(), slice.getNumber(), slice.hasNext(), slice.getNumberOfElements());
    }
}