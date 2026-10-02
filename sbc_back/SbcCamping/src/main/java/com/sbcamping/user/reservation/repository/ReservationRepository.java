package com.sbcamping.user.reservation.repository;

import com.sbcamping.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository <Reservation, String> {
    @Query(value = """
        select r
        from Reservation r
        join fetch r.site
        where r.resStatus = '예약완료'
            and r.checkoutDate >= :today
        order by r.site.siteId, r.checkinDate, r.resId
    """)
    List<Reservation> getReservations(@Param("today") LocalDate today);

    @Query("""
       select count(r)
       from Reservation r
       where r.site.siteId = :siteId
           and r.resStatus = '예약완료'
           and r.checkinDate < :checkout
           and r.checkoutDate > :checkin
       """)
    long countOverlapping(
            @Param("siteId") Long siteId,
            @Param("checkin") LocalDate checkin,
            @Param("checkout") LocalDate checkout
    );


    List<Reservation> findByResStatus(String resStatus);

    // 마이페이지 - 나의 예약내역, 회원탈퇴에 사용
    @Query("SELECT r FROM Reservation r JOIN FETCH r.member m JOIN FETCH r.site s WHERE r.member.memberID = :memberId ORDER BY r.resId desc ")
    List<Reservation> findByMemberIdOrderByResId(@Param("memberId") Long memberId);

}
