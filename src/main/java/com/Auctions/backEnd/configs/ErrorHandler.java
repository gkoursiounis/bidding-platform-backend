package com.Auctions.backEnd.configs;

import com.Auctions.backEnd.responses.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ErrorHandler {

    /**
     * Exception handler
     *
     * For every exception we return an <HTTP>BAD REQUEST</HTTP>
     * unless it is specified another HttpStatus in the function
     * or route that caused the exception
     *
     * @param ex - exception
     * @param request - request
     * @param response - response
     * @return an <HTTP>BAD REQUEST</HTTP>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception ex,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Message(
                        "Error",
                        "Oops..Something went wrong - invalid data or HTTPS request"
                ));
    }
}