package com.example.wms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, HttpServletRequest req, Model model) {
        logger.error("Unhandled exception for request {}", req.getRequestURI(), ex);
        model.addAttribute("status", 500);
        model.addAttribute("error", ex.getClass().getSimpleName());
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", req.getRequestURI());
        return "error";
    }
}
