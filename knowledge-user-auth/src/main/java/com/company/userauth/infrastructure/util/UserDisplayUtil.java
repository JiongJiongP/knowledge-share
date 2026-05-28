package com.company.userauth.infrastructure.util;

import com.company.common.config.Sm4Config;
import com.company.common.util.SM4Util;
import com.company.userauth.domain.model.User;

/**
 * 用户展示名解析工具。兼容 SM4 加密字段可能未被 MyBatis TypeHandler 自动解密的情况。
 */
public final class UserDisplayUtil {

    private UserDisplayUtil() {}

    /** 获取用户展示名。优先 displayName（随机 IV），回退 username（确定性加密）。 */
    public static String resolve(User user) {
        if (user == null) return "未知";
        String key = Sm4Config.getDataKey();

        String displayName = user.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            try { return SM4Util.decrypt(displayName, key); } catch (Exception ignored) {}
            return displayName;
        }
        String username = user.getUsername();
        if (username != null && !username.isEmpty()) {
            try { return SM4Util.decryptDeterministic(username, key); } catch (Exception ignored) {}
            return username;
        }
        return String.valueOf(user.getId());
    }
}
