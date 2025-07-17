package nova.mjs.util.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;              // HTTP 상태 코드
    private T data;                  // 실제 데이터
    private LocalDateTime timestamp; // 응답 시간

    // 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("API 요청 성공", data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("API 요청 성공", null, LocalDateTime.now());
    }
}
