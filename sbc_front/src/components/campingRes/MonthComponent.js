import React, {useEffect, useState} from 'react';
import {Button, Table} from "react-bootstrap";
import {Link, useNavigate} from "react-router-dom";
import "../../pages/reservation/css/RealTimeResPage.css"
import {getSiteList, resCheck} from "../../api/ResApi";
import {useSelector} from "react-redux";

// td 태그를 재사용 하고싶은데 어케할까? 흠....

const MonthComponent = () => {

    const [site, setSite] = useState([]);

    const today = new Date();

    // 날짜 비교 변수
    const currentYear = today.getFullYear();
    const currentMonth = today.getMonth() + 1;
    const currentDay = today.getDate();

    const [resCheckData, setResCheckData] = useState([]);
    const [calendar, setCalendar] = useState([])
    const [year, setYear] = useState(currentYear);
    const [month, setMonth] = useState(currentMonth);
    const [direction, setDirection] = useState(null); // 방향 상태 추가

    const loginState = useSelector((state) => state.loginSlice)

    // 달력 만드는 함수
    // 요일
    const daysOfWeek = ['일', '월', '화', '수', '목', '금', '토'];

    const monthsToAdd = 2;
    const monthSum = (currentMonth + monthsToAdd - 1) % 12 + 1;

    // 날짜 비교 : 현재 날짜보다 과거인지 확인
    const isPastDate = (setYear, setMonth, setDay) => {
        if (setYear < currentYear) return true;
        if (setYear === currentYear && setMonth < currentMonth) return true;
        if (setYear === currentYear && setMonth === currentMonth && setDay < currentDay) return true;

        return false;
    }

    // 주어진 년도와 월에 따라 달력 데이터를 생성하는 함수
    const generateCalendar = (year, month) => {
        const firstDay = new Date(year, month - 1, 1).getDay(); // 첫 날의 요일
        const firstMonth = new Date(year, month, 0).getDate(); // 전 달의 마지막 날

        const weeks = [];
        let date = 1;

        for (let i = 0; i < 5; i++) { // 최대 5주
            const week = [];

            for (let j = 0; j < 7; j++) {
                if (i === 0 && j < firstDay) {
                    week.push({day: '', month: month, year: year}); // 첫 주의 공백 부분
                } else if (date > firstMonth) {
                    week.push({day: '', month: month, year: year}) // 마지막 주 빈 공간
                } else {
                    week.push({day: date, month: month, year: year});
                    date++;
                }
            }
            weeks.push(week);
        }

        return weeks;
    };

    const handleClick = (dir) => {
        setDirection(dir); // 클릭한 방향을 상태에 저장
    };

    // 다음달 , 이전달 버튼 > year , month 상태 바로 반영
    useEffect(() => {
        if (direction) { // direction 이 null 이 아닐 때만 실행
            let newYear = year;
            let newMonth = month;

            if (direction === "prev") {
                newMonth -= 1;
                if (newMonth < 1) {
                    newMonth = 12;
                    newYear -= 1;
                }
            } else if (direction === "next") {
                newMonth += 1;
                if (newMonth > 12) {
                    newMonth = 1;
                    newYear += 1;
                }
            }

            setYear(newYear);
            setMonth(newMonth);

            setDirection(null); // direction 상태 초기화
        }
    }, [direction, year, month]);

    useEffect(() => {
        getSiteList().then(data => {
            console.log(data)
            setSite(data)
        })
        resCheck().then(data => {
            console.log(data)
            setResCheckData(data);
        })
    }, [])

    // 다음달 year , month 상태 바로 반영
    useEffect(() => {
        const weeks = generateCalendar(year, month);

        // 마지막 주에서 빈 공간을 다음 달 날짜로 채우기
        const lastWeek = weeks[weeks.length - 1];
        const emptySpacesCount = lastWeek.filter(day => day.day === '').length;

        // 빈 칸의 수가 0보다 클 경우, 다음 달 날짜를 추가
        if (emptySpacesCount > 0) {
            // 빈 공간에 날짜를 순서대로 채우기
            let nextYear = year;
            let nextDate = 1; // 다음 달 날짜 시작
            let nextMonth = month + 1; // 다음 달 초기화

            if (nextMonth > 12) {
                nextMonth = 1; // 12월을 넘기면 1월로
                nextYear = nextYear + 1;
            }

            // 빈 칸에 다음 달 날짜 채우기
            for (let i = 0; i < lastWeek.length; i++) {
                if (lastWeek[i].day === '') {
                    lastWeek[i] = {day: nextDate, month: nextMonth, year: nextYear}; // 빈 공간에 다음 달 날짜와 월 저장
                    nextDate++;
                }
            }
        }

        setCalendar(weeks);

    }, [year, month]);


    return (
        <div>
            <div className="topDate">
                {(year !== currentYear || currentMonth !== month) && (
                    <Button
                        variant="success"
                        className="move"
                        onClick={() => handleClick("prev")}
                    >
                        이전달
                    </Button>
                )}

                <h3 style={{display: "inline", margin: "0 30px"}}>
                    <b>
                        {year}.{month < 10 ? `0${month}` : month}
                    </b>
                </h3>

                {((year < currentYear && year !== currentYear) || month !== monthSum) && (
                    <Button
                        variant="success"
                        className="move"
                        onClick={() => handleClick("next")}
                    >
                        다음달
                    </Button>
                )}
            </div>
            
            <h4>구역상태</h4>
            <p className="resSuccess">예</p> : 예약가능&nbsp;&nbsp;
            <p className="resFail">완</p> : 예약불가&nbsp;&nbsp;
            <p className="resRepair">수</p> : 수리중&nbsp;&nbsp;
            <Table responsive bordered>
                <thead>
                <tr>
                    {daysOfWeek.map((day) => (
                        <th key={day}>{day}</th>
                    ))}
                </tr>
                </thead>
                <tbody>
                {calendar.map((week, i) => (
                    <tr key={i}>
                        {week.map((dayInfo, j) => {
                            const isPast = isPastDate(dayInfo.year, dayInfo.month, dayInfo.day);

                            const result = resCheckData.map(item => {
                                const date = new Date(item[3])
                                return date.toISOString().split('T')[0]
                            })

                            const filterCheck = (siteId, date) => {

                                const filterData = resCheckData.filter((item, index) =>
                                    item[0] === siteId &&
                                    result[index] === date &&
                                    item[4] === "true"
                                )

                                // 조건을 만족하는 경우 true를 반환하고, 만족하지 않으면 false 반환
                                return filterData.length > 0; // 필터링된 데이터가 있으면 true, 없으면 false
                            }

                            const date = new Date(dayInfo.year, dayInfo.month - 1, dayInfo.day)
                            const formattedDate = date.toLocaleDateString('en-CA')

                            if (dayInfo.day) {
                                return (
                                    <td key={j} style={{
                                        backgroundColor: isPast ? "lightgray" : "white"
                                    }}>
                                        <div>
                                            {dayInfo.month > month || dayInfo.year > year
                                                ? `${dayInfo.month}/${dayInfo.day}`
                                                : dayInfo.day /* 빈 공간일 경우 월/일 형식으로 표시 */
                                            }
                                        </div>
                                        {isPast
                                            ? (
                                                "예약 불가"
                                            ) : (
                                                <>
                                                    {site.map((site, index) => {
                                                        return (
                                                            <div key={index}>
                                                                {(() => {
                                                                    if (site.siteResLimit === 'Y') {
                                                                        return (
                                                                            <>
                                                                                <span className="resRepair">
                                                                                    &nbsp;&nbsp;수&nbsp;&nbsp;
                                                                                </span>
                                                                                <Link to="#" className="linkFail">
                                                                                    &nbsp;{site.siteName}
                                                                                </Link>
                                                                            </>
                                                                        )
                                                                    }
                                                                    else if (filterCheck(site.siteId, formattedDate)) {
                                                                        return (
                                                                            <>
                                                                                <span className="resFail">
                                                                                    &nbsp;&nbsp;완&nbsp;&nbsp;
                                                                                </span>
                                                                                <Link to="#" className="linkFail">
                                                                                    &nbsp;{site.siteName}
                                                                                </Link>
                                                                            </>
                                                                        );
                                                                    } else {
                                                                        return (
                                                                            <>
                                                                                <span className="resSuccess">
                                                                                    &nbsp;&nbsp;예&nbsp;&nbsp;
                                                                                </span>
                                                                                <Link
                                                                                    to="/res/respage"
                                                                                    state={{
                                                                                        year: dayInfo.year,
                                                                                        month: dayInfo.month,
                                                                                        day: dayInfo.day,
                                                                                        memberId: loginState.member.memberId,
                                                                                        memberName: loginState.member.memberName,
                                                                                        memberPhone: loginState.member.memberPhone,
                                                                                        memberEmail: loginState.member.memberEmail,
                                                                                        siteId: site.siteId,
                                                                                        siteName: site.siteName,
                                                                                        weekDayPay: site.weekdayPay,
                                                                                        weekEndPay: site.weekendPay,
                                                                                        maxPeople: site.maxPeople
                                                                                    }}
                                                                                >
                                                                                    &nbsp;{site.siteName}
                                                                                </Link>
                                                                            </>
                                                                        );
                                                                    }
                                                                })()}
                                                            </div>
                                                        );
                                                    })}
                                                </>
                                            )
                                        }
                                    </td>
                                )
                            } else {
                                // 공백일경우
                                return <td key={j} style={{
                                    backgroundColor: "lightgray"
                                }}></td>
                            }
                        })}
                    </tr>
                ))}
                </tbody>
            </Table>
        </div>
    );
};

export default MonthComponent;