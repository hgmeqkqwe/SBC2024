package com.sbcamping.user.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationDateDTO {
    private Long siteId;
    private String reservedDate;
    private String checkoutDate;
    private boolean reserved;
}
