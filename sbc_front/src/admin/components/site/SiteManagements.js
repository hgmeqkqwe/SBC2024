// src/components/SiteManagements.js

import React, {useState, useRef, useEffect} from 'react';
import Table from 'react-bootstrap/Table';
import {Button, Modal, Form} from 'react-bootstrap';
import {getAllSites, updateSiteData} from "../../api/SiteApi"; // API 모듈에서 함수 가져오기
import { OverlayTrigger, Tooltip } from 'react-bootstrap';
import { FaQuestionCircle } from 'react-icons/fa';
/*import Spinner from 'react-bootstrap/Spinner';*/ //생각보다 사용감이 구림


// 최대,최소 허용 인원 수를 상수로 선언
const maxLimitPeople = 20;
const minLimitPeople = 1;

// SiteManagements 컴포넌트
const SiteManagements = () => {

    // 사이트 전체 정보 목록을 저장하는 변수
    const [sites, setSites] = useState([]);

    // 수정 모달 창(1)의 표시 여부를 관리하는 상태변수
    const [firstModal, setFirstModal] = useState(false);
    // 현재 선택된 구역의 정보를 저장하는 변수
    const [selectedSite, setSelectedSite] = useState(null);
    // 모달 창에서 수정된 새로운 값을 임시로 저장하는 상태 변수
    const [newSiteValue, setNewSiteValue] = useState({});
    // 수정 확인 기능을 하는 두 번째 모달 창의 표시 여부를 관리하는 변수
    const [secondModal, setSecondModal] = useState(false);

    // 입력 검증 시 발생한 에러 메시지를 저장하는 변수
    const [error, setError] = useState('');

    // 구역 이름 입력 필드에 대한 참조를 생성하여 자동 포커스를 설정할 때 사용
    const siteNameRef = useRef(null);

    // 데이터 불러오는 비동기 함수
    const settingSites = async () => {
        try {
            //변수 data에 geSiteDataALL의 Responce.data를 할당
            const data = await getAllSites();
            //set 생성자(변수)
            setSites(data);
        } catch (err) {
            console.error('사이트 데이터를 불러오는데 실패했습니다 SiteManagements파일 settingSites함수:', err);
            setError('사이트 데이터를 불러오는데 실패했습니다.');
        }
    };

    // useEffect를 사용하여 컴포넌트 마운트 시 데이터 불러오기
    useEffect(() => {
        settingSites();
    }, []);

    // **모달 창 열기 함수**
    // 선택한 구역의 정보를 설정하고 수정 모달 창을 엽니다.
    const handleShowModal = (site) => {
        setSelectedSite(site); // 선택된 구역 정보를 상태에 저장
        setNewSiteValue(site); // 선택된 구역 정보를 수정할 수 있도록 새로운 값으로 설정
        setFirstModal(true); // 수정 모달 창을 표시
        console.log(site);
    };

    // **모달 창 닫기 함수**
    // 수정 모달 창을 닫고 선택된 구역 정보를 초기화합니다.
    const handleCloseModal = () => {
        setFirstModal(false); // 수정 모달 창을 숨김
        setSelectedSite(null); // 선택된 구역 정보 초기화
        setNewSiteValue({}); // 수정된 값 초기화
        setError(''); // 에러 메시지 초기화
    };

    // **수정 사항 저장 함수**
    // 입력된 값을 검증한 후 수정 확인 모달 창을 엽니다.
    const handleSaveChanges = () => {
        // 기준 인원이 최대 인원보다 큰지 검증
        if (newSiteValue.minPeople > newSiteValue.maxPeople) {
            setError('기준 인원은 최대 인원보다 클 수 없습니다.');
            return; // 검증 실패 시 함수 종료
        }
        // 추가적인 검증 로직을 여기에 추가하면 됨
        setFirstModal(false); // 수정 모달 창을 숨김
        setSecondModal(true); // 수정 확인 모달 창을 표시
    };

    // **입력 필드 변경 처리 함수 - 텍스트 필드**
    // 텍스트 기반 입력 필드의 값 변경을 처리합니다.
    const handleTextChange = (e) => {
        const {name, value} = e.target;
        setNewSiteValue((prev) => ({...prev, [name]: value}));
        setError(''); // 입력 변경 시 에러 메시지를 초기화
    };

    // **입력 필드 변경 처리 함수 - 숫자 필드**
    // 숫자 기반 입력 필드의 값 변경을 처리하고 검증합니다.
    const handleNumberChange = (e) => {
        const {name, value} = e.target;
        const numberValue = Number(value);

        // 'maxPeople' 필드의 경우 최대 인원 제한을 검증합니다.
        if (name === 'maxPeople' && numberValue > maxLimitPeople) {
            setError(`최대 인원은 ${maxLimitPeople}명을 초과할 수 없습니다.`);
            return; // 검증 실패 시 함수 종료
        }

        setNewSiteValue((prev) => ({...prev, [name]: numberValue}));
        setError(''); // 입력 변경 시 에러 메시지를 초기화
    };

    // **입력 필드 변경 처리 함수 - 요금 필드**
    // 요금 입력 필드의 값 변경을 처리하고 숫자로 변환합니다.
    const handleRateChange = (e) => {
        const {name, value} = e.target;

        // 입력된 값에서 쉼표를 제거하여 숫자만 추출
        const numericValue = value.replace(/,/g, '');

        // 숫자인지 확인하고, 숫자일 경우 새로운 값으로 설정
        if (!isNaN(numericValue) && numericValue.trim() !== '') {
            setNewSiteValue((prev) => ({
                ...prev,
                [name === 'weekdayRate' ? 'weekdayPay' : 'weekendPay']: Number(numericValue),
            }));
        }
    };

    // **최종 수정 저장 함수**
    // 서버에 PUT 요청을 보내고, 로컬 상태를 업데이트합니다.
    const handleFinalSave = async () => {
        if (!selectedSite) {
            setError('수정할 사이트를 선택해주세요.');
            return;
        }
        try {
            // updateSiteData API 호출
            const updatedSite = await updateSiteData(selectedSite.siteId, newSiteValue);

            // 로컬 상태 업데이트
            setSites((prevSites) =>
                prevSites.map((site) =>
                    site.siteId === selectedSite.siteId ? {...site, ...updatedSite} : site));
            setSecondModal(false); // 수정 확인 모달 창을 숨김
            setSelectedSite(null); // 선택된 구역 정보 초기화
            setNewSiteValue({}); // 수정된 값 초기화
            setError(''); // 에러 메시지 초기화
            alert("수정 성공");
            //비동기식으로 정보를 다시 불러옴=새로고침과 유사한 경험을 위해
            await settingSites();
        } catch (error) {
            console.error('수정에 실패했습니다:', error);
            setError('수정에 실패했습니다. 다시 시도해주세요.');
            alert("수정 실패");
        }
    };

    return (
        <div className="max-w-full mx-auto p-6 bg-white rounded-lg shadow-md">
            {/* **페이지 제목** */}
            <h2 className="text-2xl font-bold mb-4">구역 정보 </h2>

            {/* **구역 정보 테이블** */}
            <Table bordered hover responsive className="text-sm">
                <thead>
                <tr className="bg-green-200">
                    <th style={{minWidth: '20px', maxWidth: '100px'}}>구역 번호</th>
                    <th style={{minWidth: '100px', maxWidth: '150px'}}>구역 이름</th>
                    <th style={{minWidth: '80px', maxWidth: '120px'}}>예약 제한</th>
                    <th style={{minWidth: '80px', maxWidth: '120px'}}>기준 인원</th>
                    <th style={{minWidth: '80px', maxWidth: '120px'}}>최대 인원</th>
                    <th style={{minWidth: '120px', maxWidth: '160px'}}>평일 요금</th>
                    <th style={{minWidth: '120px', maxWidth: '160px'}}>주말 요금</th>
                    <th style={{minWidth: '120px', maxWidth: '160px'}}>요금표 설정</th>
                </tr>
                </thead>
                {/* sites 배열을 순회하여 각 구역의 정보를 테이블에 표시 */}
                <tbody>
                {/*삼항연산자로 sites의 랭스가 0보다 크다면 출력 작다면 로딩이라는 문구를 띄움 랜더링 시간 고려*/}
                {sites.length > 0 ? (
                    sites.map(site => (
                        <tr key={site.siteId}>
                            {/*크기 넘버10*/}
                            <td>{site.siteId}</td>
                            {/*크기 바챠2 10*/}
                            <td>{site.siteName}</td>
                            {/*크기 챠 1*/}
                            <td>{site.siteResLimit === 'Y' ? '예약 불가능' : '예약 가능'}</td>
                            {/*크기 넘버 1*/}
                            <td>{site.minPeople}</td>
                            {/*크기 넘버 1*/}
                            <td>{site.maxPeople}</td>
                            {/*크기 넘버10*/}
                            <td>{site.weekdayPay ? site.weekdayPay.toLocaleString() : 0}</td>
                            {/*크기 넘버10*/}
                            <td>{site.weekendPay ? site.weekendPay.toLocaleString() : 0}</td>
                            <td>
                                <Button
                                    variant="outline-warning" //호버시 노랑색
                                    onClick={() => handleShowModal(site)}
                                    style={{width: '100%', color: 'black'}}//td에 꽉채우기,텍스트 컬러설정
                                >
                                    수정하기
                                </Button>
                            </td>
                        </tr>
                    ))
                ) : (
                    /*  <Spinner animation="border" role="status">
                          <td className="visually-hidden">Loading...</td>
                      </Spinner>*/ //생각보다 사용감이 많이 구림
                    <td>
                        Loding...
                    </td>
                )}
                </tbody>
            </Table>

            {/* **수정 모달 창** */}
            <Modal show={firstModal} onHide={handleCloseModal} centered>
                <Modal.Header closeButton>
                    <Modal.Title>구역 수정 - {selectedSite?.siteId ? selectedSite.siteId : 'N/A'}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedSite && (
                        <div>
                            {/* **구역 이름 입력 필드** */}
                            <Form.Group className="mb-3 d-inline">
                                <Form.Label>구역 이름
                                    <OverlayTrigger
                                        placement="right"
                                        overlay={<Tooltip id="tooltip-siteName">구역 이름을 입력하세요. <div>최대 10자</div></Tooltip>}
                                    >
                                        <p className="ms-2 d-inline">
                                            <FaQuestionCircle style={{cursor: 'pointer', color: '#006000'}}/>
                                        </p>
                                    </OverlayTrigger>
                                </Form.Label> {/*바챠2 10*/}{/*마우스 호버시 툴팁 or 라벨 옆 문구 고민중 */}

                                <Form.Control
                                    type="text"
                                    name="siteName"
                                    value={newSiteValue.siteName || ''}
                                    onChange={handleTextChange} // 값 변경 시 handleTextChange 함수 호출
                                    ref={siteNameRef} // 자동 포커스를 위한 참조 설정
                                    maxLength={10} // 최대 10글자 제한
                                />
                            </Form.Group>
                            <br/>
                            {/* **예약 제한 라디오 버튼** */}
                            <Form.Group className="mb-3">
                                <Form.Label>예약 제한</Form.Label> {/*챠 1 - Y=예약 불가 ,N= 예약 가능*/}
                                <Form.Check
                                    type="radio"
                                    label="예약 가능"
                                    name="siteResLimit"
                                    value="N"
                                    checked={newSiteValue.siteResLimit === 'N'} //N=예약가능=예약제한상태가 아니라는 뜻
                                    onChange={handleTextChange} // 값 변경 시 handleTextChange 함수 호출
                                />
                                <Form.Check
                                    type="radio"
                                    label="예약 불가능"
                                    name="siteResLimit"
                                    value="Y"
                                    checked={newSiteValue.siteResLimit === 'Y'} ///Y=예약불가=예약제한상태라는 뜻
                                    onChange={handleTextChange} // 값 변경 시 handleTextChange 함수 호출
                                />
                            </Form.Group>

                            {/* **기준 인원 입력 필드** */}
                            <Form.Group className="mb-3">
                                <Form.Label>기준 인원
                                    <OverlayTrigger
                                        placement="right"
                                        overlay={<Tooltip id="tooltip-siteName">최대 인원을 초과 할 수 없음</Tooltip>}
                                    >
                                        <p className="ms-2 d-inline">
                                            <FaQuestionCircle style={{cursor: 'pointer', color: '#006000'}}/>
                                        </p>
                                    </OverlayTrigger>
                                </Form.Label>
                                <Form.Control
                                    type="number"
                                    name="minPeople"
                                    value={newSiteValue.minPeople || ''}
                                    onChange={handleNumberChange} // 값 변경 시 handleNumberChange 함수 호출
                                    min={minLimitPeople} // 최소값 설정
                                    max={maxLimitPeople} // 최대값 설정 위에서 선언해놓은 상수 값은 6
                                />
                            </Form.Group>

                            {/* **최대 인원 입력 필드** */}
                            <Form.Group className="mb-3">
                                <Form.Label>최대 인원
                                    <OverlayTrigger
                                        placement="right"
                                        overlay={<Tooltip id="tooltip-siteName">{maxLimitPeople}을 초과 할 수 없음</Tooltip>}
                                    >
                                        <p className="ms-2 d-inline">
                                            <FaQuestionCircle style={{cursor: 'pointer', color: '#006000'}}/>
                                        </p>
                                    </OverlayTrigger>
                                </Form.Label>
                                <Form.Control
                                    type="number"
                                    name="maxPeople"
                                    value={newSiteValue.maxPeople || ''}
                                    onChange={handleNumberChange} // 값 변경 시 handleNumberChange 함수 호출
                                    min={minLimitPeople} // 최소값 설정
                                    max={maxLimitPeople} // 최대값 설정
                                />
                            </Form.Group>
                            {/* **평일 요금 입력 필드** */}
                            <Form.Group className="mb-3">
                                <Form.Label>평일 요금</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="weekdayRate"
                                    value={newSiteValue.weekdayPay?.toLocaleString() || ''}
                                    onChange={handleRateChange} // 값 변경 시 handleRateChange 함수 호출
                                    maxLength={13}
                                />
                            </Form.Group>

                            {/* **주말 요금 입력 필드** */}
                            <Form.Group className="mb-3">
                                <Form.Label>주말 요금</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="weekendRate"
                                    value={newSiteValue.weekendPay?.toLocaleString() || ''}
                                    onChange={handleRateChange} // 값 변경 시 handleRateChange 함수 호출
                                    maxLength={13}
                                />
                            </Form.Group>

                            {/* **조건부 렌더링으로 에러 발생시 메시지 표시** */}
                            {error && <div className="text-danger">{error}</div>}
                        </div>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    {/* **취소 버튼** */}
                    <Button variant="outline-primary" onClick={handleCloseModal}>
                        취소
                    </Button>
                    {/* **저장 버튼** */}
                    <Button variant="outline-primary" onClick={handleSaveChanges}>
                        저장
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* **수정 확인 모달 창** */}
            <Modal show={secondModal} onHide={() => setSecondModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>구역 수정 확인 - {selectedSite?.siteId ? selectedSite.siteId : 'N/A'}번</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    {selectedSite && (
                        <div>
                            {/* 수정 전후의 값들을 비교하여 표시 */}
                            <p><strong>구역 번호:</strong> {selectedSite.siteId}</p>
                            <p><strong>구역 이름:</strong> {selectedSite.siteName} => {newSiteValue.siteName}</p>
                            <p><strong>예약
                                제한:</strong> {selectedSite.siteResLimit === 'Y' ? '예약 불가능' : '예약 가능'} => {newSiteValue.siteResLimit === 'Y' ? '예약 불가능' : '예약 가능'}
                            </p>
                            <p><strong>기준 인원:</strong> {selectedSite.minPeople} => {newSiteValue.minPeople}</p>
                            <p><strong>최대 인원:</strong> {selectedSite.maxPeople} => {newSiteValue.maxPeople}</p>
                            <p><strong>평일
                                요금:</strong> {selectedSite.weekdayPay ? selectedSite.weekdayPay.toLocaleString() : 0} => {newSiteValue.weekdayPay ? newSiteValue.weekdayPay.toLocaleString() : 0}
                            </p>
                            <p><strong>주말
                                요금:</strong> {selectedSite.weekendPay ? selectedSite.weekendPay.toLocaleString() : 0} => {newSiteValue.weekendPay ? newSiteValue.weekendPay.toLocaleString() : 0}
                            </p>
                        </div>
                    )}
                </Modal.Body>

                <Modal.Footer>

                    {/* **취소 버튼** */}
                    <Button variant="outline-secondary" onClick={() => setSecondModal(false)}>
                        취소
                    </Button>

                    {/* **최종 수정 버튼** */}
                    <Button variant="outline-primary" onClick={handleFinalSave}>
                        최종 수정
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );

};

// **컴포넌트 내보내기**
export default SiteManagements;