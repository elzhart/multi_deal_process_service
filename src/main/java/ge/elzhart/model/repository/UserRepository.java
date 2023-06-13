package ge.elzhart.model.repository;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import ge.elzhart.exception.NotFoundException;
import ge.elzhart.model.domain.User;

@Repository
@CacheConfig(cacheNames = "users")
public interface UserRepository extends Neo4jRepository<User, String> {

    @CacheEvict(allEntries = true)
    <S extends User> List<S> saveAll(Iterable<S> entities);

    @Caching(
            evict = {
                    @CacheEvict(key = "#p0.id", condition = "#p0.id != null"),
                    @CacheEvict(key = "#p0.username", condition = "#p0.username != null")
            })
    <S extends User> S save(S entity);

    @Cacheable
    Optional<User> findById(String id);

    @Cacheable
    default User getById(String id) {
        var optionalUser = findById(id);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException(User.class, id);
        }
        if (!optionalUser.get().isEnabled()) {
            throw new NotFoundException(User.class, id);
        }
        return optionalUser.get();
    }

    @Cacheable
    Optional<User> findByUsername(String username);
}