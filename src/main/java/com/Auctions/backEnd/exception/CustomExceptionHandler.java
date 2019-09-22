package com.Auctions.backEnd.exception;

import com.Auctions.backEnd.responses.Message;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@EnableWebMvc
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {
            FileUploadBase.FileSizeLimitExceededException.class,
            FileUploadBase.SizeLimitExceededException.class,
            GenericJDBCException.class
    })
    protected ResponseEntity handleFileUpload() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                "Error",
                "File too big"
        ));
    }
}