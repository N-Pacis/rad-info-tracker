package rw.auca.radinfotracker.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import rw.auca.radinfotracker.model.File;
import rw.auca.radinfotracker.model.PatientAudit;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountAudit;
import rw.auca.radinfotracker.model.enums.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IUserAccountAuditRepositoryTest {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IUserAccountAuditRepository userAccountAuditRepository;

    @Autowired
    private FileRepository fileRepository;

    @AfterEach
    void tearDown() {
        userAccountAuditRepository.deleteAll();
        fileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllByUserAccount() {
        UserAccount user = new UserAccount(UUID.randomUUID(), "Testing name", "Last name", "testemail@gmail.com", "+2390232", ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, "93203wkfajkfa");
        user = userRepository.save(user);

        File file = new File(UUID.randomUUID(), "Testing name", "", "", 0, EFileSizeType.B, "", EFileStatus.SAVED);
        file = fileRepository.save(file);

        UserAccountAudit audit = new UserAccountAudit(user, EAuditType.CREATE, UUID.randomUUID(), "Testing name", "testingemail@test.test", "Testing remarks", file);
        audit = userAccountAuditRepository.save(audit);

        Page<UserAccountAudit> auditPage = userAccountAuditRepository.findAllByUserAccount(user, PageRequest.of(0, 10));
        assertEquals(auditPage.getTotalElements(), 1);
        assertThat(auditPage.getContent()).contains(audit);
    }
}