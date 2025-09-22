package nova.mjs.mentor.mentoring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/mentoring")
public class MentoringController {

    // 1. 멘토링 현황 summary

    // 2. 전체 멘토링 가능한 선배 조회

    // ----

    // 멘토링 상세 조회

    // 1. 멘토 정보 조회
    // 2. 멘토링 후기 조회
    // 3. 멘토링 신청하기 - webSocket의 시작
    /**
     * 이때 멘토한테는 멘토링 시작 여부로 state가 대기로 되어야하고,
     * 멘토이름, 멘티 이름, 보낼 텍스트 및 첨부파일이 시간에 따라 전달이 되어야함
     * 이때 기본적으로 멘토, 멘토링 주제(title), 멘토 직업이 프로필로 보임
     */



}
