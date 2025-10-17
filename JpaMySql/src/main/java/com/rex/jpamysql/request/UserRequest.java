package com.rex.jpamysql.request;

import com.rex.jpamysql.entity.UserBase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRequest {

    @Size(max = 36)
    private String uuid;

    @NotBlank
    @Size(min = 6, max = 100, message = "account size is between 6 and 100")
    private String account;

    @NotBlank
    @Size(max = 100)
    private String password;

    @Size(max = 255)
    private String remark;

    @NotNull
    private Long createdUserId;

    private Long updateUserId;

    /**
     * 建立一個新的 UserBase Entity（若 uuid 為 null 則自動產生）
     */
    public UserBase toEntity() {
        UserBase entity = new UserBase();
        entity.setUuid(this.uuid != null ? this.uuid : UUID.randomUUID().toString());
        entity.setAccount(this.account);
        entity.setPassword(this.password);
        entity.setRemark(this.remark);
        entity.setCreatedUserId(this.createdUserId);
        entity.setUpdateUserId(this.updateUserId);
        return entity;
    }

    /**
     * 將 DTO 的可更新欄位套用到既有 Entity（不覆寫 createdUserId/createdAt）
     */
    public UserBase updateEntity(UserBase existing) {
        if (this.account != null) existing.setAccount(this.account);
        if (this.password != null) existing.setPassword(this.password);
        if (this.remark != null) existing.setRemark(this.remark);
        if (this.updateUserId != null) existing.setUpdateUserId(this.updateUserId);
        return existing;
    }
}

