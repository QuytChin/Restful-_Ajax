package vn.iotstar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.config.DemoData;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:upgrade;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.seed=false","app.storage=target/test-uploads"})
@ActiveProfiles("demo")
@Transactional
class DemoDataUpgradeTest {
 @Autowired CategoryRepository categories;
 @Autowired ProductRepository products;
 @Test void upgradeOldSamplesAndPreservePersonalEdits() throws Exception {
  Category c=new Category();c.setCategoryName("Laptop");c.setIcon("");categories.save(c);
  Product p=new Product();p.setProductName("Laptop học tập 14 inch");p.setQuantity(37);p.setUnitPrice(new BigDecimal("123456.00"));p.setDiscount(BigDecimal.ZERO);p.setStatus((short)1);p.setCategory(c);p.setCreateDate(LocalDateTime.now());p.setImages("");p.setDescription("Sản phẩm mẫu dùng cho bài thực hành CRUD, tìm kiếm và phân trang.");products.saveAndFlush(p);
  Long id=p.getProductId();
  DemoData upgrade=new DemoData(categories,products);upgrade.run();products.flush();
  Product saved=products.findById(id).orElseThrow();
  assertEquals("/images/categories/laptops.png",categories.findById(c.getCategoryId()).orElseThrow().getIcon());
  assertEquals("/images/products/laptop-study.svg",saved.getImages());
  assertTrue(saved.getDescription().startsWith("Laptop 14 inch"));
  assertEquals(37,saved.getQuantity());assertEquals(0,new BigDecimal("123456.00").compareTo(saved.getUnitPrice()));
  assertEquals(c.getCategoryId(),saved.getCategory().getCategoryId());assertEquals(1,products.count());
  c.setIcon("/uploads/22222222-2222-2222-2222-222222222222.png");categories.saveAndFlush(c);
  saved.setDescription("Mô tả tôi đã tự chỉnh");saved.setImages("/uploads/11111111-1111-1111-1111-111111111111.png");products.saveAndFlush(saved);
  upgrade.run();products.flush();
  Product after=products.findById(id).orElseThrow();
  assertEquals("/uploads/22222222-2222-2222-2222-222222222222.png",categories.findById(c.getCategoryId()).orElseThrow().getIcon());
  assertEquals("Mô tả tôi đã tự chỉnh",after.getDescription());
  assertEquals("/uploads/11111111-1111-1111-1111-111111111111.png",after.getImages());
  assertEquals(id,after.getProductId());assertEquals(1,products.count());
 }
}
