package com.sbcamping.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "Reservation")
@Getter
@ToString
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    @Id
    @Column(name = "RES_ID", nullable = false, length = 10)
    private String resId; // 예약 번호

    @Column(name = "RES_USER_NAME", nullable = false, length = 10)
    private String resUserName; // 예약자 명

    @Column(name = "RES_USER_PHONE", nullable = false, length = 11)
    private String resUserPhone; // 예약자 핸드폰 번호

    @Column(name = "RES_PEOPLE", nullable = false, columnDefinition = "NUMBER(1,0)")
    private Long resPeople; // 인원수

    @Column(name = "CHECKIN_DATE", nullable = false)
    private LocalDate checkinDate; // 입실 날짜

    @Column(name = "CHECKOUT_DATE", nullable = false)

    private LocalDate checkoutDate; // 퇴실 날짜

    @Column(name = "RES_DATE", nullable = false)

    private LocalDate resDate;// 예약 날짜

    @Column(name = "RES_STATUS", nullable = false, length = 10)
    @Builder.Default
    private String resStatus = "예약완료"; // 예약 상태

    @Column(name = "RES_TOTAL_PAY", nullable = false, columnDefinition = "NUMBER(10,0)")
    private Long resTotalPay; // 결제금액

    @Column(name = "RES_CANCEL_DATE")
    private LocalDate resCancelDate; // 취소날짜

    @Column(name = "RES_CANCEL_REASON", length = 100)
    private String resCancelReason; // 취소사유

    @Column(name = "RES_REVIEW", nullable = false, length = 1)
    @Builder.Default
    private char resReview = 'N'; // 리뷰 작성 여부

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID", referencedColumnName = "MEMBER_ID")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "SITE_ID", referencedColumnName = "Site_ID")
    private Site site;

    public void changeResUserName(String resUserName) {
        this.resUserName = resUserName;
    }

    public void changeResUserPhone(String resUserPhone) {
        this.resUserPhone = resUserPhone;
    }

    public void changeResPeople(Long resPeople) {
        this.resPeople = resPeople;
    }

    public void changeResCancelReason(String resCancelReason) {
        this.resCancelReason = resCancelReason;
    }

}
