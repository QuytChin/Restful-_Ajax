package vn.iotstar.exception;
import vn.iotstar.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
@RestControllerAdvice
public class RestErrors {
 private ResponseEntity<ApiResponse<Object>> error(int status,String message) {return ResponseEntity.status(status).body(new ApiResponse<>(false,message,null));}
 @ExceptionHandler(BusinessException.class) public ResponseEntity<?> business(BusinessException e) {return error(e.getStatus(),e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validation(MethodArgumentNotValidException e) {return error(400,e.getBindingResult().getFieldErrors().stream().map(f->f.getField()+": "+f.getDefaultMessage()).sorted().reduce((a,b)->a+"; "+b).orElse("Dữ liệu không hợp lệ"));}
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,MissingServletRequestParameterException.class}) public ResponseEntity<?> invalid(Exception e) {return error(400,"Dữ liệu hoặc tham số không đúng định dạng");}
 @ExceptionHandler(MaxUploadSizeExceededException.class) public ResponseEntity<?> large(Exception e) {return error(413,"Ảnh không được vượt quá 5 MB");}
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> conflict(Exception e) {return error(409,"Dữ liệu bị trùng hoặc đang được sử dụng");}
}
