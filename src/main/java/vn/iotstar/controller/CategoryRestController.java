package vn.iotstar.controller;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import vn.iotstar.dto.*;
import vn.iotstar.service.*;
@RestController
@RequestMapping({"/api/categories","/api/category"})
@Tag(name="01. Category",description="CRUD, tìm kiếm và phân trang danh mục")
public class CategoryRestController {
 private final CatalogService service;private final StorageService storage;
 public CategoryRestController(CatalogService s,StorageService storage) {service=s;this.storage=storage;}
 @GetMapping public ApiResponse<PageDto<CategoryDto>> list(@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="5") int size) {return ApiResponse.ok(service.categories(keyword,page,size));}
 @GetMapping("/{id}") public ApiResponse<CategoryDto> get(@PathVariable Long id) {return ApiResponse.ok(service.category(id));}
 @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE) public ResponseEntity<ApiResponse<CategoryDto>> create(@Valid @RequestBody CategoryInput in) {var c=service.saveCategory(null,in);return ResponseEntity.created(URI.create("/api/categories/"+c.categoryId())).body(ApiResponse.ok(c));}
 @PutMapping(value="/{id}",consumes=MediaType.APPLICATION_JSON_VALUE) public ApiResponse<CategoryDto> update(@PathVariable Long id,@Valid @RequestBody CategoryInput in) {return ApiResponse.ok(service.saveCategory(id,in));}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {service.deleteCategory(id);return ResponseEntity.noContent().build();}
 // Endpoint tương thích cách gọi multipart trong tài liệu mục 2.
 @PostMapping("/getCategory") public ApiResponse<CategoryDto> legacyGet(@RequestParam Long id) {return get(id);}
 @PostMapping(value="/addCategory",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ResponseEntity<ApiResponse<CategoryDto>> legacyAdd(@RequestParam String categoryName,@RequestPart(value="icon",required=false) MultipartFile icon) {return create(new CategoryInput(categoryName,icon==null||icon.isEmpty()?null:storage.store(icon)));}
 @PutMapping(value="/updateCategory",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ApiResponse<CategoryDto> legacyUpdate(@RequestParam Long categoryId,@RequestParam String categoryName,@RequestPart(value="icon",required=false) MultipartFile icon) {return update(categoryId,new CategoryInput(categoryName,icon==null||icon.isEmpty()?null:storage.store(icon)));}
 @DeleteMapping("/deleteCategory") public ApiResponse<Object> legacyDelete(@RequestParam Long categoryId) {service.deleteCategory(categoryId);return ApiResponse.ok(null);}
}
