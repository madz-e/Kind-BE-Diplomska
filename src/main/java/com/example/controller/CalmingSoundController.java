package com.example.controller;

import com.example.model.CalmingSound;
import com.example.model.MindfulnessExercise;
import com.example.service.impl.CalmingSoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calming-sounds")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // For mobile app development
public class CalmingSoundController {

    private final CalmingSoundService calmingSoundService;

    /**
     * POST /api/calming-sounds/init
     * Initialize default calming sounds
     * HAS TO BE CALLED FOR CALMING SOUNDS TO WORK
     * Response:
     * {
     * "success": true,
     * "message": "Default calming sounds initialized",
     * "count": 5,
     * "timestamp": "2024-01-15T14:30:00"
     * }
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initializeCalmingSounds() {
        calmingSoundService.initializeDefaultSounds();
        Map<String, Object> response = buildSuccessResponse("Default calming sounds initialized",
                Map.of("count", calmingSoundService.getAllCalmingSounds().size()));

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/calming-sounds
     * Get all calming sounds
     * Response: List of CalmingSound objects
     */
    @GetMapping
    public ResponseEntity<List<CalmingSound>> getAllCalmingSounds() {
        return ResponseEntity.ok(calmingSoundService.getAllCalmingSounds());
    }

    /**
     * GET /api/calming-sounds/{id}
     * Get specific calming sound by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CalmingSound> getCalmingSoundById(@PathVariable Long id) {
        return ResponseEntity.ok(calmingSoundService.getCalmingSoundById(id));
    }

    /**
     * GET /api/calming-sounds/dropdown
     * Get sounds formatted for dropdown selection
     * Includes a "No Sound" option
     * Response:
     * [
     * { "id": null, "name": "No Sound", "loopEnabled": false, "isDefault": true },
     * { "id": 1, "name": "Gentle Rain", "loopEnabled": true, "isDefault": false }
     * ]
     */
    @GetMapping("/dropdown")
    public ResponseEntity<List<Map<String, Object>>> getSoundsForDropdown() {
        return ResponseEntity.ok(calmingSoundService.getSoundsForExerciseDropdown());
    }

    /**
     * GET /api/calming-sounds/search?name=rain
     * Search calming sounds by name (case-insensitive)
     */
    @GetMapping("/search")
    public ResponseEntity<List<CalmingSound>> searchSounds(@RequestParam String name) {
        return ResponseEntity.ok(calmingSoundService.searchSoundsByName(name));
    }

    /**
     * GET /api/calming-sounds/{id}/exercises
     * Get all mindfulness exercises using this sound
     */
    @GetMapping("/{id}/exercises")
    public ResponseEntity<List<MindfulnessExercise>> getExercisesUsingSound(@PathVariable Long id) {
        return ResponseEntity.ok(calmingSoundService.getExercisesUsingSound(id));
    }

    /**
     * GET /api/calming-sounds/{id}/statistics
     * Get detailed statistics about a sound
     * Response:
     * {
     * "id": 1,
     * "name": "Gentle Rain",
     * "usageCount": 3,
     * "loopEnabled": true,
     * "createdDate": "2024-01-15T10:00:00",
     * "fileSize": "2.5 MB",
     * "usedInExercises": [...]
     * }
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<Map<String, Object>> getSoundStatistics(@PathVariable Long id) {
        return ResponseEntity.ok(calmingSoundService.getSoundStatistics(id));
    }

    /**
     * GET /api/calming-sounds/{id}/validate
     * Validate that the audio file exists for this sound
     */
    @GetMapping("/{id}/validate")
    public ResponseEntity<Map<String, Object>> validateSoundFile(@PathVariable Long id) {
        return ResponseEntity.ok(calmingSoundService.validateSoundFile(id));
    }

    /**
     * POST /api/calming-sounds/upload
     * Create a new calming sound with audio file upload
     * Request: multipart/form-data
     * - name: String (sound name)
     * - audioFile: File (audio file: mp3, wav, ogg, m4a)
     * - loopEnabled: boolean (default: true)
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> createCalmingSound(
            @RequestParam String name,
            @RequestParam MultipartFile audioFile,
            @RequestParam(defaultValue = "true") boolean loopEnabled) {

        try {
            CalmingSound sound = calmingSoundService.createCalmingSound(name, audioFile, loopEnabled);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(buildSuccessResponse("Calming sound created successfully", sound));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Failed to upload audio file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    /**
     * PUT /api/calming-sounds/{id}
     * Update calming sound metadata (name and loop setting)
     * Does NOT update the audio file
     * Request Body (optional fields):
     * {
     * "name": "New Sound Name",
     * "loopEnabled": false
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<CalmingSound> updateCalmingSound(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean loopEnabled) {

        CalmingSound updated = calmingSoundService.updateCalmingSound(id, name, loopEnabled);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/calming-sounds/{id}/toggle-loop
     * Toggle the loop setting for a sound
     */
    @PatchMapping("/{id}/toggle-loop")
    public ResponseEntity<CalmingSound> toggleLoopSetting(@PathVariable Long id) {
        return ResponseEntity.ok(calmingSoundService.toggleLoopSetting(id));
    }

    /**
     * PATCH /api/calming-sounds/bulk-loop
     * Bulk update loop settings for multiple sounds
     * Request Body:
     * {
     * "soundIds": [1, 2, 3],
     * "loopEnabled": true
     * }
     */
    @PatchMapping("/bulk-loop")
    public ResponseEntity<Map<String, Object>> bulkUpdateLoopSettings(
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        List<Long> soundIds = (List<Long>) request.get("soundIds");
        Boolean loopEnabled = (Boolean) request.get("loopEnabled");

        int updatedCount = calmingSoundService.bulkUpdateLoopSettings(soundIds, loopEnabled);

        Map<String, Object> data = Map.of(
                "updatedCount", updatedCount,
                "loopEnabled", loopEnabled
        );

        return ResponseEntity.ok(buildSuccessResponse("Loop settings updated", data));    }

    /**
     * DELETE /api/calming-sounds/{id}
     * Delete a calming sound
     * Will fail if the sound is being used by any mindfulness exercises
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCalmingSound(@PathVariable Long id) {
        try {
            calmingSoundService.deleteCalmingSound(id);
            return ResponseEntity.ok(buildSuccessResponse("Calming sound deleted successfully",null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    private Map<String, Object> buildSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now().toString());
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    private Map<String, Object> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", LocalDateTime.now().toString());
        return error;
    }
}