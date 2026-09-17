package vn.iotstar.config;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
@Component
@ConditionalOnProperty(name="app.seed",havingValue="true")
public class DemoData implements CommandLineRunner {
 private static final String[] CATEGORY_NAMES={"Laptop","Điện thoại","Phụ kiện","Âm thanh","Màn hình","Thiết bị lưu trữ"};
 private static final String[] CATEGORY_IMAGES={"/images/categories/laptops.png","/images/categories/phones.png","/images/categories/accessories.png","/images/categories/audio.png","/images/categories/monitors.png","/images/categories/storage.png"};
 private static final String LEGACY_DESCRIPTION="Sản phẩm mẫu dùng cho bài thực hành CRUD, tìm kiếm và phân trang.";
 private static final String[] TITLES={"Laptop học tập 14 inch","Laptop đồ họa 15 inch","Điện thoại A1","Điện thoại B2","Bàn phím cơ","Chuột không dây","Tai nghe chụp tai","Loa Bluetooth","Màn hình 24 inch","Màn hình 27 inch","Ổ cứng SSD 500 GB","USB 64 GB"};
 private static final String[] IMAGES={"/images/products/laptop-study.svg","/images/products/laptop-creative.svg","/images/products/phone-a1.svg","/images/products/phone-b2.svg","/images/products/keyboard.svg","/images/products/mouse.svg","/images/products/headphones.svg","/images/products/speaker.svg","/images/products/monitor-24.svg","/images/products/monitor-27.svg","/images/products/ssd-500.svg","/images/products/usb-64.svg"};
 private static final String[] DESCRIPTIONS={"Laptop 14 inch gọn gàng cho góc học tập, soạn thảo tài liệu và học trực tuyến. Thiết kế thuận tiện mang theo giữa lớp học và nơi làm việc.","Laptop màn hình 15 inch tạo không gian rộng hơn khi chỉnh sửa hình ảnh, dựng bố cục và làm việc với nhiều cửa sổ. Phù hợp bố trí tại góc làm việc sáng tạo.","Điện thoại A1 với thiết kế tối giản, phù hợp các nhu cầu liên lạc, nhắn tin, tra cứu thông tin và giải trí hằng ngày. Kiểu dáng dễ phối cùng phụ kiện cá nhân.","Điện thoại B2 có kiểu dáng hiện đại, thích hợp sử dụng khi học tập, làm việc và giữ liên lạc. Lựa chọn dành cho người yêu thích thiết kế gọn và thanh lịch.","Bàn phím cơ dành cho góc học tập hoặc làm việc, giúp thao tác nhập liệu rõ ràng và thuận tiện. Bố cục phím dễ làm quen khi soạn thảo văn bản hoặc giải trí.","Chuột không dây giúp bàn làm việc gọn hơn, thuận tiện khi di chuyển giữa các vị trí sử dụng. Kiểu dáng vừa tay, phù hợp thao tác văn phòng và học tập.","Tai nghe chụp tai với phần đệm ôm quanh tai, phù hợp nghe nhạc, xem video hoặc học trực tuyến tại không gian riêng. Thiết kế quai đeo dễ kết hợp với góc làm việc.","Loa Bluetooth kết nối không dây với thiết bị tương thích để phát nhạc và nội dung âm thanh. Dáng loa gọn, dễ bố trí trên bàn, kệ sách hoặc trong phòng sinh hoạt.","Màn hình 24 inch dành cho bàn học hoặc bàn làm việc có diện tích vừa phải. Không gian hiển thị phù hợp đọc tài liệu, làm bảng tính và sử dụng cùng máy tính cá nhân.","Màn hình 27 inch mang lại không gian hiển thị rộng, thuận tiện quan sát nhiều cửa sổ và nội dung cùng lúc. Phù hợp góc làm việc cần màn hình lớn hơn.","Ổ cứng SSD dung lượng 500 GB dùng lưu hệ điều hành, ứng dụng và dữ liệu cá nhân. Cần kiểm tra chuẩn kết nối và khả năng tương thích của máy trước khi lắp đặt.","USB dung lượng 64 GB giúp mang theo tài liệu, hình ảnh và bài thuyết trình. Thiết kế nhỏ gọn, tiện cất trong túi và trao đổi dữ liệu với thiết bị có cổng phù hợp."};

 private final CategoryRepository categories;private final ProductRepository products;
 public DemoData(CategoryRepository c,ProductRepository p) {categories=c;products=p;}
 @Override @Transactional public void run(String...args) {
  if(categories.count()==0 && products.count()==0) {
  String[] names={"Laptop","Điện thoại","Phụ kiện","Âm thanh","Màn hình","Thiết bị lưu trữ"};
  Category[] cats=new Category[names.length];
  for(int i=0;i<names.length;i++) {Category c=new Category();c.setCategoryName(names[i]);c.setIcon(CATEGORY_IMAGES[i]);cats[i]=categories.save(c);}
  String[] titles={"Laptop học tập 14 inch","Laptop đồ họa 15 inch","Điện thoại A1","Điện thoại B2","Bàn phím cơ","Chuột không dây","Tai nghe chụp tai","Loa Bluetooth","Màn hình 24 inch","Màn hình 27 inch","Ổ cứng SSD 500 GB","USB 64 GB"};
  long[] prices={12990000,19990000,4590000,7990000,790000,290000,990000,690000,2590000,3990000,1190000,159000};
  for(int i=0;i<titles.length;i++) {Product p=new Product();p.setProductName(titles[i]);p.setQuantity(10+i*2);p.setUnitPrice(BigDecimal.valueOf(prices[i]));p.setImages(IMAGES[i]);p.setDescription(DESCRIPTIONS[i]);p.setDiscount(BigDecimal.ZERO);p.setCreateDate(LocalDateTime.now());p.setStatus((short)1);p.setCategory(cats[i/2]);products.save(p);}
  }
  // Bổ sung ảnh danh mục còn trống, không thay ảnh người dùng đã tải lên.
  for(Category c:categories.findAll()) {
   if(c.getIcon()!=null && !c.getIcon().isBlank() && !"/images/product.svg".equals(c.getIcon())) continue;
   for(int i=0;i<CATEGORY_NAMES.length;i++) {
    if(CATEGORY_NAMES[i].equalsIgnoreCase(c.getCategoryName().trim())) {
     c.setIcon(CATEGORY_IMAGES[i]);categories.save(c);break;
    }
   }
  }
  // Chỉ nâng cấp nội dung mẫu cũ; giữ nguyên ID, giá, số lượng, ảnh tự tải và nội dung người dùng đã sửa.
  for(Product p:products.findAll()) {
   for(int i=0;i<TITLES.length;i++) {
    if(!TITLES[i].equals(p.getProductName())) continue;
    boolean changed=false;
    if(p.getDescription()==null || p.getDescription().isBlank() || LEGACY_DESCRIPTION.equals(p.getDescription())) {
     p.setDescription(DESCRIPTIONS[i]);changed=true;
    }
    if(p.getImages()==null || p.getImages().isBlank() || "/images/product.svg".equals(p.getImages())) {
     p.setImages(IMAGES[i]);changed=true;
    }
    if(changed) products.save(p);
   }
  }
 }
}
