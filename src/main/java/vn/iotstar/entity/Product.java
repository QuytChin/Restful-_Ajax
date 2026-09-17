package vn.iotstar.entity;
import jakarta.persistence.*;
@Entity
@Table(name="Products")
public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
 private Long productId;
 @org.hibernate.annotations.Nationalized @Column(nullable=false,length=500)
 private String productName;
 @Column(nullable=false)
 private Integer quantity;
 @Column(nullable=false,precision=18,scale=2)
 private java.math.BigDecimal unitPrice;
 @Column(length=255)
 private String images;
 @org.hibernate.annotations.Nationalized @Column(nullable=false,length=500)
 private String description;
 @Column(nullable=false,precision=5,scale=2)
 private java.math.BigDecimal discount;
 @Column(nullable=false)
 private java.time.LocalDateTime createDate;
 @Column(nullable=false)
 private Short status;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="category_id",nullable=false)
 private Category category;
 public Product() {}
 public Long getProductId() {return productId;}
 public void setProductId(Long value) {this.productId=value;}
 public String getProductName() {return productName;}
 public void setProductName(String value) {this.productName=value;}
 public Integer getQuantity() {return quantity;}
 public void setQuantity(Integer value) {this.quantity=value;}
 public java.math.BigDecimal getUnitPrice() {return unitPrice;}
 public void setUnitPrice(java.math.BigDecimal value) {this.unitPrice=value;}
 public String getImages() {return images;}
 public void setImages(String value) {this.images=value;}
 public String getDescription() {return description;}
 public void setDescription(String value) {this.description=value;}
 public java.math.BigDecimal getDiscount() {return discount;}
 public void setDiscount(java.math.BigDecimal value) {this.discount=value;}
 public java.time.LocalDateTime getCreateDate() {return createDate;}
 public void setCreateDate(java.time.LocalDateTime value) {this.createDate=value;}
 public Short getStatus() {return status;}
 public void setStatus(Short value) {this.status=value;}
 public Category getCategory() {return category;}
 public void setCategory(Category value) {this.category=value;}
}
