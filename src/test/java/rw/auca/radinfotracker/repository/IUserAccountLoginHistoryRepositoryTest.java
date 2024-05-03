package rw.auca.radinfotracker.repository;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAppointment;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountLoginHistory;
import rw.auca.radinfotracker.model.enums.ELoginStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IUserAccountLoginHistoryRepositoryTest {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IUserAccountLoginHistoryRepository loginHistoryRepository;

    private final Faker faker = new Faker();

    @AfterEach
    void tearDown() {
        loginHistoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnCountByUserAndUserAgent() {
        UserAccountLoginHistory loginHistory = createUserLoginHistory();

        Integer count = loginHistoryRepository.countAllByUserAndUserAgent(loginHistory.getUser(), loginHistory.getUserAgent());

        assertEquals(count, 1);
    }

    @Test
    void shouldReturnZeroWhenUserAndUserAgentNotInLoginHistory() {
        UserAccountLoginHistory loginHistory = createUserLoginHistory();

        Integer count = loginHistoryRepository.countAllByUserAndUserAgent(loginHistory.getUser(), "");

        assertEquals(count, 0);
    }

    @Test
    void shouldReturnLoginHistoriesByAUserPaginated() {
        UserAccountLoginHistory loginHistory = createUserLoginHistory();
        LocalDate date = LocalDate.now();
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.atTime(23,59,59,9999999);

        Page<UserAccountLoginHistory> loginHistories = loginHistoryRepository.findByUserAndCreatedAtBetween(loginHistory.getUser(),startTime, endTime, PageRequest.of(0, 10));

        assertEquals(loginHistories.getTotalElements(), 1);
        assertThat(loginHistories.getContent()).contains(loginHistory);
    }

    private UserAccountLoginHistory createUserLoginHistory(){
        UserAccount user = new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
        user = userRepository.save(user);

        UserAccountLoginHistory loginHistory =  new UserAccountLoginHistory(faker.internet().userAgentAny(),faker.company().name(), user);
        return loginHistoryRepository.save(loginHistory);
    }
}