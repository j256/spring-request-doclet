package com.j256.auth;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
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
	 * @return HTML response.
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
	public @ResponseBody String sayHello(//
			@RequestParam(value = "sid", required = false) String sid, //
			@RequestParam(value = "resp", required = false) Integer responseCode) {

		return null;
	}

	/**
	 * Method that has an overlapping name but with an additional argument.
	 * 
	 * @param sid
	 *            Session-id returned by the oauth processing.
	 * @param arg
	 *            Random argument.
	 * @return HTML response.
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "text/html", params = "arg")
	public @ResponseBody String sayHello(//
			@RequestParam(value = "sid", required = false) String sid, //
			@RequestParam(value = "arg", required = false) String arg) {

		return null;
	}

	/**
	 * Demonstration of a method which has a body parameter.
	 * 
	 * @param request
	 *            Request body that encapsulates a number of fields.
	 * @return HTML results from the operation.
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "text/html")
	public @ResponseBody String objectPost(@RequestBody UpdateUser updateUser) {
		return null;
	}

	/**
	 * Demonstration of a method which has an array parameter and return.
	 * 
	 * @param tokens
	 *            Multiple request tokens.
	 * @return Array of values for the tokens.
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody int[] tokenValues(@RequestParam("token") String[] tokens) {
		return null;
	}

	/**
	 * Demonstration of a method which has a body parameter which is just a string.
	 * 
	 * @param body
	 *            Request body that encapsulates the whole post as a string.
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json")
	public void stringPost(@RequestBody String body) {
	}
}
