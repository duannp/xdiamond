package io.github.xdiamond.web.shiro;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;

public class CustomRolesAuthorizationFilter extends AuthorizationFilter {

  String unauthorizedJSONString =
      "{\"success\":false,\"error\":{\"message\":\"unauthorized! need login\"}}";

  String forbiddenJSONString =
      "{\"success\":false,\"error\":{\"message\":\"forbidden! no permission.\"}}";

  @Override
  protected boolean onAccessDenied(ServletRequest request, ServletResponse response)
      throws IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    Subject subject = getSubject(request, response);

    if (subject.getPrincipal() == null) {
      if (AjaxUtils.isAjaxRequest(httpRequest) || AjaxUtils.isAcceptJSON(httpRequest)) {
        WebUtil.wrietJSONResponse(httpResponse, unauthorizedJSONString,
            HttpServletResponse.SC_UNAUTHORIZED);
      } else {
        saveRequestAndRedirectToLogin(request, response);
      }
    } else {
      if (AjaxUtils.isAjaxRequest(httpRequest) || AjaxUtils.isAcceptJSON(httpRequest)) {
        WebUtil.wrietJSONResponse(httpResponse, forbiddenJSONString, HttpServletResponse.SC_FORBIDDEN);
      } else {
        String unauthorizedUrl = getUnauthorizedUrl();
        if (StringUtils.hasText(unauthorizedUrl)) {
          WebUtils.issueRedirect(request, response, unauthorizedUrl);
        } else {
          WebUtils.toHttp(response).sendError(401);
        }
      }
    }
    return false;
  }

  @Override
  public boolean isAccessAllowed(ServletRequest request, ServletResponse response,
      Object mappedValue) throws IOException {

    Subject subject = getSubject(request, response);
    String[] rolesArray = (String[]) mappedValue;

    if (rolesArray == null || rolesArray.length == 0) {
      // no roles specified, so nothing to check - allow access.
      return true;
    }

    Set<String> roles = CollectionUtils.asSet(rolesArray);
    return subject.hasAllRoles(roles);
  }


}
