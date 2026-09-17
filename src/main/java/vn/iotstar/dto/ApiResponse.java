package vn.iotstar.dto;
public record ApiResponse<T>(boolean status,String message,T body) {
 public static <T> ApiResponse<T> ok(T body) {return new ApiResponse<>(true,"Thành công",body);}
}
