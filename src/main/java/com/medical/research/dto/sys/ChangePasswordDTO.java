package com.medical.research.dto.sys;

import com.medical.research.util.AESUtil;

/**
 * @Auther: yilefeng
 * @Date: 2026/1/15 16:04
 * @Description:
 */
public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;

    public String getOldPassword() {
        try {
            return AESUtil.decrypt(oldPassword) ;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        try {
            return AESUtil.decrypt(newPassword) ;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
