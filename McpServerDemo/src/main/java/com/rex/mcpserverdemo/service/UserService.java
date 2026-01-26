package com.rex.mcpserverdemo.service;

import com.rex.mcpserverdemo.entity.User;
import com.rex.mcpserverdemo.repository.UserRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用者服務 - MCP CRUD 工具
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Tool(description = "建立新使用者")
    @Transactional
    public String createUser(
            @ToolParam(description = "用戶名稱（必填，唯一）") String username,
            @ToolParam(description = "電子郵件（必填，唯一）") String email,
            @ToolParam(description = "電話號碼（選填）") String phone) {

        // 檢查用戶名是否已存在
        if (userRepository.existsByUsername(username)) {
            return "❌ 建立失敗：用戶名 '" + username + "' 已存在";
        }

        // 檢查 Email 是否已存在
        if (userRepository.existsByEmail(email)) {
            return "❌ 建立失敗：Email '" + email + "' 已存在";
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .phone(phone)
                .build();

        User savedUser = userRepository.save(user);

        return String.format("✅ 使用者建立成功！\n" +
                "ID: %d\n" +
                "用戶名: %s\n" +
                "Email: %s\n" +
                "電話: %s\n" +
                "建立時間: %s",
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getPhone() != null ? savedUser.getPhone() : "未設定",
                savedUser.getCreatedAt());
    }

    @Tool(description = "依 ID 查詢使用者")
    public String getUserById(
            @ToolParam(description = "使用者 ID") Long id) {

        return userRepository.findById(id)
                .map(this::formatUser)
                .orElse("❌ 找不到 ID 為 " + id + " 的使用者");
    }

    @Tool(description = "查詢所有使用者")
    public String getAllUsers() {
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            return "📭 目前沒有任何使用者資料";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 使用者列表（共 ").append(users.size()).append(" 筆）\n");
        sb.append("═".repeat(40)).append("\n");

        for (User user : users) {
            sb.append(formatUserBrief(user)).append("\n");
        }

        return sb.toString();
    }

    @Tool(description = "更新使用者資料")
    @Transactional
    public String updateUser(
            @ToolParam(description = "使用者 ID") Long id,
            @ToolParam(description = "新的用戶名稱（傳入空字串則不更新）") String username,
            @ToolParam(description = "新的電子郵件（傳入空字串則不更新）") String email,
            @ToolParam(description = "新的電話號碼（傳入空字串則不更新）") String phone) {

        return userRepository.findById(id)
                .map(user -> {
                    boolean updated = false;

                    if (username != null && !username.isBlank()) {
                        // 檢查新用戶名是否已被其他人使用
                        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
                            return "❌ 更新失敗：用戶名 '" + username + "' 已被使用";
                        }
                        user.setUsername(username);
                        updated = true;
                    }

                    if (email != null && !email.isBlank()) {
                        // 檢查新 Email 是否已被其他人使用
                        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                            return "❌ 更新失敗：Email '" + email + "' 已被使用";
                        }
                        user.setEmail(email);
                        updated = true;
                    }

                    if (phone != null && !phone.isBlank()) {
                        user.setPhone(phone);
                        updated = true;
                    }

                    if (!updated) {
                        return "⚠️ 沒有提供任何要更新的資料";
                    }

                    userRepository.save(user);
                    return "✅ 使用者更新成功！\n" + formatUser(user);
                })
                .orElse("❌ 找不到 ID 為 " + id + " 的使用者");
    }

    @Tool(description = "刪除使用者")
    @Transactional
    public String deleteUser(
            @ToolParam(description = "使用者 ID") Long id) {

        return userRepository.findById(id)
                .map(user -> {
                    String username = user.getUsername();
                    userRepository.delete(user);
                    return "✅ 使用者 '" + username + "' (ID: " + id + ") 已成功刪除";
                })
                .orElse("❌ 找不到 ID 為 " + id + " 的使用者");
    }

    @Tool(description = "依用戶名搜尋使用者（支援模糊搜尋）")
    public String searchUserByUsername(
            @ToolParam(description = "搜尋關鍵字") String keyword) {

        List<User> users = userRepository.findByUsernameContaining(keyword);

        if (users.isEmpty()) {
            return "🔍 找不到包含 '" + keyword + "' 的使用者";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔍 搜尋結果（關鍵字: '").append(keyword).append("'，共 ").append(users.size()).append(" 筆）\n");
        sb.append("═".repeat(40)).append("\n");

        for (User user : users) {
            sb.append(formatUserBrief(user)).append("\n");
        }

        return sb.toString();
    }

    /**
     * 格式化使用者詳細資訊
     */
    private String formatUser(User user) {
        return String.format(
                "【使用者資訊】\n" +
                        "ID: %d\n" +
                        "用戶名: %s\n" +
                        "Email: %s\n" +
                        "電話: %s\n" +
                        "建立時間: %s\n" +
                        "更新時間: %s",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone() != null ? user.getPhone() : "未設定",
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    /**
     * 格式化使用者簡要資訊
     */
    private String formatUserBrief(User user) {
        return String.format("• [%d] %s <%s>",
                user.getId(),
                user.getUsername(),
                user.getEmail());
    }
}
