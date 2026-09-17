package vn.iotstar.dto;
import vn.iotstar.entity.Category;
public record CategoryDto(Long categoryId,String categoryName,String icon) {
 public static CategoryDto from(Category c) {return new CategoryDto(c.getCategoryId(),c.getCategoryName(),c.getIcon());}
}
