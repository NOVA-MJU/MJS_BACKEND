package nova.mjs.util.s3;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * S3 Key 생성 유틸리티 클래스
 *
 * - 도메인별 prefix 생성
 * - 파일 해시 기반 S3 Key 생성
 */
public class S3KeyGenerator {

    /**
     * 도메인 유형과 UUID를 조합하여 S3 prefix 생성
     *
     * @param domainType 도메인 구분 Enum
     * @param folderUuid 폴더 구분용 UUID
     * @return 예: boards/temp/{UUID}/
     */
    public static String generatePrefix(S3DomainType domainType, UUID folderUuid) {
        return domainType.getPrefix() + folderUuid + "/";
    }

    /**
     * 파일 명을 노출시키지 않기위해 SHA-256 해시 기반으로 S3 Key(파일명)으로 생성 (확장자 자동 추출)
     *
     * @param file MultipartFile
     * @param domainType 도메인 구분 Enum
     * @param folderUuid 폴더 구분용 UUID
     * @return 예: boards/temp/{UUID}/{SHA-256}.{ext}
     * @throws IOException
     */
    public static String generateFileKeyWithHash(MultipartFile file, S3DomainType domainType, UUID folderUuid) throws IOException {
        String prefix = generatePrefix(domainType, folderUuid);
        String extension = getExtension(Objects.requireNonNull(file.getOriginalFilename()));
        String fileHash = DigestUtils.sha256Hex(file.getInputStream());
        return prefix + fileHash + extension;
    }

    //파일의 마지막 .를 추출하여 확장자 추출하기
    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex != -1 ? fileName.substring(dotIndex) : "";
    }
}
