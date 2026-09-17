package vn.iotstar.dto;
import jakarta.validation.constraints.*;
public record CategoryInput(@NotBlank(message="Tên danh mục không được trống") @Size(max=150) String categoryName, @Size(max=255) String icon) {}
