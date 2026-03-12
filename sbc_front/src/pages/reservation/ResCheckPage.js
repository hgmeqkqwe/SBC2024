import React, {useState} from 'react';
import Form from 'react-bootstrap/Form';
import "./css/Respage.css";
import "./css/ResCheckPage.css"
import {useLocation, useNavigate} from "react-router-dom";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";

const ResCheckPage = () => {
    const location = useLocation();

    const {resData, resNumber, siteName} = location.state || {}

    const navigate = useNavigate()

    function formatPhoneNumber(phoneNumber) {
        return phoneNumber.replace(/(\d{3})(\d{4})(\d{4})/, "$1-$2-$3");
    }

    const formattedNumber = formatPhoneNumber(resData.resUserPhone)

    /*
    *
    * 결제 테스트 시작
    *
    * */
    function onClickPayment() {

        /* 1. 가맹점 식별하기 */
        const { IMP } = window;
        IMP.init('**********');

        /* 2. 결제 데이터 정의하기 */
        const data = {
            pg: 'kakaopay.TC0ONETIME',                           // PG사
            pay_method: 'EASY_PAY',                           // 결제수단
            merchant_uid: `mid_${new Date().getTime()}`,   // 주문번호
            amount: 1,                                 // 결제금액
            name: 'SBCAMPING 결제 테스트',                  // 주문명
            buyer_name: '홍길동',                           // 구매자 이름
            buyer_tel: '01012341234',                     // 구매자 전화번호
            buyer_email: 'example@example',               // 구매자 이메일
        };

        /* 4. 결제 창 호출하기 */
        IMP.request_pay(data, callback);
    }

    /* 3. 콜백 함수 정의하기 */
    function callback(response) {
        const {
            success,
            merchant_uid,
            error_msg,
        } = response;

        if (success) {
            alert('결제가 완료되었습니다');
            navigate('/mypage/res')
        } else {
            alert(`결제 실패: ${error_msg}`);
        }
    }
    /*
    *
    * 결제 테스트 끝
    *
    * */

    return (
        <div className="mainDiv">
            <div>
                <h3>예약완료</h3>
                <Form className="mainForm">
                    <Form.Group as={Row} className="mb-0" controlId="resNumber">
                        <Form.Label className="label" column sm="3">
                            예약번호
                        </Form.Label>
                        <Col sm="9">
                            <Form.Control className="formControl" type="text" value={resNumber} readOnly={true}/>
                        </Col>
                    </Form.Group>
                    <Form.Group as={Row} className="mb-0" controlId="siteName">
                        <Form.Label className="label" column sm="3">
                            구역이름
                        </Form.Label>
                        <Col sm="9">
                            <Form.Control className="formControl" type="text" value={siteName} readOnly={true}/>
                        </Col>
                    </Form.Group>
                    <Form.Group as={Row} className="mb-0" controlId="resUserName">
                        <Form.Label className="label" column sm="3">
                            예약자명
                        </Form.Label>
                        <Col sm="9">
                            <Form.Control className="formControl" type="text" value={resData.resUserName}
                                          readOnly={true}/>
                        </Col>
                    </Form.Group>
                    <Form.Group as={Row} className="mb-0" controlId="resUserPhone">
                        <Form.Label className="label" column sm="3">
                            예약자 핸드폰 번호
                        </Form.Label>
                        <Col sm="9">
                            <Form.Control className="formControl" type="text" value={formattedNumber}
                                          readOnly={true}/>
                        </Col>
                    </Form.Group>
                    <Form.Group as={Row} className="mb-0" controlId="checkinDate">
                        <Form.Label className="label" column sm="3">
                            입실날짜
                        </Form.Label>
                        <Col sm="9">
                            <Form.Control className="formControl" type="text" value={resData.checkinDate}
                                          readOnly={true}/>
                        </Col>
                    </Form.Group>
                    <Form.Group as={Row} className="mb-0" controlId="checkoutDate">
                        <Form.Label className="label" column sm="3">
                            퇴실날짜
                        </Form.Label>
                        <Col sm="9">
                            <Form.Control className="formControl" type="text" value={resData.checkoutDate}
                                          readOnly={true}/>
                        </Col>
                    </Form.Group>
                    <Form.Group as={Row} className="mb-0" controlId="resPeople">
                        <Form.Label className="label" column sm="3">
                            입실인원
                        </Form.Label>
                        <Col sm="9">
                            <Form.Control className="formControl" type="text" value={`${resData.resPeople}명`}
                                          readOnly={true}/>
                        </Col>
                    </Form.Group>
                    <Form.Group as={Row} className="mb-0" controlId="resTotalPay">
                        <Form.Label className="label" column sm="3">
                            결제금액
                        </Form.Label>
                        <Col sm="9">
                            <Form.Control className="formControl" type="text" value={resData.resTotalPay}
                                          readOnly={true}/>
                        </Col>
                    </Form.Group>
                </Form>

                <div style={{
                    display: "flex", alignItems: "center", justifyContent: "center"
                }}>
                    <Button variant="warning" className="checkSuccess" onClick={onClickPayment}>카카오 결제하기</Button>
                </div>
            </div>
        </div>
    );
};

export default ResCheckPage;
