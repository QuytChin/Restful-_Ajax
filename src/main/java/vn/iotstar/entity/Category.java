package vn.iotstar.entity;
import jakarta.persistence.*;
@Entity
@Table(name="Categories")
public class Category {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
 private Long categoryId;
 @org.hibernate.annotations.Nationalized @Column(nullable=false,unique=true,length=150)
 private String categoryName;
 @Column(length=255)
 private String icon;
 public Category() {}
 public Long getCategoryId() {return categoryId;}
 public void setCategoryId(Long value) {this.categoryId=value;}
 public String getCategoryName() {return categoryName;}
 public void setCategoryName(String value) {this.categoryName=value;}
 public String getIcon() {return icon;}
 public void setIcon(String value) {this.icon=value;}
}
