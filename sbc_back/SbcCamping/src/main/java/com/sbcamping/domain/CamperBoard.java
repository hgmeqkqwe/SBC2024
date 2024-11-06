package com.sbcamping.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;


@Entity
@Table(name = "Camper_Board")
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CamperBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//프라이머리키 자동설정 od
    @Column(name = "Cboard_ID", columnDefinition = "NUMBER(10,0)")
    private Long cBoardID; //캠퍼 게시판 글 번호

    // 깃 최종
    // Member테이블을 참조하는 회원 번호(FK) ,이 경우엔 필드명이 같지만 테이블마다 컬럼명이 다를 수도 있으니 지정해줘야함
    @ManyToOne
    @JoinColumn(name = "MEMBER_ID", referencedColumnName = "MEMBER_ID")
    private Member member; // 외래키가 참조하는 테이블

    @Column(name = "Cboard_Category", nullable = false, length = 10)
    private String cBoardCategory; //캠퍼 게시글 말머리

    @Column(name = "Cboard_Title", nullable = false, length = 50)
    private String cBoardTitle;    //캠퍼 게시판 글 제목

    @Column(name = "Cboard_Content", nullable = false, length = 1000)
    private String cBoardContent;  //캠퍼 게시판 글 내용
    //수정
    @Column(name = "Cboard_Views", nullable = false, columnDefinition = "NUMBER(30,0)")
    private Long cBoardViews = 0L; // 기본값을 0으로 설정      //게시글 조회수

    @Column(name = "Cboard_Date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date cBoardDate;       //캠퍼게시판 게시글 작성일자

    @Column(name = "Cboard_Attachment", nullable = true, length = 200)
    private String cBoardAttachment;    //파일 첨부여부(url,링크)

    //현재날짜 입력하려 수정
    @PrePersist
    protected void onCreate() {
        this.cBoardDate = new Date(); // 현재 날짜로 설정
        if (this.cBoardViews == null) {
            this.cBoardViews = 0L; // cBoardViews가 null이면 기본값으로 설정
        }
    }

    // 말머리 수정
    public void changeCategory(String Category) {
        this.cBoardCategory = Category;
    }

    //글 제목 수정
    public void changeTitle(String title) {
        this.cBoardTitle = title;
    }

    // 글 내용 수정
    public void changeContent(String content) {
        this.cBoardContent = content;
    }

    // 첨부파일 수정
    public void changeAttachment(String attachment) {
        this.cBoardAttachment = attachment;
    }

    public void changeViews(long views) {
        this.cBoardViews = views;
    }
}
