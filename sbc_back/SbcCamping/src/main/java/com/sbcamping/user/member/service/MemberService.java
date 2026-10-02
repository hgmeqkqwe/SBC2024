package com.sbcamping.user.member.service;

import com.sbcamping.domain.Member;
import com.sbcamping.domain.Reservation;

import java.util.List;
import java.util.Map;

public interface MemberService {
    void addMember(Member member);
    Member updateMember(Member member);
    Member getMember(Long memberId);
    String emailCheck(String memberEmail);
    String findEmail(String memberName, String memberPhone);
    Member findMemberByNameAndEmail(Member member);
    String updatePw(Member member);
    String authPw(Long memberId, String memberPw);
    List<Reservation> getMemberRes(Long memberId);
    Reservation getResDetail(String resId);
    void cancelRes(String resId, String reason);
    String withdraw(Long memberId, String memberPw);
    Map<String, Long> getReviewNo(String resID);
}
