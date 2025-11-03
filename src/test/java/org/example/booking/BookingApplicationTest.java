package org.example.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BookingApplicationTest {

	@Autowired
	private MockMvc mockMvc;

	@Value("${springdoc.api-docs.path}.yaml")
	private String apiDocsPath;

	@Test
	void loadContextAndGenerateApiDocs() throws Exception {
		String openApiYaml = mockMvc.perform(get(apiDocsPath))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		Path outputDir = Paths.get(System.getProperty("user.dir"));
		Path outputPath = outputDir.resolve("booking-api.yaml");

		try (PrintWriter writer = new PrintWriter(outputPath.toFile(), StandardCharsets.UTF_8)) {
			writer.println(openApiYaml);
		}
	}
}
