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
            if (totalXp >= XP_THRESHOLDS[i])
                return i + 1;
        }
        return 1; // Default to level 1
    }

    /**
     * Get the level name for a given level number
     * @param level The level number (1-10)
     * @return The name of the level
     */
    public String getLevelName(int level) {
        if (level < 1) {
            return "Unknown";
        }
        return LEVEL_NAMES[level - 1];
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

        if (currentUser == null)
            return null;

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
        if (xpAmount <= 0)
            return null;

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
