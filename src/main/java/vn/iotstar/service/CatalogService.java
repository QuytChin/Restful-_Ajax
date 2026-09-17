package vn.iotstar.service;
import java.time.LocalDateTime;
import java.util.*;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import vn.iotstar.entity.*;
import vn.iotstar.dto.*;
import vn.iotstar.repository.*;
import vn.iotstar.exception.BusinessException;

@Service
@Transactional(readOnly=true)
public class CatalogService {
 private final CategoryRepository categories;
 private final ProductRepository products;
 private final Validator validator;
 public CatalogService(CategoryRepository c,ProductRepository p,Validator v) {categories=c;products=p;validator=v;}
 private void validate(Object input) {
  if(input==null) throw new BusinessException(400,"Thiếu dữ liệu");
  var errors=validator.validate(input);
  if(!errors.isEmpty()) throw new BusinessException(400,errors.stream().map(e->e.getPropertyPath()+": "+e.getMessage()).sorted().reduce((a,b)->a+"; "+b).orElse("Dữ liệu không hợp lệ"));
 }
 private PageRequest paging(int page,int size,Sort sort) {
  if(page<0||size<1||size>100) throw new BusinessException(400,"page phải >= 0; size từ 1 đến 100");
  return PageRequest.of(page,size,sort);
 }
 private String term(String keyword) {return keyword==null?"":keyword.trim();}
 private Category categoryEntity(Long id) {return categories.findById(id).orElseThrow(()->new BusinessException(404,"Không tìm thấy danh mục #"+id));}
 private Product productEntity(Long id) {return products.findById(id).orElseThrow(()->new BusinessException(404,"Không tìm thấy sản phẩm #"+id));}
 public CategoryDto category(Long id) {return CategoryDto.from(categoryEntity(id));}
 public ProductDto product(Long id) {return ProductDto.from(productEntity(id));}
 public PageDto<CategoryDto> categories(String keyword,int page,int size) {
  return PageDto.from(categories.findByCategoryNameContainingIgnoreCase(term(keyword),paging(page,size,Sort.by("categoryId"))).map(CategoryDto::from));
 }
 private Specification<Product> filter(String keyword,Long categoryId) {
  String escaped=term(keyword).toLowerCase(Locale.ROOT).replace("\\","\\\\").replace("%","\\%").replace("_","\\_");
  return (root,query,cb)-> {
   var name=cb.like(cb.lower(root.get("productName")),"%"+escaped+"%",'\\');
   return categoryId==null?name:cb.and(name,cb.equal(root.get("category").get("categoryId"),categoryId));
  };
 }
 private Sort order(String sort) {
  return switch(sort==null?"id":sort) {
   case "priceAsc" -> Sort.by("unitPrice").ascending().and(Sort.by("productId"));
   case "priceDesc" -> Sort.by("unitPrice").descending().and(Sort.by("productId"));
   case "id" -> Sort.by("productId");
   default -> throw new BusinessException(400,"sort phải là id, priceAsc hoặc priceDesc");
  };
 }
 public PageDto<ProductDto> products(String keyword,Long categoryId,int page,int size,String sort) {
  return PageDto.from(products.findAll(filter(keyword,categoryId),paging(page,size,order(sort))).map(ProductDto::from));
 }
 @Transactional public CategoryDto saveCategory(Long id,CategoryInput in) {
  validate(in);
  Category c=id==null?new Category():categoryEntity(id);
  String name=in.categoryName().trim();
  boolean duplicate=id==null?categories.existsByCategoryNameIgnoreCase(name):categories.existsByCategoryNameIgnoreCaseAndCategoryIdNot(name,id);
  if(duplicate) throw new BusinessException(409,"Tên danh mục đã tồn tại");
  c.setCategoryName(name);
  // Không chọn ảnh mới khi sửa thì giữ ảnh cũ; chuỗi rỗng là yêu cầu bỏ ảnh.
  if(in.icon()!=null) c.setIcon(imagePath(in.icon()));
  return CategoryDto.from(categories.saveAndFlush(c));
 }
 @Transactional public ProductDto saveProduct(Long id,ProductInput in) {
  validate(in);
  Product p=id==null?new Product():productEntity(id);
  p.setProductName(in.productName().trim());p.setQuantity(in.quantity());p.setUnitPrice(in.unitPrice());
  if(in.images()!=null) p.setImages(imagePath(in.images()));
  p.setDescription(in.description().trim());p.setDiscount(in.discount());p.setStatus(in.status());p.setCategory(categoryEntity(in.categoryId()));
  if(id==null) p.setCreateDate(LocalDateTime.now());
  return ProductDto.from(products.saveAndFlush(p));
 }
 private String imagePath(String value) {
  if(value.isBlank()) return "";
  if(java.util.Set.of("/images/categories/laptops.png","/images/categories/phones.png","/images/categories/accessories.png","/images/categories/audio.png","/images/categories/monitors.png","/images/categories/storage.png","/images/categories/default.png","/images/products/laptop-study.svg","/images/products/laptop-creative.svg","/images/products/phone-a1.svg","/images/products/phone-b2.svg","/images/products/keyboard.svg","/images/products/mouse.svg","/images/products/headphones.svg","/images/products/speaker.svg","/images/products/monitor-24.svg","/images/products/monitor-27.svg","/images/products/ssd-500.svg","/images/products/usb-64.svg","/images/categories/laptops.svg","/images/categories/phones.svg","/images/categories/accessories.svg","/images/categories/audio.svg","/images/categories/monitors.svg","/images/categories/storage.svg").contains(value)) return value;
  if(!value.matches("/uploads/[a-f0-9-]+\\.(png|jpg|jpeg|gif|webp)")) throw new BusinessException(400,"Hãy chọn ảnh bằng nút tải ảnh");
  return value;
 }
 @Transactional public void deleteCategory(Long id) {
  Category c=categoryEntity(id);
  if(products.existsByCategoryCategoryId(id)) throw new BusinessException(409,"Danh mục đang có sản phẩm. Hãy chuyển hoặc xóa sản phẩm trước.");
  categories.delete(c);categories.flush();
 }
 @Transactional public void deleteProduct(Long id) {products.delete(productEntity(id));products.flush();}
}
