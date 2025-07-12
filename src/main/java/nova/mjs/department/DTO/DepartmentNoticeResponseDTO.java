package nova.mjs.department.DTO;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DepartmentNoticeResponseDTO {
    private DepartmentInfoDTO departmentInfo;
    private List<DepartmentNoticeDTO> notices;
}