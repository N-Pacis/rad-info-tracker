package rw.auca.radinfotracker.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.repository.IUserRepository;
import rw.auca.radinfotracker.security.dtos.LoginRequest;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
        locations = "classpath:application-it.properties"
)
@AutoConfigureMockMvc
public class Authorization {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    private final IUserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public Authorization(MockMvc mockMvc, ObjectMapper objectMapper, IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String obtainAccessToken(UserAccount userAccount, String password) throws Exception {
        String username = userAccount.getEmail();

        LoginRequest loginRequest = new LoginRequest(username, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode dataNode = objectMapper.readTree(response).get("data");
        return dataNode.get("token").get("accessToken").asText();
    }

    public Map<ERole, String> initializeUserAccounts() throws Exception {
        Map<ERole, String> tokenMap = new HashMap<>();
        String password = "Qwerty@570";

        UserAccount adminUser = Data.createAdmin();
        adminUser.setEmail("admin@email.com");
        adminUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(adminUser);
        tokenMap.put(ERole.ADMIN, obtainAccessToken(adminUser, password));

        UserAccount technicianUser = Data.createAdmin();
        technicianUser.setRole(ERole.TECHNICIAN);
        technicianUser.setEmail("technician@email.com");
        technicianUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(technicianUser);
        tokenMap.put(ERole.TECHNICIAN, obtainAccessToken(technicianUser, password));

        UserAccount radiologistUser = Data.createAdmin();
        radiologistUser.setRole(ERole.RADIOLOGIST);
        radiologistUser.setEmail("radiologist@email.com");
        radiologistUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(radiologistUser);
        tokenMap.put(ERole.RADIOLOGIST, obtainAccessToken(radiologistUser, password));

        return tokenMap;
    }

    public void destroy(){
        userRepository.deleteAll();
    }
}
