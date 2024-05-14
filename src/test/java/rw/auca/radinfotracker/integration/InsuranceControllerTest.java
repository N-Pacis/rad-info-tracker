package rw.auca.radinfotracker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import rw.auca.radinfotracker.model.Insurance;
import rw.auca.radinfotracker.model.dtos.NewInsuranceDTO;
import rw.auca.radinfotracker.model.enums.EInsuranceStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.repository.IInsuranceRepository;
import rw.auca.radinfotracker.repository.IUserRepository;
import rw.auca.radinfotracker.utilities.Authorization;
import rw.auca.radinfotracker.utilities.Data;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-it.properties"
)
@AutoConfigureMockMvc
class InsuranceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IInsuranceRepository insuranceRepository;

    private Map<ERole, String> tokenMap;

    private final Authorization authorization;

    InsuranceControllerTest(@Autowired MockMvc mockMvc,
                            @Autowired ObjectMapper objectMapper,
                            @Autowired IUserRepository userRepository,
                            @Autowired PasswordEncoder passwordEncoder) {
        this.authorization = new Authorization(mockMvc, objectMapper, userRepository, passwordEncoder);
    }

    @BeforeEach
    void setUp() throws Exception {
        tokenMap = authorization.initializeUserAccounts();
    }

    @AfterEach
    void tearDown() {
        insuranceRepository.deleteAll();
        authorization.destroy();
    }

    @Test
    void canRegisterNewInsurance() throws Exception {
        NewInsuranceDTO insuranceDTO = Data.insuranceDTO();

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/insurances/register")
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insuranceDTO)));

        resultActions.andExpect(status().isOk());
    }

    @Test
    void throwsErrorWhenNonAdminUserTriesToRegisterInsurance() throws Exception {
        NewInsuranceDTO insuranceDTO = Data.insuranceDTO();

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/insurances/register")
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.TECHNICIAN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insuranceDTO)));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    void canGetAllActiveInsurances() throws Exception {
        Insurance insurance1 = Data.createInsurance();
        Insurance insurance2 = Data.createInsurance();
        Insurance insurance3 = Data.createInsurance();
        insurance3.setStatus(EInsuranceStatus.INACTIVE);
        insuranceRepository.saveAll(List.of(insurance1, insurance2, insurance3));

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/insurances/list")
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.TECHNICIAN))
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void canSearchAllInsurances() throws Exception {
        Insurance insurance1 = Data.createInsurance();
        Insurance insurance2 = Data.createInsurance();
        insurance2.setStatus(EInsuranceStatus.INACTIVE);
        insuranceRepository.saveAll(List.of(insurance1, insurance2));

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/insurances")
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.ADMIN))
                        .param("page", "1")
                        .param("status", "INACTIVE")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].status").value("INACTIVE"));
    }
}