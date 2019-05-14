package com.moesome.spike.resolver;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
	@Autowired
	private AuthService authService;

	@Override
	public boolean supportsParameter(MethodParameter parameter){
		return User.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse httpServletResponse = webRequest.getNativeRequest(HttpServletResponse.class);
		String paramSessionId = httpServletRequest.getParameter("sessionId");
		String cookieSessionId = getCookieValue(httpServletRequest,"sessionId");
		String sessionId = null;
		if (!StringUtils.isEmpty(paramSessionId)){
			sessionId = paramSessionId;
		}else if (!StringUtils.isEmpty(cookieSessionId)){
			sessionId = cookieSessionId;
		}else{
			return null;
		}
		return authService.getUserBySessionId(sessionId);
	}

	private String getCookieValue(HttpServletRequest request, String cookieName) {
		Cookie[]  cookies = request.getCookies();
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(cookieName)) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
