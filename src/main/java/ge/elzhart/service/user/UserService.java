package ge.elzhart.service.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import ge.elzhart.api.dto.user.CreateUserRequest;
import ge.elzhart.api.dto.user.UpdateUserRequest;
import ge.elzhart.api.dto.user.UserDto;
import ge.elzhart.api.mapper.UserEditMapper;
import ge.elzhart.api.mapper.UserViewMapper;
import ge.elzhart.exception.NotFoundException;
import ge.elzhart.exception.ValidationException;
import ge.elzhart.model.domain.user.User;
import ge.elzhart.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserEditMapper userEditMapper;
    private final UserViewMapper userViewMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto create(CreateUserRequest request, String creatorName) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ValidationException("Username exists!");
        }
        if (!request.password().equals(request.rePassword())) {
            throw new ValidationException("Passwords don't match!");
        }

        var user = userEditMapper.create(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedBy(creatorName);
        user.setCreatedDate(LocalDateTime.now());

        user = userRepository.save(user).withCreatedDate(LocalDateTime.now());

        return userViewMapper.toUserView(user);
    }

    @Transactional
    public UserDto update(String id, UpdateUserRequest request, String updaterName) {
        var user = userRepository.getById(id).withLastModifiedDate(LocalDateTime.now()).withLastModifiedBy(updaterName);
        userEditMapper.update(request, user);

        user = userRepository.save(user);

        return userViewMapper.toUserView(user);
    }

    @Transactional
    public UserDto delete(String id) {
        var user = userRepository.getById(id);

        user.setUsername(
                user.getUsername().replace("@", String.format("_%s@", user.getId().toString())));
        user.setEnabled(false);
        user = userRepository.save(user);

        return userViewMapper.toUserView(user);
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException(format("User with username - %s, not found", username)));
    }

    public UserDto getUser(String id) {
        return userViewMapper.toUserView(userRepository.getById(id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException(User.class, username));
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> findAllByNameIn(Set<String> usernames) {
        return userRepository.findAllByUsernameIn(usernames);
    }

    @Transactional
    public void saveAll(List<User> users) {
        userRepository.saveAll(users);
    }

    @Transactional
    public void detachOrderFromUser(String username, String orderId) {
        userRepository.detachOrderFromUser(username, orderId);
    }

    @Transactional
    public void deleteAll(Set<String> users) {
        userRepository.deleteAll(userRepository.findAllByUsernameIn(users));
    }
}
