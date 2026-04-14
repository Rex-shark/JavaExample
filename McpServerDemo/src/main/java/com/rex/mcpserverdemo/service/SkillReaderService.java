package com.rex.mcpserverdemo.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Skills 讀取服務 - MCP 工具
 * 提供讀取專案 .agent/skills 目錄下的 Skill 定義功能
 */
@Service
public class SkillReaderService {

    private static final String SKILLS_DIR = ".agent/skills";
    private static final String SKILL_FILE = "SKILL.md";

    @Tool(description = "列出專案中所有可用的 Skills 名稱清單")
    public String listSkills() {
        Path skillsPath = Paths.get(SKILLS_DIR);

        if (!Files.exists(skillsPath) || !Files.isDirectory(skillsPath)) {
            return "❌ 找不到 Skills 目錄：" + skillsPath.toAbsolutePath();
        }

        try (Stream<Path> dirs = Files.list(skillsPath)) {
            List<String> skills = dirs
                    .filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());

            if (skills.isEmpty()) {
                return "📭 目前沒有任何 Skill 定義";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📋 可用的 Skills（共 ").append(skills.size()).append(" 個）\n");
            sb.append("═".repeat(40)).append("\n");
            for (String skill : skills) {
                sb.append("• ").append(skill).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return "❌ 讀取 Skills 目錄失敗：" + e.getMessage();
        }
    }

    @Tool(description = "讀取指定 Skill 的 SKILL.md 定義內容")
    public String readSkill(
            @ToolParam(description = "Skill 的名稱（即 .agent/skills 下的資料夾名稱）") String skillName) {

        if (skillName == null || !skillName.matches("[a-zA-Z0-9_-]+")) {
            return "❌ 無效的 Skill 名稱：名稱只能包含英數字、連字號和底線";
        }

        Path skillDir = Paths.get(SKILLS_DIR, skillName);
        if (!Files.exists(skillDir) || !Files.isDirectory(skillDir)) {
            return "❌ 找不到 Skill 目錄：" + skillName;
        }

        Path skillFile = skillDir.resolve(SKILL_FILE);

        if (!Files.exists(skillFile)) {
            return "❌ 找不到 Skill：" + skillName + " 的 " + SKILL_FILE + " 定義檔";
        }

        try {
            String content = Files.readString(skillFile);
            return "📄 Skill：" + skillName + "\n" +
                    "═".repeat(40) + "\n" +
                    content;
        } catch (IOException e) {
            return "❌ 讀取 Skill 內容失敗：" + e.getMessage();
        }
    }
}
