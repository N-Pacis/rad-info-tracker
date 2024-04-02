package rw.auca.radinfotracker.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.dtos.RegisterUserDTO;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;
import rw.auca.radinfotracker.repository.IUserRepository;

@Configuration
public class UserSeeder {
    @Bean
    CommandLineRunner commandLineRunner(
                                        IUserRepository userRepo
                                        ) {
        return args -> {

            if(userRepo.findByEmail("admin@radInfoTracker.rw").isEmpty()){

                RegisterUserDTO userDto = new RegisterUserDTO("System", "Admin","admin@radInfoTracker.rw", "+250787161515","", ERole.ADMIN);

                UserAccount userAccount = new UserAccount(userDto);
                userAccount.setRole(ERole.ADMIN);
                userAccount.setStatus(EUserStatus.ACTIVE);

                userAccount.setPassword("$2a$12$N9hr.Cw4ySeAxcVdTlmzF.nAFq41zST5YJRUhDs/N0Qcc4nxdGwUu");

                userRepo.save(userAccount);
            }

        };
    }
}