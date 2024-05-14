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
import rw.auca.radinfotracker.model.ImageType;
import rw.auca.radinfotracker.model.dtos.NewImageTypeDTO;
import rw.auca.radinfotracker.model.enums.EImageTypeStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.repository.IImageTypeRepository;
import rw.auca.radinfotracker.repository.IUserRepository;
import rw.auca.radinfotracker.utilities.Authorization;
import rw.auca.radinfotracker.utilities.Data;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
class ImageTypeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IImageTypeRepository imageTypeRepository;

    private Map<ERole, String> tokenMap;

    private final Authorization authorization;

    ImageTypeControllerTest(@Autowired MockMvc mockMvc,
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
        imageTypeRepository.deleteAll();
        authorization.destroy();
    }

    @Test
    void canRegisterNewImageType() throws Exception {
        NewImageTypeDTO imageTypeDTO = Data.imageTypeDTO();

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/imageTypes/register")
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imageTypeDTO)));

        resultActions.andExpect(status().isOk());
    }

    @Test
    void throwsErrorWhenNonAdminUserTriesToRegisterImageType() throws Exception {
        NewImageTypeDTO imageTypeDTO = Data.imageTypeDTO();

        ResultActions resultActions = mockMvc
                .perform(post("/api/v1/imageTypes/register")
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.TECHNICIAN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imageTypeDTO)));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    void canGetAllActiveImageTypes() throws Exception {
        ImageType imageType1 = Data.createImageType();
        ImageType imageType2 = Data.createImageType();
        ImageType imageType3 = Data.createImageType();
        imageType3.setStatus(EImageTypeStatus.INACTIVE);
        imageTypeRepository.saveAll(List.of(imageType1, imageType2, imageType3));

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/imageTypes/list")
                        .header("Authorization", "Bearer " + tokenMap.get(ERole.TECHNICIAN))
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void canSearchAllImageTypes() throws Exception {
        ImageType imageType1 = Data.createImageType();
        ImageType imageType2 = Data.createImageType();
        imageType2.setStatus(EImageTypeStatus.INACTIVE);
        imageTypeRepository.saveAll(List.of(imageType1, imageType2));

        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/imageTypes")
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