package rw.auca.radinfotracker.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAudit;
import rw.auca.radinfotracker.model.dtos.NewInsuranceDTO;
import rw.auca.radinfotracker.model.dtos.NewPatientDTO;
import rw.auca.radinfotracker.model.enums.EAuditType;
import rw.auca.radinfotracker.model.enums.EInsuranceStatus;
import rw.auca.radinfotracker.model.enums.EPatientStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.repository.IPatientAuditRepository;
import rw.auca.radinfotracker.repository.IPatientRepository;
import rw.auca.radinfotracker.repository.IUserRepository;
import rw.auca.radinfotracker.utilities.Authorization;
import rw.auca.radinfotracker.utilities.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-it.properties"
)
@AutoConfigureMockMvc
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IPatientRepository patientRepository;

    @Autowired
    private IPatientAuditRepository patientAuditRepository;

    private Map<ERole, String> tokenMap;

    private final Authorization authorization;

    PatientControllerTest(@Autowired MockMvc mockMvc,
                          @Autowired ObjectMapper objectMapper,
                          @Autowired IUserRepository userRepository,
                          @Autowired PasswordEncoder passwordEncoder){
        this.authorization = new Authorization(mockMvc, objectMapper, userRepository, passwordEncoder);
    }

    @BeforeEach
    void setUp() throws Exception {
        tokenMap = authorization.initializeUserAccounts();
    }

    @AfterEach
    void tearDown() {
        patientRepository.deleteAll();
        authorization.destroy();
    }

    @Test
    void canRegisterNewPatient() throws Exception {
        NewPatientDTO dto = Data.patientDTO();

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/patients/register")
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.FRONT_DESK))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)));

        resultActions.andExpect(status().isOk());
    }

    @Test
    void throwsErrorWhenNonFrontDeskUserTriesToRegisterPatient() throws Exception {
        NewInsuranceDTO insuranceDTO = Data.insuranceDTO();

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/insurances/register")
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.TECHNICIAN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insuranceDTO)));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    void canSearchAllPatients() throws Exception {
        Patient patient = Data.createPatient();
        Patient patient2 = Data.createPatient();
        patient2.setStatus(EPatientStatus.INACTIVE);
        patientRepository.saveAll(List.of(patient, patient2));

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/patients")
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.FRONT_DESK))
                        .param("page", "1")
                        .param("status", "INACTIVE")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].status").value("INACTIVE"));
    }

    @Test
    void canGetPatientAuditsByPatientId() throws Exception {
        Patient patient = Data.createPatient();
        patient = patientRepository.save(patient);

        PatientAudit audit1 = new PatientAudit(patient, EAuditType.CREATE, UUID.randomUUID(), "Names", "Email", "Observation", null);
        PatientAudit audit2 = new PatientAudit(patient, EAuditType.UPDATE, UUID.randomUUID(), "Names", "Email", "Observation", null);

        patientAuditRepository.saveAll(List.of(audit1, audit2));

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/patients/{id}/audits", patient.getId())
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.ADMIN))
                        .param("page", "1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    void throwsErrorWhenNonAdminUserTriesToGetPatientAudits() throws Exception {
        Patient patient = Data.createPatient();
        patient = patientRepository.save(patient);

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/patients/{id}/audits", patient.getId())
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.FRONT_DESK))
                        .param("page", "1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    void canActivatePatient() throws Exception {
        Patient patient = Data.createPatient();
        patient.setStatus(EPatientStatus.INACTIVE);
        patient = patientRepository.save(patient);

        ResultActions resultActions = mockMvc
                .perform(put("/api/v1/patients/{id}/activate", patient.getId())
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.FRONT_DESK))
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void canDeactivatePatient() throws Exception {
        Patient patient = Data.createPatient();
        patient.setStatus(EPatientStatus.ACTIVE);
        patient = patientRepository.save(patient);

        ResultActions resultActions = mockMvc
                .perform(put("/api/v1/patients/{id}/deactivate", patient.getId())
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.FRONT_DESK))
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }
}