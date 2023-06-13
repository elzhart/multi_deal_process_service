package ge.elzhart.service.user;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import ge.elzhart.api.dto.user.UserDto;
import ge.elzhart.api.dto.user.UserFilterDto;
import ge.elzhart.api.mapper.RecordSearchMapper;
import ge.elzhart.service.SearchService;

@Service
public class UserSearchService extends SearchService<UserFilterDto, UserDto> {

    public UserSearchService(Neo4jClient neo4jClient, DatabaseSelectionProvider databaseSelectionProvider) {
        super(neo4jClient, databaseSelectionProvider);
    }

    @NotNull
    @Override
    protected String generateQuery(UserFilterDto userFilterDto) {
        StringBuilder stringBuilder = new StringBuilder(
                "MATCH (user: User)\n"
                        + "WHERE user.createdDate <> null\n");
        if (StringUtils.isNotBlank(userFilterDto.getId())) {
            stringBuilder.append("AND user.id = $id\n");
        }
        if (userFilterDto.getUsername() != null) {
            stringBuilder.append("AND user.username = $username\n");
        }
        if (StringUtils.isNotBlank(userFilterDto.getFullName())) {
            stringBuilder.append("AND user.fullName = $fullName\n");
        }

        stringBuilder.append(
                "RETURN order\n"
                        + "ORDER BY $order\n"
                        + "SKIP $offset\n"
                        + "LIMIT $pageSize");

        return stringBuilder.toString();
    }

    @Override
    protected Map<String, Object> generateParams(UserFilterDto userFilter, Pageable pageable) {
        Map<String, Object> params = new java.util.HashMap<>(Map.of(
                "order", pageable.getSort().stream().findFirst().toString(),
                "offset", pageable.getOffset(),
                "pageSize", pageable.getPageSize()
        ));

        if (StringUtils.isNotBlank(userFilter.getId())) {
            params.put("id", userFilter.getId());
        }
        if (StringUtils.isNotBlank(userFilter.getUsername())) {
            params.put("username", userFilter.getUsername());
        }
        if (StringUtils.isNotBlank(userFilter.getFullName())) {
            params.put("fullName", userFilter.getFullName());
        }
        return params;
    }

    @Override
    protected Class<UserDto> getTargetClass() {
        return UserDto.class;
    }

    @Override
    protected UserDto toResult(TypeSystem typeSystem, Record record) {
        Value user = record.get("user");
        return RecordSearchMapper.fromRecordToUser(user);
    }

    @Override
    protected String totalCountQuery(UserFilterDto userFilterDto) {
        StringBuilder stringBuilder = new StringBuilder(
                "MATCH (user: User)\n"
                        + "WHERE user.createdDate <> null\n");
        if (StringUtils.isNotBlank(userFilterDto.getId())) {
            stringBuilder.append("AND user.id = $id\n");
        }
        if (userFilterDto.getUsername() != null) {
            stringBuilder.append("AND user.username = $username\n");
        }
        if (StringUtils.isNotBlank(userFilterDto.getFullName())) {
            stringBuilder.append("AND user.fullName = $fullName\n");
        }

        stringBuilder.append("RETURN count(distinct user)");

        return stringBuilder.toString();
    }

    @Override
    protected Map<String, Object> generateTotalCountParams(UserFilterDto userFilter) {
        Map<String, Object> params = new HashMap<>();

        if (StringUtils.isNotBlank(userFilter.getId())) {
            params.put("id", userFilter.getId());
        }
        if (StringUtils.isNotBlank(userFilter.getUsername())) {
            params.put("username", userFilter.getUsername());
        }
        if (StringUtils.isNotBlank(userFilter.getFullName())) {
            params.put("fullName", userFilter.getFullName());
        }
        return params;
    }
}
