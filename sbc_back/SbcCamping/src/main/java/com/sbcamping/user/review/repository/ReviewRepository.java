package com.sbcamping.user.review.repository;

import com.sbcamping.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;



public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 정렬 기준 처음에 등록한 글이 마지막으로
    @Query("select rv from Review rv order by rv.reviewID desc")
    Page<Review> orderdList(Pageable pageable);

    // 게시글 검색 : 제목
    Page<Review> findByReviewTitleContaining(String title, Pageable pageable);

    // 게시글 검색 : 내용
    Page<Review> findByReviewContentContaining(String content, Pageable pageable);

    // 예약번호로 리뷰 글번호 가져오기
    @Query("select r from Review r where r.reservation.resId = :resId")
    Review findByResId(String resId);
}
