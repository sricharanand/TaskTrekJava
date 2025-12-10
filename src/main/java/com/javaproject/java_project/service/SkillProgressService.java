package com.javaproject.java_project.service;

import com.javaproject.java_project.model.SkillProgress;
import com.javaproject.java_project.model.User;
import com.javaproject.java_project.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SkillProgressService {

    @Autowired
    AuthService authService;

    UsersRepository usersRepository;

    // Level names with academic/achievement theme
    private static final String[] LEVEL_NAMES = {
            "Novice Scholar",      // Level 1
            "Apprentice",          // Level 2
            "Adept Learner",       // Level 3
            "Knowledge Seeker",    // Level 4
            "Skilled Practitioner",// Level 5
            "Expert",              // Level 6
            "Master Scholar",      // Level 7
            "Grandmaster",         // Level 8
            "Sage",                // Level 9
            "Legendary Virtuoso"   // Level 10
    };

    // XP thresholds for each level (cumulative)
    // Formula: XP(n) = 100 * n^2.5 (rounded)
    private static final int[] XP_THRESHOLDS = {
            0,      // Level 1: Starting point
            283,    // Level 2: 100 * 2^2.5 ≈ 283
            844,    // Level 3: 100 * 3^2.5 ≈ 844
            1789,   // Level 4: 100 * 4^2.5 ≈ 1789
            3162,   // Level 5: 100 * 5^2.5 ≈ 3162
            5097,   // Level 6: 100 * 6^2.5 ≈ 5097
            7588,   // Level 7: 100 * 7^2.5 ≈ 7588
            10734,  // Level 8: 100 * 8^2.5 ≈ 10734
            14627,  // Level 9: 100 * 9^2.5 ≈ 14627
            19149   // Level 10: 100 * 10^2.5 ≈ 19149
    };

    /**
     * Get the current level based on total XP
     * @param totalXp The accumulated XP
     * @return Current level (1-10)
     */
    public int calculateLevel(int totalXp) {
        for (int i = XP_THRESHOLDS.length - 1; i >= 0; i--) {
            if (totalXp >= XP_THRESHOLDS[i]) {
                return i + 1;
            }
        }
        return 1; // Default to level 1
    }

    /**
     * Get the level name for a given level number
     * @param level The level number (1-10)
     * @return The name of the level
     */
    public String getLevelName(int level) {
        if (level < 1 || level > 10) {
            return "Unknown";
        }
        return LEVEL_NAMES[level - 1];
    }

    /**
     * Get XP required to reach the next level
     * @param currentXp Current total XP
     * @return XP needed for next level, or 0 if at max level
     */
    public int getXpToNextLevel(int currentXp) {
        int currentLevel = calculateLevel(currentXp);

        if (currentLevel >= 10) {
            return 0; // Max level reached
        }

        return XP_THRESHOLDS[currentLevel] - currentXp;
    }

    /**
     * Get XP accumulated in current level
     * @param currentXp Current total XP
     * @return XP accumulated in current level
     */
    public int getXpInCurrentLevel(int currentXp) {
        int currentLevel = calculateLevel(currentXp);

        if (currentLevel == 1) {
            return currentXp;
        }

        return currentXp - XP_THRESHOLDS[currentLevel - 1];
    }

    /**
     * Get total XP range for the current level
     * @param currentXp Current total XP
     * @return Total XP range for current level
     */
    public int getTotalXpForCurrentLevel(int currentXp) {
        int currentLevel = calculateLevel(currentXp);

        if (currentLevel >= 10) {
            return 0; // At max level
        }

        int levelStart = (currentLevel == 1) ? 0 : XP_THRESHOLDS[currentLevel - 1];
        int levelEnd = XP_THRESHOLDS[currentLevel];

        return levelEnd - levelStart;
    }

    /**
     * Calculate progress percentage within current level
     * @param currentXp Current total XP
     * @return Progress percentage (0-100)
     */
    public double getLevelProgressPercentage(int currentXp) {
        int currentLevel = calculateLevel(currentXp);

        if (currentLevel >= 10) {
            return 100.0; // Max level
        }

        int xpInLevel = getXpInCurrentLevel(currentXp);
        int totalXpForLevel = getTotalXpForCurrentLevel(currentXp);

        return (totalXpForLevel > 0) ? (xpInLevel * 100.0 / totalXpForLevel) : 0.0;
    }

    /**
     * Initialize skill progress for a new course
     * @param courseName Name of the course
     * @return New SkillProgress object
     */
    public SkillProgress initializeSkillProgress(String courseName) {
        return SkillProgress.builder()
                .courseName(courseName)
                .build();
    }

    /**
     * Get or create skill progress for a course
     * @param courseName Name of the course
     * @return SkillProgress for the course
     */
    public SkillProgress getOrCreateSkillProgress(String courseName) {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        Map<String, SkillProgress> skillProgressMap = currentUser.getSkillProgress();

        // If course doesn't exist in map, create it
        if (!skillProgressMap.containsKey(courseName)) {
            SkillProgress newProgress = initializeSkillProgress(courseName);
            skillProgressMap.put(courseName, newProgress);
        }
        usersRepository.save(currentUser);
        return skillProgressMap.get(courseName);
    }

    /**
     * Add XP to a specific course and update its level
     * @param courseName Name of the course
     * @param xpAmount Amount of XP to add
     * @return Map containing level up information, or null if no user logged in
     */
    public Map<String, Object> addXpToCourse(String courseName, int xpAmount) {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            return null; // No logged-in user
        }

        // Validate xpAmount
        if (xpAmount <= 0) {
            return null; // Invalid XP amount
        }

        SkillProgress progress = getOrCreateSkillProgress(courseName);

        int oldLevel = progress.getLevel();
        int oldXp = progress.getXp();

        // Add XP
        int newXp = oldXp + xpAmount;
        progress.setXp(newXp);

        // Calculate new level
        int newLevel = calculateLevel(newXp);
        progress.setLevel(newLevel);
        progress.setLevelName(getLevelName(newLevel));

        // Also add XP to user's overall progress
        int oldUserLevel = currentUser.getLevel();
        currentUser.setXp(currentUser.getXp() + xpAmount);
        int userLevel = calculateLevel(currentUser.getXp());
        currentUser.setLevel(userLevel);

        // Prepare response
        Map<String, Object> result = new HashMap<>();
        result.put("courseName", courseName);
        result.put("xpAdded", xpAmount);

        // Course progress info
        result.put("course", Map.of(
                "oldXp", oldXp,
                "newXp", newXp,
                "oldLevel", oldLevel,
                "newLevel", newLevel,
                "leveledUp", newLevel > oldLevel,
                "newLevelName", getLevelName(newLevel),
                "levelsGained", newLevel - oldLevel
        ));

        // User overall progress info
        result.put("user", Map.of(
                "level", userLevel,
                "totalXp", currentUser.getXp(),
                "leveledUp", userLevel > oldUserLevel,
                "levelsGained", userLevel - oldUserLevel
        ));
        usersRepository.save(currentUser);
        return result;
    }

    /**
     * Get detailed progress for a specific course
     * @param courseName Name of the course
     * @return Map containing all progress details
     */
    public Map<String, Object> getCourseProgress(String courseName) {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        SkillProgress progress = getOrCreateSkillProgress(courseName);

        int totalXp = progress.getXp();
        int level = progress.getLevel();

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("courseName", courseName);
        progressData.put("totalXp", totalXp);
        progressData.put("level", level);
        progressData.put("levelName", progress.getLevelName());
        progressData.put("xpInCurrentLevel", getXpInCurrentLevel(totalXp));
        progressData.put("xpToNextLevel", getXpToNextLevel(totalXp));
        progressData.put("totalXpForCurrentLevel", getTotalXpForCurrentLevel(totalXp));
        progressData.put("progressPercentage", getLevelProgressPercentage(totalXp));
        progressData.put("isMaxLevel", level >= 10);

        return progressData;
    }

    /**
     * Get progress for all courses of current user
     * @return Map of course names to progress data
     */
    public Map<String, Map<String, Object>> getAllCourseProgress() {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        Map<String, Map<String, Object>> allProgress = new HashMap<>();

        for (String courseName : currentUser.getSkillProgress().keySet()) {
            allProgress.put(courseName, getCourseProgress(courseName));
        }

        return allProgress;
    }

    /**
     * Get overall user progress (not course-specific)
     * @return Map containing user's overall progress details
     */
    public Map<String, Object> getOverallUserProgress() {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        int totalXp = currentUser.getXp();
        int level = currentUser.getLevel();

        Map<String, Object> progress = new HashMap<>();
        progress.put("totalXp", totalXp);
        progress.put("level", level);
        progress.put("levelName", getLevelName(level));
        progress.put("xpInCurrentLevel", getXpInCurrentLevel(totalXp));
        progress.put("xpToNextLevel", getXpToNextLevel(totalXp));
        progress.put("totalXpForCurrentLevel", getTotalXpForCurrentLevel(totalXp));
        progress.put("progressPercentage", getLevelProgressPercentage(totalXp));
        progress.put("isMaxLevel", level >= 10);
        progress.put("totalCourses", currentUser.getSkillProgress().size());

        return progress;
    }

    /**
     * Get all level thresholds and names for reference
     * @return Map of level progression data
     */
    public Map<String, Object> getAllLevelInfo() {
        Map<String, Object> levelInfo = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            Map<String, Object> levelData = new HashMap<>();
            levelData.put("level", i + 1);
            levelData.put("name", LEVEL_NAMES[i]);
            levelData.put("xpThreshold", XP_THRESHOLDS[i]);

            if (i < 9) {
                levelData.put("xpRequired", XP_THRESHOLDS[i + 1] - XP_THRESHOLDS[i]);
            } else {
                levelData.put("xpRequired", "MAX LEVEL");
            }

            levelInfo.put("level" + (i + 1), levelData);
        }

        return levelInfo;
    }

    /**
     * Delete skill progress for a course (when course is deleted)
     * @param courseName Name of the course to remove
     * @return true if removed, false if not found
     */
    public boolean removeSkillProgress(String courseName) {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            return false;
        }

        boolean removeSuccess = (currentUser.getSkillProgress().remove(courseName) != null);
        if (removeSuccess){
            usersRepository.save(currentUser);
        }
        return removeSuccess;
    }

    /**
     * Get leaderboard data showing best performing courses
     * @return List of courses sorted by level and XP
     */
    public Map<String, Object> getCourseLeaderboard() {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        Map<String, SkillProgress> skillProgressMap = currentUser.getSkillProgress();

        // Sort courses by level (desc), then by XP (desc)
        var sortedCourses = skillProgressMap.entrySet().stream()
                .sorted((e1, e2) -> {
                    SkillProgress p1 = e1.getValue();
                    SkillProgress p2 = e2.getValue();

                    if (p1.getLevel() != p2.getLevel()) {
                        return Integer.compare(p2.getLevel(), p1.getLevel());
                    }
                    return Integer.compare(p2.getXp(), p1.getXp());
                })
                .toList();

        Map<String, Object> leaderboard = new HashMap<>();
        leaderboard.put("topCourses", sortedCourses);
        leaderboard.put("totalCourses", skillProgressMap.size());

        return leaderboard;
    }

    /**
     * Helper method for TaskService to call when a task is completed
     * Calculates XP based on task difficulty/priority and adds to course
     * @param courseName Name of the course
     * @param baseXp Base XP amount (can be modified by difficulty multiplier)
     * @param difficultyMultiplier Multiplier based on task difficulty (e.g., 1.0, 1.5, 2.0)
     * @return Map containing level up information
     */
    public Map<String, Object> awardTaskCompletionXp(String courseName, int baseXp, double difficultyMultiplier, int streak)
    {
        int extraXP = streak * 10; // streak bonus

        int finalXp = (int) Math.round(baseXp * difficultyMultiplier) + extraXP;
        return addXpToCourse(courseName, finalXp);
    }
}