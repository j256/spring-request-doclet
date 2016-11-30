package com.j256.auth;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Little test constructor so we can test out oauth controller.
 * 
 * @author graywatson
 */
@RequestMapping(value = { "/auth/test" })
public class TestController {

	/**
	 * Print our hello world message and display the results from the auth processing.
	 * 
	 * @param sid
	 *            Session-id returned by the oauth processing.
	 * @param responseCode
	 *            The enumerated number code which corresponds to the results of the auth processing.
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public @ResponseBody String sayHello(//
			@RequestParam(value = "sid", required = false) String sid, //
			@RequestParam(value = "resp", required = false) Integer responseCode) {

		return null;
	}
}
