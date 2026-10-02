package com.sbcamping.user.member.service;

import com.sbcamping.domain.Member;
import com.sbcamping.domain.Reservation;
import com.sbcamping.domain.Review;
import com.sbcamping.user.member.repository.MemberRepository;
import com.sbcamping.user.reservation.repository.ReservationRepository;
import com.sbcamping.user.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    private final ReservationRepository reservationRepository;

    private final ReviewRepository reviewRepository;

    private final PasswordEncoder passwordEncoder;

    // 예약번호로 리뷰 글 번호 가져오기
    @Override
    public Map<String, Long> getReviewNo(String resID) {
        Review re = reviewRepository.findByResId(resID);
        Long reviewId = re.getReviewID();
        Map<String, Long> map = new HashMap<>();
        map.put("reviewId", reviewId);
        return map;
    }

    // 회원 비활동 처리(탈퇴)
    @Override
    public String withdraw(Long memberId, String memberPw) {
        log.info("------- withdraw memberId : {} memberPw : {}", memberId, memberPw.substring(7));
        Member member = memberRepository.findById(memberId).orElse(null);
        String password = Objects.requireNonNull(member).getMemberPw();
        boolean result = passwordEncoder.matches(memberPw, password);
        String msg;

        // member 를 찾을 수 없거나 비밀번호가 일치하지 않으면 fail
        if (!result) {
            msg = "fail";
            return msg;
        }

        // 비밀번호가 일치하는 경우
        List<Reservation> resList = reservationRepository.findByMemberIdOrderByResId(memberId);
        LocalDate today = LocalDate.now();
        // 예약정보가 "예약완료"인 경우(예약취소는 제외)
        for (Reservation reservation : resList) {
            String status = reservation.getResStatus();
            if (status.equals("예약완료")) {
                LocalDate checkoutDate = reservation.getCheckoutDate();
                // 퇴실일 날짜가 오늘보다 이후 날짜면 탈퇴 불가능
                if (checkoutDate.isAfter(today)) {
                    msg = "fail";
                    return msg;
                }
            }
        }
        member.changeStatus("WITHDRAWN");
        member.changePhone("00000000000");
        memberRepository.save(member);
        msg = "success";

        return msg;
    }

    // 예약 상태 변경
    @Override
    public void cancelRes(String resId, String reason) {
        Reservation res = reservationRepository.findById(resId).orElse(null);
        Objects.requireNonNull(res, "reservation is null").setResStatus("예약취소");
        res.setResCancelDate(LocalDate.now());
        // 예약 취소 사유도 추가
        res.setResCancelReason(reason);
        reservationRepository.save(res);
    }

    // 예약 내역 상세 조회
    @Override
    public Reservation getResDetail(String resId) {
        Reservation res = reservationRepository.findById(resId).orElse(null);
        log.info("res : {}", res);
        return res;
    }

    // 예약내역 가져오기
    @Override
    public List<Reservation> getMemberRes(Long memberId) {
        List<Reservation> list = reservationRepository.findByMemberIdOrderByResId(memberId);
        log.info("예약내역 : {}", list);
        return list;
    }




    // 비밀번호 인증 (회원정보수정 들어갈 때 사용)
    @Override
    public String authPw(Long memberId, String memberPw) {
        log.info("memberId : {} memberPw : {}", memberId, memberPw);
        Member member = memberRepository.findById(memberId).orElse(null);
        String password = Objects.requireNonNull(member).getMemberPw();
        boolean result = passwordEncoder.matches(memberPw, password);
        String msg;
        if (!result) {
            msg = "fail";
        } else {
            msg = "success";
        }
        return msg;
    }

    // 회원명 + 이메일로 회원 찾기 (비밀번호 찾기 1)
    @Override
    public Member findMemberByNameAndEmail(Member member) {
        return memberRepository.findByMemberNameAndMemberEmail(member.getMemberName(), member.getMemberEmail());
    }

    // 회원 비밀번호 변경 (비밀번호 찾기 2)
    @Override
    public String updatePw(Member mem) {
        // ID로 member 조회
        Member member = memberRepository.findById(mem.getMemberID()).orElse(null);
        String msg;
        log.info("회원 상태 : {}", Objects.requireNonNull(member, "Member is null !!").getMemberStatus());
        if(member.getMemberStatus().equals("OFF")){
            msg = "fail";
            return msg;
        }
        if (Objects.requireNonNull(member).getMemberPw() != null && member.getMemberEmail() != null) {
            // 비밀번호 변경
            member.changePw(passwordEncoder.encode(mem.getMemberPw()));
            memberRepository.save(member);
            msg = "success";
        } else {
            msg = "fail";
        }
        return msg;
    }

    // 이메일 찾기 (회원명 + 회원 핸드폰번호)
    @Override
    public String findEmail(String memberName, String memberPhone) {
        Member member = memberRepository.findByMemberNameAndMemberPhone(memberName, memberPhone);
        log.info("이메일찾기 : {}", member.toString());
        return member.getMemberEmail();
    }


    // 회원 등록
    @Override
    public void addMember(Member member) {
        String pw = passwordEncoder.encode(member.getMemberPw());
        member.changePw(pw);
        memberRepository.save(member);
    }

    // 회원가입시 이메일 중복 체크
    @Override
    public String emailCheck(String memberEmail) {
        Integer count = memberRepository.countByMemberEmail(memberEmail);
        String msg;
        if (count == 0) {
            msg = "enable";
        } else {
            msg = "disable";
        }
        return msg;
    }

    // 회원 정보 수정
    @Override
    public Member updateMember(Member newMember) {

        // 회원번호로 회원 조회
        Member member = memberRepository.findById(newMember.getMemberID()).get();

        // 수정할 값이 있으면 수정
        if (newMember.getMemberPhone() != null && (!newMember.getMemberPhone().equals(member.getMemberPhone()))) {
            member.changePhone(newMember.getMemberPhone());
            log.info("핸드폰 번호 수정");
        }
        if (newMember.getMemberBirth() != null && (!newMember.getMemberBirth().equals(member.getMemberBirth()))) {
            member.changeBirth(newMember.getMemberBirth());
            log.info("생년월일 수정");
        }
        Character gender = newMember.getMemberGender();
        if ((!gender.equals(member.getMemberGender()))) {
            member.changeGender(newMember.getMemberGender());
            log.info("성별 수정");
        }
        if (newMember.getMemberLocal() != null && (!newMember.getMemberLocal().equals(member.getMemberLocal()))) {
            member.changeLocal(newMember.getMemberLocal());
            log.info("지역 수정");
        }
        if (newMember.getMemberName() != null && (!newMember.getMemberName().equals(member.getMemberName()))) {
            member.changeName(newMember.getMemberName());
            log.info("이름 수정");
        }
        log.info("mem pw :{}", newMember.getMemberPw());
        if (newMember.getMemberPw() != null && (!newMember.getMemberPw().equals("none")) && (!passwordEncoder.matches(newMember.getMemberPw(), member.getMemberPw()))) {
            member.changePw(passwordEncoder.encode(newMember.getMemberPw()));
            log.info("비밀번호 수정");
        }

        // 회원정보 수정
        return memberRepository.save(member);
    }

    // 회원 조회
    @Override
    public Member getMember(Long memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        return member.orElse(null);
    }

}
