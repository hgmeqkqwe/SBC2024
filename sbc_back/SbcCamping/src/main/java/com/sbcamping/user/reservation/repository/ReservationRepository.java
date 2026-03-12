package com.sbcamping.user.reservation.repository;

import com.sbcamping.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository <Reservation, Long> {

//    @Query(value = """
//                with date_range as (
//                    select res_id,
//                    TRUNC(FROM_TZ(CAST(checkin_date AS TIMESTAMP), 'UTC') AT TIME ZONE 'Asia/Seoul') AS checkin_date_KST,
//                    TRUNC(FROM_TZ(CAST(checkout_date AS TIMESTAMP), 'UTC') AT TIME ZONE 'Asia/Seoul') AS checkout_date_KST,
//                    site_id,
//                    FROM_TZ(CAST(checkin_date AS TIMESTAMP), 'UTC') AT TIME ZONE 'Asia/Seoul' + (level - 1) as date_seq
//                    from reservation
//                    where TRUNC(CHECKOUT_DATE) >= TRUNC(sysdate)
//                    AND RES_STATUS = '예약완료'
//                    connect by level <= TRUNC(FROM_TZ(CAST(checkout_date AS TIMESTAMP), 'UTC') AT TIME ZONE 'Asia/Seoul')
//                                        -
//                                        TRUNC(FROM_TZ(CAST(checkin_date AS TIMESTAMP), 'UTC') AT TIME ZONE 'Asia/Seoul') + 1
//                                        AND prior res_id = res_id
//                                        AND prior dbms_random.value is not null
//                    )
//                    select SITE_ID, checkin_date_KST, checkout_date_KST, date_seq,
//                    case
//                    when date_seq < checkout_date_KST then 'true'
//                    else 'false'
//                    END as result
//                    from date_range
//            """, nativeQuery = true)
//    List<Object[]> getReservations();

//    @Query(value = """
//                WITH RECURSIVE date_range(res_id, site_id, checkin_date, checkout_date, date_seq) AS (
//                        -- 1. 시작점: 3월 12일 (첫 번째 행)
//                    SELECT
//                        res_id,
//                        site_id,
//                        CAST(checkin_date AS DATE),
//                        CAST(checkout_date AS DATE),
//                        CAST(checkin_date AS DATE) -- 여기서 12일이 생성됨
//                    FROM reservation
//                    WHERE CAST(checkout_date AS DATE) >= CURRENT_DATE\s
//                        AND res_status = '예약완료'
//                    UNION ALL
//                        -- 2. 재귀: 하루씩 더하기 (두 번째 행 이상)
//                    SELECT
//                        res_id,
//                        site_id,
//                        checkin_date,
//                        checkout_date,
//                        CAST(date_seq + 1 AS DATE) -- 하루를 더함
//                    FROM date_range
//                    -- 핵심 조건: 다음 날(date_seq + 1)이 체크아웃 날짜보다 작을 때까지만 반복
//                    WHERE CAST(date_seq + 1 AS DATE) < checkout_date)
//                    SELECT
//                        site_id,\s
//                        date_seq AS reserved_date, -- 여기에 12일, 13일이 각각 찍힘
//                        'true' AS is_reserved
//                    FROM date_range
//                    ORDER BY site_id, reserved_date;
//            """, nativeQuery = true)
//    List<Object[]> getReservations();

    @Query(value = """
                WITH RECURSIVE date_range(res_id, site_id, checkin_date, checkout_date, date_seq) AS (
                    -- 1. 시작점: 3월 12일 (첫 번째 행)
                    SELECT
                        res_id,
                        site_id,
                        checkin_date,
                        checkout_date,
                        CAST(TRUNC(checkin_date) AS DATE) -- 시작 날짜
                    FROM reservation
                    WHERE TRUNC(checkout_date) >= TRUNC(CURRENT_DATE)
                        AND res_status = '예약완료'
                    UNION ALL
                    -- 2. 재귀: 하루씩 더해서 행 복제
                    SELECT\s
                        res_id,
                        site_id,
                        checkin_date,
                        checkout_date,
                        CAST(date_seq + 1 AS DATE)
                    FROM date_range
                    -- 핵심 조건: 다음 날짜(date_seq + 1)가 체크아웃 날짜(14일)보다 작을 때까지만!
                    -- 이렇게 하면 12일, 13일까지만 생성되고 14일은 생성되지 않습니다.
                    WHERE CAST(date_seq + 1 AS DATE) < CAST(TRUNC(checkout_date) AS DATE)
                )
                SELECT
                    site_id,
                    TO_CHAR(date_seq, 'YYYY-MM-DD') AS checkin_date_KST,\s
                    TO_CHAR(checkout_date, 'YYYY-MM-DD') AS checkout_date_KST,\s
                    TO_CHAR(date_seq, 'YYYY-MM-DD') AS date_seq,
                    'true' AS result -- 기존 result 컬럼 유지
                FROM date_range
                ORDER BY site_id, date_seq;
            """, nativeQuery = true)
    List<Object[]> getReservations();

    List<Reservation> findByResStatus(String resStatus);

    // 마이페이지 - 나의 예약내역, 회원탈퇴에 사용
    @Query("SELECT r FROM Reservation r JOIN FETCH r.member m JOIN FETCH r.site s WHERE r.member.memberID = :memberId ORDER BY r.resId desc ")
    List<Reservation> findByMemberIdOrderByResId(@Param("memberId") Long memberId);

}
