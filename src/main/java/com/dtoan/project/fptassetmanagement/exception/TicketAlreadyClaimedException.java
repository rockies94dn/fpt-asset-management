package com.dtoan.project.fptassetmanagement.exception;

public class TicketAlreadyClaimedException extends RuntimeException {

    public TicketAlreadyClaimedException(String message) {
        super(message);
    }
}
