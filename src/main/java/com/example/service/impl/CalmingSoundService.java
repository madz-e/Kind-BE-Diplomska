package com.example.service.impl;

import com.example.jpaRepository.CalmingSoundRepository;
import com.example.model.CalmingSound;
import com.example.model.MindfulnessExercise;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CalmingSoundService {
    private final CalmingSoundRepository calmingSoundRepository;

    // Configuration - you can move these to application.properties
    private final String SOUNDS_DIRECTORY = "uploads/sounds/";
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("mp3", "wav", "ogg", "m4a");
    private final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // 1. Create a new calming sound (with file upload)
    public CalmingSound createCalmingSound(String name, MultipartFile audioFile, boolean loopEnabled) throws IOException {
        // Validate input
        validateSoundInput(name, audioFile);

        // Generate unique filename
        String originalFilename = audioFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + "." + fileExtension;

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(SOUNDS_DIRECTORY);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(audioFile.getInputStream(), filePath);

        // Create and save CalmingSound entity
        CalmingSound calmingSound = new CalmingSound();
        calmingSound.setName(name);
        calmingSound.setAudioUrl("/" + SOUNDS_DIRECTORY + uniqueFilename);
        calmingSound.setLoopEnabled(loopEnabled);

        return calmingSoundRepository.save(calmingSound);
    }

    // 2. Get all calming sounds
    public List<CalmingSound> getAllCalmingSounds() {
        List<CalmingSound> sounds = calmingSoundRepository.findAllByOrderByNameAsc();

        return sounds;
    }

    // 3. Get sound by ID
    public CalmingSound getCalmingSoundById(Long soundId) {
        CalmingSound sound = calmingSoundRepository.findById(soundId)
                .orElseThrow(() -> new RuntimeException("Calming sound not found with id: " + soundId));

        return sound;
    }

    // 4. Update calming sound (metadata only, not audio file)
    public CalmingSound updateCalmingSound(Long soundId, String name, Boolean loopEnabled) {
        CalmingSound sound = calmingSoundRepository.findById(soundId)
                .orElseThrow(() -> new RuntimeException("Calming sound not found with id: " + soundId));

        if (name != null && !name.trim().isEmpty()) {
            sound.setName(name);
        }

        if (loopEnabled != null) {
            sound.setLoopEnabled(loopEnabled);
        }

        return calmingSoundRepository.save(sound);
    }

    // 5. Delete calming sound (with file cleanup)
    public void deleteCalmingSound(Long soundId) {
        CalmingSound sound = calmingSoundRepository.findById(soundId)
                .orElseThrow(() -> new RuntimeException("Calming sound not found with id: " + soundId));

        // Check if sound is being used by any exercises
        if (!sound.getExercises().isEmpty()) {
            throw new RuntimeException("Cannot delete sound. It is being used by " +
                    sound.getExercises().size() + " mindfulness exercises.");
        }

        // Delete audio file from storage
        deleteAudioFile(sound.getAudioUrl());

        // Delete from database
        calmingSoundRepository.delete(sound);
    }

    // 6. Get sounds by exercise (for frontend dropdown)
    public List<Map<String, Object>> getSoundsForExerciseDropdown() {
        List<CalmingSound> allSounds = calmingSoundRepository.findAllByOrderByNameAsc();
        List<Map<String, Object>> dropdownOptions = new ArrayList<>();

        // Add "No Sound" option
        Map<String, Object> noSoundOption = new HashMap<>();
        noSoundOption.put("id", null);
        noSoundOption.put("name", "No Sound");
        noSoundOption.put("loopEnabled", false);
        noSoundOption.put("isDefault", true);
        dropdownOptions.add(noSoundOption);

        // Add all available sounds
        if (allSounds != null) {
            for (CalmingSound sound : allSounds) {
                if (sound != null) {
                    Map<String, Object> soundOption = new HashMap<>();
                    soundOption.put("id", sound.getId());
                    soundOption.put("name", sound.getName() != null ? sound.getName() : "Unknown");
                    soundOption.put("loopEnabled", sound.isLoopEnabled());
                    soundOption.put("isDefault", false);
                    dropdownOptions.add(soundOption);
                }
            }
        }

        return dropdownOptions;
    }

    // 8. Get exercises using a specific sound
    public List<MindfulnessExercise> getExercisesUsingSound(Long soundId) {
        CalmingSound sound = getCalmingSoundById(soundId);
        return new ArrayList<>(sound.getExercises());
    }

    // 9. Search sounds by name (case-insensitive)
    public List<CalmingSound> searchSoundsByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllCalmingSounds();
        }
        return calmingSoundRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    // 10. Toggle loop setting for a sound
    public CalmingSound toggleLoopSetting(Long soundId) {
        CalmingSound sound = calmingSoundRepository.findById(soundId)
                .orElseThrow(() -> new RuntimeException("Calming sound not found with id: " + soundId));

        sound.setLoopEnabled(!sound.isLoopEnabled());

        return calmingSoundRepository.save(sound);
    }

    // 11. Get sound statistics
    public Map<String, Object> getSoundStatistics(Long soundId) {
        CalmingSound sound = calmingSoundRepository.findById(soundId)
                .orElseThrow(() -> new RuntimeException("Calming sound not found with id: " + soundId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("id", sound.getId());
        stats.put("name", sound.getName());
        stats.put("usageCount", sound.getExercises().size());
        stats.put("loopEnabled", sound.isLoopEnabled());
        stats.put("createdDate", getCreationTimestamp(sound.getAudioUrl()));
        stats.put("fileSize", getFileSize(sound.getAudioUrl()));

        // List exercises using this sound
        List<Map<String, Object>> exercises = sound.getExercises().stream()
                .map(exercise -> {
                    Map<String, Object> exerciseMap = new HashMap<>();
                    exerciseMap.put("id", exercise.getId());
                    exerciseMap.put("title", exercise.getTitle());
                    exerciseMap.put("type", exercise.getType().toString());
                    return exerciseMap;
                })
                .collect(Collectors.toList());

        stats.put("usedInExercises", exercises);

        return stats;
    }

    // 12. Initialize default calming sounds (run once on app startup)
    @Transactional
    public void initializeDefaultSounds() {
        if (calmingSoundRepository.count() > 0) {
            log.info("Default sounds already initialized");
            return;
        }

        log.info("Initializing default calming sounds...");

        try {
            // Create default sounds (these would point to pre-uploaded files)
            // In production, these files should exist in your static resources or S3

            createDefaultSound("Gentle Rain", "/static/sounds/rain.mp3", true);
            createDefaultSound("Ocean Waves", "/static/sounds/waves.mp3", true);
            createDefaultSound("Forest Birds", "/static/sounds/forest.mp3", true);
            createDefaultSound("White Noise", "/static/sounds/white-noise.mp3", true);
            createDefaultSound("Calm Piano", "/static/sounds/piano.mp3", false);

            log.info("Successfully initialized {} default sounds", calmingSoundRepository.count());
        } catch (Exception e) {
            log.error("Failed to initialize default sounds: {}", e.getMessage());
        }
    }

    // 13. Bulk update loop settings
    public int bulkUpdateLoopSettings(List<Long> soundIds, boolean loopEnabled) {
        List<CalmingSound> sounds = calmingSoundRepository.findAllById(soundIds);

        sounds.forEach(sound -> sound.setLoopEnabled(loopEnabled));
        calmingSoundRepository.saveAll(sounds);

        return sounds.size();
    }

    // 14. Validate and check audio file existence
    public Map<String, Object> validateSoundFile(Long soundId) {
        CalmingSound sound = calmingSoundRepository.findById(soundId)
                .orElseThrow(() -> new RuntimeException("Calming sound not found with id: " + soundId));

        Map<String, Object> validation = new HashMap<>();
        validation.put("soundId", soundId);
        validation.put("soundName", sound.getName());
        validation.put("audioUrl", sound.getAudioUrl());

        File audioFile = new File(sound.getAudioUrl().replaceFirst("^/", ""));
        boolean fileExists = audioFile.exists();
        validation.put("fileExists", fileExists);

        if (fileExists) {
            validation.put("fileSize", audioFile.length());
            validation.put("lastModified", new Date(audioFile.lastModified()));
        } else {
            validation.put("error", "Audio file not found at path: " + audioFile.getAbsolutePath());
        }

        return validation;
    }

    // ===== PRIVATE HELPER METHODS =====

    private void validateSoundInput(String name, MultipartFile audioFile) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Sound name cannot be empty");
        }

        if (audioFile == null || audioFile.isEmpty()) {
            throw new RuntimeException("Audio file is required");
        }

        // Check file size
        if (audioFile.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Audio file size exceeds maximum limit of " +
                    (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
        }

        // Check file extension
        String originalFilename = audioFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new RuntimeException("Invalid file type. Allowed types: " +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Check if sound name already exists
        Optional<CalmingSound> existingSound = calmingSoundRepository.findByNameIgnoreCase(name.trim());
        if (existingSound.isPresent()) {
            throw new RuntimeException("A sound with name '" + name + "' already exists");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            throw new RuntimeException("File must have an extension");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private void deleteAudioFile(String audioUrl) {
        try {
            if (audioUrl != null && !audioUrl.trim().isEmpty()) {
                // Remove leading slash if present
                String filePath = audioUrl.replaceFirst("^/", "");
                Path path = Paths.get(filePath);

                if (Files.exists(path)) {
                    Files.delete(path);
                    log.info("Deleted audio file: {}", filePath);
                }
            }
        } catch (IOException e) {
            log.error("Failed to delete audio file {}: {}", audioUrl, e.getMessage());
            // Don't throw exception, just log error
        }
    }

    private void createDefaultSound(String name, String audioUrl, boolean loopEnabled) {
        CalmingSound sound = new CalmingSound();
        sound.setName(name);
        sound.setAudioUrl(audioUrl);
        sound.setLoopEnabled(loopEnabled);

        calmingSoundRepository.save(sound);
    }

    private Date getCreationTimestamp(String audioUrl) {
        try {
            String filePath = audioUrl.replaceFirst("^/", "");
            File file = new File(filePath);
            if (file.exists()) {
                return new Date(file.lastModified());
            }
        } catch (Exception e) {
            log.warn("Could not get creation timestamp for: {}", audioUrl);
        }
        return null;
    }

    private String getFileSize(String audioUrl) {
        try {
            String filePath = audioUrl.replaceFirst("^/", "");
            File file = new File(filePath);
            if (file.exists()) {
                long size = file.length();
                if (size < 1024) {
                    return size + " bytes";
                } else if (size < 1024 * 1024) {
                    return (size / 1024) + " KB";
                } else {
                    return String.format("%.2f MB", size / (1024.0 * 1024.0));
                }
            }
        } catch (Exception e) {
            log.warn("Could not get file size for: {}", audioUrl);
        }
        return "Unknown";
    }
}