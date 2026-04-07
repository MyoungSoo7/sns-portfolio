package lms.snsportfolio.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the SNS Portfolio service.
 *
 * <p>Swagger UI is exposed at {@code /swagger-ui/index.html} and the raw
 * OpenAPI document is available at {@code /v3/api-docs}.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SNS Portfolio API",
                version = "0.0.1-SNAPSHOT",
                description = "게시글, 댓글, 좋아요, 실시간 알림(SSE + Kafka) 기능을 제공하는 "
                        + "소셜 네트워크 서비스 REST API.",
                contact = @Contact(name = "sns-portfolio team", url = "https://github.com/lms/sns-portfolio"),
                license = @License(name = "Apache License 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development server"),
                @Server(url = "https://api.sns-portfolio.example.com", description = "Production server")
        }
)
public class OpenApiConfig {
}
