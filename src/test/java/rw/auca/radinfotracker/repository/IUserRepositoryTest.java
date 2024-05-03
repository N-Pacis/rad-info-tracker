package rw.auca.radinfotracker.repository;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountLoginHistory;
import rw.auca.radinfotracker.model.enums.ELoginStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IUserRepositoryTest {

    @Autowired
    private IUserRepository userRepository;

    private final Faker faker = new Faker();

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void findByPhoneNumber_ShouldReturnUserAccountWhenPhoneNumberExists() {
        UserAccount userAccount = createUserAccount();

        Optional<UserAccount> foundUser = userRepository.findByPhoneNumber(userAccount.getPhoneNumber());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(userAccount);
    }

    @Test
    void findByPhoneNumber_ShouldReturnEmptyOptionalWhenPhoneNumberDoesNotExist() {
        Optional<UserAccount> foundUser = userRepository.findByPhoneNumber(faker.phoneNumber().phoneNumber());

        assertThat(foundUser).isNotPresent();
    }

    @Test
    void findByEmail_ShouldReturnUserAccountWhenEmailExists() {
        UserAccount userAccount = createUserAccount();

        Optional<UserAccount> foundUser = userRepository.findByEmail(userAccount.getEmail());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(userAccount);
    }

    @Test
    void findByEmail_ShouldReturnEmptyOptionalWhenEmailDoesNotExist() {
        Optional<UserAccount> foundUser = userRepository.findByEmail(faker.internet().emailAddress());

        assertThat(foundUser).isNotPresent();
    }

    @Test
    void searchAll_ShouldReturnAllUsersWhenNoFiltersApplied() {
        List<UserAccount> users = List.of(createUserAccount(), createUserAccount(), createUserAccount());

        Page<UserAccount> foundUsers = userRepository.searchAll("", null, null, PageRequest.of(0, 10));

        assertThat(foundUsers.getTotalElements()).isEqualTo(users.size());
        assertThat(foundUsers.getContent()).containsExactlyInAnyOrderElementsOf(users);
    }

    @Test
    void searchAll_ShouldReturnFilteredUsersByStatus() {
        UserAccount activeUser = createUserAccount();
        UserAccount inactiveUser = createUserAccount();
        inactiveUser.setStatus(EUserStatus.INACTIVE);
        userRepository.save(inactiveUser);

        Page<UserAccount> foundUsers = userRepository.searchAll("", EUserStatus.ACTIVE, null, PageRequest.of(0, 10));

        assertEquals(foundUsers.getTotalElements(), 1);
        assertThat(foundUsers.getContent()).containsExactly(activeUser);
    }

    @Test
    void findBySessionIdAndId_ShouldReturnUserAccountWhenSessionIdAndIdMatch() {
        UserAccount userAccount = createUserAccount();

        Optional<UserAccount> foundUser = userRepository.findBySessionIdAndId(userAccount.getSessionId(), userAccount.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(userAccount);
    }

    @Test
    void findBySessionIdAndId_ShouldReturnEmptyOptionalWhenSessionIdOrIdDoNotMatch() {
        UserAccount userAccount = createUserAccount();

        Optional<UserAccount> foundUser = userRepository.findBySessionIdAndId(UUID.randomUUID(), userAccount.getId());

        assertThat(foundUser).isNotPresent();
    }

    private UserAccount createUserAccount() {
        UserAccount user = new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
        user.setSessionId(UUID.randomUUID());
        return userRepository.save(user);
    }
}