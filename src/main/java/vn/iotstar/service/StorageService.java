package vn.iotstar.service;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.exception.BusinessException;
@Service
public class StorageService {
 private final Path root;
 public StorageService(@Value("${app.storage}") String dir) throws IOException {root=Path.of(dir).toAbsolutePath().normalize();Files.createDirectories(root);}
 public String store(MultipartFile file) {
  if(file.isEmpty()) throw new BusinessException(400,"Chưa chọn ảnh");
  if(file.getSize()>5*1024*1024) throw new BusinessException(413,"Ảnh không được vượt quá 5 MB");
  // Xác minh định dạng thực tế, không tin phần mở rộng do người dùng gửi.
  try(var stream=ImageIO.createImageInputStream(file.getInputStream())) {
   var readers=ImageIO.getImageReaders(stream);
   if(!readers.hasNext()) throw new BusinessException(400,"Chỉ nhận ảnh PNG, JPEG hoặc GIF hợp lệ");
   var reader=readers.next();
   String ext;
   try {reader.setInput(stream);ext=reader.getFormatName().toLowerCase(Locale.ROOT);
    if(!Set.of("png","jpeg","jpg","gif").contains(ext)||reader.getWidth(0)>10000||reader.getHeight(0)>10000) throw new BusinessException(400,"Ảnh không hợp lệ hoặc quá lớn");
   } finally {reader.dispose();}
   String filename=UUID.randomUUID()+"."+ext;
   try(var in=file.getInputStream()) {Files.copy(in,root.resolve(filename));}
   return "/uploads/"+filename;
  } catch(IOException e) {throw new BusinessException(500,"Không lưu được ảnh");}
 }
 public Resource load(String filename) {
  if(!filename.matches("[a-f0-9-]+\\.(png|jpg|jpeg|gif)")) throw new BusinessException(404,"Không tìm thấy ảnh");
  Path file=root.resolve(filename).normalize();
  if(!file.startsWith(root)||!Files.isRegularFile(file)) throw new BusinessException(404,"Không tìm thấy ảnh");
  return new FileSystemResource(file);
 }
}
