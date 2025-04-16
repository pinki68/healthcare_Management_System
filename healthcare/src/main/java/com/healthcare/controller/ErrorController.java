package com.healthcare.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		String errorMessage = "An error occurred";

		if (status != null) {
			Integer statusCode = Integer.valueOf(status.toString());

			if (statusCode == HttpStatus.NOT_FOUND.value()) {
				errorMessage = "Page not found";
			} else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
				errorMessage = "Internal server error";
			} else if (statusCode == HttpStatus.FORBIDDEN.value()) {
				errorMessage = "Access denied";
			}
		}

		model.addAttribute("errorMessage", errorMessage);
		return "error";
	}
}