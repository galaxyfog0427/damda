package dev.damda;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
public class ContractExportTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("계약을 contract/openapi.yaml로 꺼낸다")
    void exportContract() throws Exception {
        String yaml = mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        Path path = Path.of("../contract/openapi.yaml");
        Files.createDirectories(path.getParent());
        Files.writeString(path, yaml);
    }
}
