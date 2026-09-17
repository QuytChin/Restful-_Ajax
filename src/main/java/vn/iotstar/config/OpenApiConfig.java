package vn.iotstar.config;
import org.springframework.context.annotation.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
@Configuration
public class OpenApiConfig {
 @Bean public OpenAPI catalogApi() {return new OpenAPI().info(new Info().title("Category & Product API").version("1.0").description("Mục 2: CRUD API; mục 3: Swagger/OpenAPI 3; mục 4: RESTful + AJAX. Phân trang bắt đầu từ page=0."));}
}
