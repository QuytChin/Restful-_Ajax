package vn.iotstar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:testcatalog;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.seed=false","app.storage=target/test-uploads"})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class CatalogIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 private Map<String,Object> product(String name,Long category,int price) {
  return Map.of("productName",name,"categoryId",category,"quantity",3,"unitPrice",price,"description","Dữ liệu kiểm thử","discount",0,"status",1);
 }

 private JsonNode call(String method,String path,Object body,int expected) throws Exception {
  var builder=request(org.springframework.http.HttpMethod.valueOf(method),path);
  if(body!=null) builder.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body));
  var response=mvc.perform(builder).andReturn().getResponse();
  assertEquals(expected,response.getStatus(),response.getContentAsString());
  return response.getContentAsByteArray().length==0?json.getNodeFactory().nullNode():json.readTree(response.getContentAsByteArray());
 }
 @Test void crudSearchPaginationValidationAndSwagger() throws Exception {
  long cat=call("POST","/api/categories",Map.of("categoryName","Nhóm kiểm thử"),201).path("body").path("categoryId").asLong();
  call("POST","/api/categories",Map.of("categoryName","  Nhóm kiểm thử  "),409);
  call("POST","/api/categories",Map.of("categoryName","   "),400);
  call("GET","/api/categories?page=-1",null,400);
  call("GET","/api/categories?size=101",null,400);
  long p1=call("POST","/api/products",product("Kiểm thử giá cao",cat,900),201).path("body").path("productId").asLong();
  long p2=call("POST","/api/products",product("Kiểm thử giá thấp",cat,100),201).path("body").path("productId").asLong();
  JsonNode page=call("GET","/api/products?keyword=Kiểm&size=1&sort=priceAsc&categoryId="+cat,null,200).path("body");
  assertEquals(2,page.path("totalElements").asInt());assertEquals(2,page.path("totalPages").asInt());assertEquals(p2,page.path("content").get(0).path("productId").asLong());
  JsonNode next=call("GET","/api/products?keyword=Kiểm&page=1&size=1&sort=priceAsc",null,200).path("body");
  assertEquals(p1,next.path("content").get(0).path("productId").asLong());
  call("DELETE","/api/categories/"+cat,null,409);
  assertEquals("Nhóm kiểm thử",call("GET","/api/products/"+p1,null,200).path("body").path("category").path("categoryName").asText());
  call("PUT","/api/products/"+p1,product("Đã sửa",cat,1200),200);
  assertEquals("Đã sửa",call("GET","/api/products/"+p1,null,200).path("body").path("productName").asText());
  var invalid=new HashMap<>(product("Lỗi số lượng",cat,100));invalid.put("quantity",-1);call("POST","/api/products",invalid,400);
  call("POST","/api/products",product("Sai danh mục",999999L,100),404);
  call("GET","/api/products?sort=unknown",null,400);
  assertTrue(call("GET","/v3/api-docs",null,200).path("paths").has("/api/products"));
  call("DELETE","/api/products/"+p1,null,204);call("GET","/api/products/"+p1,null,404);
  call("DELETE","/api/products/"+p2,null,204);
  call("PUT","/api/categories/"+cat,Map.of("categoryName","Danh mục đã sửa"),200);
  assertEquals(1,call("GET","/api/categories?keyword=đã sửa",null,200).path("body").path("totalElements").asInt());
  call("DELETE","/api/categories/"+cat,null,204);call("GET","/api/categories/"+cat,null,404);
 }
 @Test void legacyMultipartPreservesImageAndRejectsFakeImage() throws Exception {
  byte[] png=Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jhioAAAAASUVORK5CYII=");
  var file=new MockMultipartFile("icon","photo.png","image/png",png);
  var response=mvc.perform(multipart("/api/category/addCategory").file(file).param("categoryName","Ảnh thử")).andReturn().getResponse();
  assertEquals(201,response.getStatus());JsonNode c=json.readTree(response.getContentAsByteArray()).path("body");long id=c.path("categoryId").asLong();String icon=c.path("icon").asText();
  assertTrue(icon.startsWith("/uploads/"));assertEquals(200,mvc.perform(get(icon)).andReturn().getResponse().getStatus());
  JsonNode saved=call("PUT","/api/categories/"+id,Map.of("categoryName","Giữ ảnh"),200).path("body");assertEquals(icon,saved.path("icon").asText());
  var fake=new MockMultipartFile("file","fake.png","image/png","not an image".getBytes());
  assertEquals(400,mvc.perform(multipart("/api/files").file(fake)).andReturn().getResponse().getStatus());
  call("DELETE","/api/categories/"+id,null,204);
 }
}
