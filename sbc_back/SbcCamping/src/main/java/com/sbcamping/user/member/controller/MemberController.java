package com.sbcamping.user.member.controller;

import com.sbcamping.domain.Member;
import com.sbcamping.domain.Reservation;
import com.sbcamping.user.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController("userMemberController") //사용자 관련 멤버 컨트롤러
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping("/")
    public void join(@RequestBody Member member){
        memberService.addMember(member);
    }

    // 예약 상태 변경 (예약취소)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/{resID}/cancel")
    public void cancelReservation(@PathVariable String resID, @RequestBody Map<String,String> reason){
        log.info("----------- 예약 상태 변경 메소드 도착 ID : {} 이유 : {}", resID, reason);
        memberService.cancelRes(resID, reason.get("reason"));
    }

    // 나의 예약내역 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/reslist")
    public List<Reservation> getMemberReservations(@RequestBody Long memberId){
        return memberService.getMemberRes(memberId);
    }

    // 예약 상세 내역 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/res/{resID}")
    public Reservation getDetailMyRes(@PathVariable(name = "resID") String resID){
        return memberService.getResDetail(resID);
    }

    // 예약번호로 리뷰글 번호 찾기
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/review/{resId}")
    public Map<String, Long> getReviewNo(@PathVariable String resId){
        log.info("--------- 예약번호로 리뷰글 찾기 : {}", resId);
        return memberService.getReviewNo(resId);
    }

    // 회원정보 수정 페이지 들어가기 전 비밀번호 인증
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/pwauth")
    public Map<String, String> memberPwAuth(@RequestBody Map<String, Object> member){
        Long memberId = Long.parseLong(member.get("memberID").toString());
        String memberPw = member.get("memberPw").toString();
        log.info("------비밀번호 인증 메소드 : " + memberId, memberPw);
        String msg = memberService.authPw(memberId, memberPw);
        Map<String, String> map = new HashMap<>();
        map.put("msg", msg);
        return map;
    }

    // 회원정보 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{memberId}")
    public Map<String, Member> getMember(@PathVariable Long memberId){
        log.info("---------회원 조회 메서드 : {}", memberId);
        Member member = memberService.getMember(memberId);
        Map<String, Member> map = new HashMap<>();
        map.put("member", member);
        return map;
    }

    // 회원정보 수정
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/mod")
    public Map<String, Member> modifyMember(@RequestBody Member member){
        //log.info("---------- 회원정보 수정 : " + member);
        Member memResult = memberService.updateMember(member);
        Map<String, Member> map = new HashMap<>();
        map.put("member", memResult);
        return map;
    }

    // 회원 탈퇴(삭제)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/withdraw")
    public Map<String, String> withdraw(@RequestBody Map<String, Object> member){
        Long memberId = Long.parseLong(member.get("memberId").toString());
        String memberPw = member.get("memberPw").toString();
        String result = memberService.withdraw(memberId, memberPw);
        Map<String, String> map = new HashMap<>();
        map.put("msg", result); // 회원 상태 바뀌면 success 아니면 fail
        return map;
    }


}
