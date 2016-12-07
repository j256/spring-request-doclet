package com.j256.auth;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Returns user information associated with the session-id generated by the oauth process.
 * 
 * @author graywatson
 */
@RequestMapping(value = { "/auth/userInfo" })
public class UserInfoController {

	/**
	 * Return information about the user logged in.
	 * 
	 * @param userId
	 *            User-id that we are looking up.
	 * @param sessionId
	 *            Session-id associated with the user.
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody UserInfo getUserInfo(@RequestParam(value = "userId") String userId,
			@RequestParam(value = OAuthLoginController.DEFAULT_SESSION_ID_PARAM) String sessionId) {
		return null;
	}

	/**
	 * User information returned.
	 */
	public static class UserInfo {
		private String name;
		private String rank;

		/**
		 * Name of the user.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Rank of the usr.
		 */
		public String getRank() {
			return rank;
		}
	}
}
