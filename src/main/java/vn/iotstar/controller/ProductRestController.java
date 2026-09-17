package vn.iotstar.controller;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import vn.iotstar.dto.*;
import vn.iotstar.service.CatalogService;
@RestController
@RequestMapping({"/api/products","/api/product"})
@Tag(name="02. Product",description="CRUD, tìm kiếm, phân trang và lọc sản phẩm")
public class ProductRestController {
 private final CatalogService service;
 public ProductRestController(CatalogService s) {service=s;}
 @GetMapping public ApiResponse<PageDto<ProductDto>> list(@RequestParam(defaultValue="") String keyword,@RequestParam(required=false) Long categoryId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="5") int size,@RequestParam(defaultValue="id") String sort) {return ApiResponse.ok(service.products(keyword,categoryId,page,size,sort));}
 @GetMapping("/{id}") public ApiResponse<ProductDto> get(@PathVariable Long id) {return ApiResponse.ok(service.product(id));}
 @PostMapping public ResponseEntity<ApiResponse<ProductDto>> create(@Valid @RequestBody ProductInput in) {var p=service.saveProduct(null,in);return ResponseEntity.created(URI.create("/api/products/"+p.productId())).body(ApiResponse.ok(p));}
 @PutMapping("/{id}") public ApiResponse<ProductDto> update(@PathVariable Long id,@Valid @RequestBody ProductInput in) {return ApiResponse.ok(service.saveProduct(id,in));}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {service.deleteProduct(id);return ResponseEntity.noContent().build();}
}
