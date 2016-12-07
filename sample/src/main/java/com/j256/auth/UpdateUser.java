package com.j256.auth;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Example of a request body object.
 * 
 * @author graywatson
 */
public class UpdateUser {

	private String firstName;
	private String lastName;

	public UpdateUser() {
		// for json
	}

	/**
	 * First name of the user.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Last name of the user.
	 */
	public String getLastName() {
		return lastName;
	}
}
