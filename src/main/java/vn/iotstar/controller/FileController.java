package vn.iotstar.controller;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import vn.iotstar.service.StorageService;
import vn.iotstar.dto.ApiResponse;
@RestController
public class FileController {
 private final StorageService storage;
 public FileController(StorageService storage) {this.storage=storage;}
 @PostMapping(value="/api/files",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public ApiResponse<Map<String,String>> upload(@RequestPart("file") MultipartFile file) {return ApiResponse.ok(Map.of("url",storage.store(file)));}
 @GetMapping("/uploads/{filename:.+}") public ResponseEntity<Resource> image(@PathVariable String filename) {
  Resource resource=storage.load(filename);
  MediaType type=filename.endsWith(".png")?MediaType.IMAGE_PNG:filename.endsWith(".gif")?MediaType.IMAGE_GIF:MediaType.IMAGE_JPEG;
  return ResponseEntity.ok().contentType(type).header("X-Content-Type-Options","nosniff").body(resource);
 }
}
