package nova.mjs.util.s3;

import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.exception.request.RequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class S3ServiceImplTest {

    @Mock private S3Client s3Client;
    @Mock private S3Presigner s3Presigner;
    @Mock private MultipartFile file;
    @InjectMocks private S3ServiceImpl service;

    @Test
    void 리뷰_업로드는_지원하지_않는_MIME을_거부한다() {
        given(file.getContentType()).willReturn("application/pdf");

        assertThatThrownBy(() -> service.uploadFile(file, S3DomainType.REVIEW_MEDIA))
                .isInstanceOfSatisfying(RequestException.class, ex ->
                        assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.S3_PRESIGN_UNSUPPORTED_TYPE));
    }

    @Test
    void 리뷰_이미지는_10MB를_초과할_수_없다() {
        given(file.getContentType()).willReturn("image/jpeg");
        given(file.getSize()).willReturn(10L * 1024 * 1024 + 1);

        assertThatThrownBy(() -> service.uploadFile(file, S3DomainType.REVIEW_MEDIA))
                .isInstanceOfSatisfying(RequestException.class, ex ->
                        assertThat(ex.getErrorCode())
                                .isEqualTo(ErrorCode.S3_PRESIGN_SIZE_EXCEEDED));
    }
}
