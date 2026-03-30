package com.youda.utils;

import com.youda.common.BusinessException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类
 */
public class UserContext {

    private static final String USER_ID_ATTRIBUTE = "currentUserId";

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Long userId) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            request.setAttribute(USER_ID_ATTRIBUTE, userId);
        }
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object userId = request.getAttribute(USER_ID_ATTRIBUTE);
            if (userId != null) {
                return (Long) userId;
            }
        }
        throw new BusinessException(401, "请先登录");
    }

    /**
     * 获取当前用户ID，未登录返回null
     */
    public static Long getCurrentUserIdOrNull() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object userId = request.getAttribute(USER_ID_ATTRIBUTE);
            if (userId != null) {
                return (Long) userId;
            }
        }
        return null;
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
