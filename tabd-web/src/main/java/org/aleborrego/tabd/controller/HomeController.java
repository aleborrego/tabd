package org.aleborrego.tabd.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for Views
 */
@Controller
public class HomeController {

	@RequestMapping({ "/team" })
	public String adminRouterFallback(HttpServletRequest request, HttpServletResponse response) {
		return "forward:/";
	}

}