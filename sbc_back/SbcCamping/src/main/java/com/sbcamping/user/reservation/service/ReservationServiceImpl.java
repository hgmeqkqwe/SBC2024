package com.sbcamping.user.reservation.service;

import com.sbcamping.admin.site.repository.SiteRepository;
import com.sbcamping.domain.Member;
import com.sbcamping.domain.Reservation;
import com.sbcamping.domain.Site;
import com.sbcamping.user.member.repository.MemberRepository;
import com.sbcamping.user.reservation.dto.ReservationDTO;
import com.sbcamping.user.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
@EnableScheduling // 스케줄링 활성화
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private final ReservationRepository reservationRepository;

    @Autowired
    private final SiteRepository siteRepository;

    @Autowired
    private final MemberRepository memberRepository;

    private static int number = 1;

    @Transactional
    @Override
    public Reservation register(ReservationDTO reservationDTO) {
        if (reservationDTO.getMember() == null || reservationDTO.getSite() == null) {
            throw new IllegalArgumentException("Member ID and Site ID must not be null");
        }

        Long memberId = reservationDTO.getMember().getMemberId();
        Long siteId = reservationDTO.getSite().getSiteId();

        log.info("MemberId: ", memberId);
        log.info("SiteId: ", siteId);

        // 예약번호를 생성해서 만듬
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String formattedDate = date.format(formatter);

        String formattedNumber = String.format("%02d", number);
        number++;

        String resId = formattedDate + formattedNumber;

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        LocalDate checkin = reservationDTO.getCheckinDate();
        LocalDate checkout = reservationDTO.getCheckoutDate();

        if (checkin == null || checkout == null || !checkin.isBefore(checkout)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,"퇴실일은 입실일보다 늦어야 합니다."
            );
        }

        // 구역 잠금 획득
        Site site = siteRepository.findByIdForUpdate(siteId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "구역이 없습니다."
                        ));

        // 잠금을 획득한 뒤 겹치는 예약 확인
        if (reservationRepository.countOverlapping(
                siteId, checkin, checkout) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "다른 사용자가 먼저 예약했습니다."
            );
        }
        log.info("--------------------------------------------------------------------");
        log.info(member);
        log.info("--------------------------------------------------------------------");
        log.info(site);

        Reservation reservation = Reservation.builder()
                .resId(resId)
                .resUserName(reservationDTO.getResUserName())
                .resUserPhone(reservationDTO.getResUserPhone())
                .resPeople(reservationDTO.getResPeople())
                .checkinDate(reservationDTO.getCheckinDate())
                .checkoutDate(reservationDTO.getCheckoutDate())
                .resTotalPay(reservationDTO.getResTotalPay())
                .resDate(LocalDate.now())
                .member(member)
                .site(site)
                .build();


        reservationRepository.save(reservation);

        return reservation;
    }

    @Override
    public List<Site> getSite() {

        return siteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getResCheck() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<Reservation> reservations = reservationRepository.getReservations(today);

        List<Object[]> result = new ArrayList<>();

        for (Reservation reservation : reservations) {
            LocalDate checkout = reservation.getCheckoutDate();

            // 입실일부터 퇴실일 전날까지 날짜별 결과 생성
            for (LocalDate day = reservation.getCheckinDate();
                day.isBefore(checkout);
                day = day.plusDays(1)) {

                result.add(new Object[] {
                        reservation.getSite().getSiteId(),
                        day.toString(),
                        checkout.toString(),
                        day.toString(),
                        "true"
                });
            }
        }

        // 기존 쿼리처럼 구역 숙박 날짜 순으로 정렬
        result.sort(
                Comparator.<Object[], Long>comparing(row -> (Long) row[0])
                        .thenComparing(row -> (String) row[3])
        );

        return result;
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 자정마다 함수 호출
    public void resStatusCheck() {

        // 현재 날짜 구하기
        LocalDate now = LocalDate.now();

        // RES_STATUS가 예약완료인 예약들 가져오기
        List<Reservation> reservations = reservationRepository.findByResStatus("예약완료");

        reservations.forEach(reservation ->  {

            /**
             * checkout_Date가 하루 이전이거나 같은날인지
             * 현재날짜가 2024-10-23 이고 checkoutDate가 2024-10-22 일경우
             * 10-22 이전 까지는 true 반환해 사용완료로 바뀐다.
             * isBefore 10-21 까지 true
             * isEqual 10-22 true
             * @Param checkoutDate 확인할 체크아웃 날짜
             */

            if (reservation.getCheckoutDate().isBefore(now.minusDays(1))
                    ||
                    reservation.getCheckoutDate().isEqual(now.minusDays(1))) {
                // RES_STATUS를 사용완료로 변경
                reservation.setResStatus("사용완료");
                reservationRepository.save(reservation);
                log.info("예약 ID {} 상태가 '사용완료'로 변경되었습니다", reservation.getResId());
            }
        });
    }
}
