package ge.elzhart.service;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public abstract class SearchService<Filter, Result> {

    private final Neo4jClient neo4jClient;
    private final DatabaseSelectionProvider databaseSelectionProvider;

    public Page<Result> searchByFilter(Filter filter, Pageable pageable) {
        val result = this.neo4jClient
                .query(generateQuery(filter))
                .in(database())
                .bindAll(generateParams(filter, pageable))
                .fetchAs(getTargetClass())
                .mappedBy(this::toResult)
                .all().stream().toList();
        val totalCount = this.neo4jClient
                .query(totalCountQuery(filter))
                .in(database())
                .bindAll(generateTotalCountParams(filter))
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
        return new PageImpl<>(result, pageable, totalCount);
    }

    protected abstract String generateQuery(Filter orderFilterDto);

    private String database() {
        return databaseSelectionProvider.getDatabaseSelection().getValue();
    }

    protected abstract Map<String, Object> generateParams(Filter filter, Pageable pageable);

    protected abstract Class<Result> getTargetClass();

    protected abstract Result toResult(TypeSystem typeSystem, Record record);

    protected abstract String totalCountQuery(Filter filter);

    protected abstract Map<String, Object> generateTotalCountParams(Filter filter);
}
