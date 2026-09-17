package vn.iotstar.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ProductInput(
 @NotBlank(message="Tên sản phẩm không được trống") @Size(max=500) String productName,
 @NotNull @Min(0) Integer quantity,
 @NotNull @DecimalMin("0.00") @Digits(integer=16,fraction=2) BigDecimal unitPrice,
 @Size(max=255) String images,
 @NotNull @Size(max=500) String description,
 @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer=3,fraction=2) BigDecimal discount,
 @NotNull @Min(0) @Max(1) Short status,
 @NotNull @Positive Long categoryId) {}
