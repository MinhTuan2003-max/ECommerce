package fpt.tuanhm43.server.dtos.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdvancedSearchRequest {
    @Builder.Default
    private Integer page = 0;
    private Integer size = 20;
    private String keyword;
    private List<String> searchableFields;
    private boolean autocomplete;
    private boolean fuzzy;
    private boolean phrase;
    private Map<String, String> filters;
    private Map<String, RangeValue> ranges;
    private String sortBy;
    private String sortDirection = "Asc";

    @Data
    public static class RangeValue {
        private String from;
        private String to;
    }
}