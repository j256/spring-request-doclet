package com.j256.auth;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles oauth authentication.
 * 
 * <ol>
 * <li>API calls support checking out auth-token. Is it still valid?</li>
 * <li>If necessary, redirects to logging in user to the oauth handler. Might just redirect back if valid auth-token.
 * </li>
 * <li>If not valid, sends a redirect to OAuth Provider (Google) for login consent page.</li>
 * <li>after confirming oauth consent page, redirect back to here with an attached success authorization code or an
 * error message in url.</li>
 * <li>after receiving the auth code, post the auth code to the oauth provider for an access token and email (if
 * necessary).</li>
 * <li>redirects the user back to caller URL specified in the params.</li>
 * <li>caller is encouraged to make a backend call to validate the auth-token.</li>
 * </ol>
 * 
 * @author graywatson
 */
@RequestMapping(value = "/auth/oauth")
public class OAuthLoginController {

	private static final String ERROR_PARAM = "error";
	private static final String CODE_PARAM = "code";
	private static final String STATE_PARAM = "state";
	private static final String SSO_PROVIDER_NAME_PARAM = "prov";
	private static final String REDIRECT_BACK_PARAM = "redir";
	private static final String SESSION_ID_PARAM_NAME = "sid";
	static final String DEFAULT_SESSION_ID_PARAM = "sid";
	private static final String RESPONSE_CODE_PARAM_NAME = "resp";
	private static final String DEFAULT_RESPONSE_CODE_PARAM = "resp";
	private static final String RESPONSE_EXPIRE_DURATION_MILLIS = "exp";

	/**
	 * This handles the first step in the oauth process.
	 * 
	 * @param ssoVar
	 *            Name of the oauth provider we are using.
	 * @param redir
	 *            URL that we will be redirecting back to or null to use the request referer.
	 * @param sessionTokenParamName
	 *            The name of the param that will be used to send back the login token.
	 * @param responseCodeParamName
	 *            The name of the param that will be used to send back the response code.
	 * @param expireDurationMillis
	 *            Number of milliseconds that the session should live before being expired.
	 */
	@RequestMapping(method = RequestMethod.GET, params = REDIRECT_BACK_PARAM)
	public void handleStep1(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = SSO_PROVIDER_NAME_PARAM) String ssoVar,
			@RequestParam(value = REDIRECT_BACK_PARAM, required = false) String redir,
			@RequestParam(value = SESSION_ID_PARAM_NAME,
					defaultValue = DEFAULT_SESSION_ID_PARAM) String sessionTokenParamName,
			@RequestParam(value = RESPONSE_CODE_PARAM_NAME,
					defaultValue = DEFAULT_RESPONSE_CODE_PARAM) String responseCodeParamName,
			@RequestParam(value = RESPONSE_EXPIRE_DURATION_MILLIS, required = false) Long expireDurationMillis,
			HttpSession session) throws IOException {
	}

	/**
	 * The handles steps 2, 3, and 4 of the oauth process after the redirect to oauth provider has occurred back from
	 * clicking on the "Login With OAuth" sort of button.
	 * 
	 * @param authCode
	 *            Authentication code from the oauth provider.
	 * @param stateSecret
	 *            Secret provided in the redirect.
	 */
	@RequestMapping(method = RequestMethod.GET, params = { CODE_PARAM, STATE_PARAM })
	public void handleStep234(@RequestParam(CODE_PARAM) String authCode, @RequestParam(STATE_PARAM) String stateSecret)
			throws Exception {
	}

	/**
	 * Handle any errors that have occurred in the oauth process.
	 * 
	 * @param errorMsg
	 *            Error message from the oauth provider.
	 */
	@RequestMapping(method = RequestMethod.GET, params = ERROR_PARAM)
	public void handleError(@RequestParam(ERROR_PARAM) String errorMsg) throws IOException {
	}
}
