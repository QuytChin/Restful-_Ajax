package vn.iotstar.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import vn.iotstar.entity.Product;
public record ProductDto(Long productId,String productName,Integer quantity,BigDecimal unitPrice,String images,
 String description,BigDecimal discount,LocalDateTime createDate,Short status,CategoryDto category) {
 public static ProductDto from(Product p) {
  return new ProductDto(p.getProductId(),p.getProductName(),p.getQuantity(),p.getUnitPrice(),p.getImages(),p.getDescription(),p.getDiscount(),p.getCreateDate(),p.getStatus(),CategoryDto.from(p.getCategory()));
 }
}
