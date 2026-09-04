package com.boardwise.backend.vault.controller;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.vault.dto.request.CommitEditDeltaOrDoActionRequestDto;
import com.boardwise.backend.vault.dto.request.DeleteChunkRequestDto;
import com.boardwise.backend.vault.dto.request.InsertNewChunkRequestDto;
import com.boardwise.backend.vault.dto.response.AcquireWriteLockDto;
import com.boardwise.backend.vault.dto.response.CommitEditDeltaResponseDto;
import com.boardwise.backend.vault.dto.response.DeleteChunkResponseDto;
import com.boardwise.backend.vault.dto.response.InsertNewChunkResponseDto;
import com.boardwise.backend.vault.dto.response.UndoOrRedoActionResponseDto;
import com.boardwise.backend.vault.service.WriteLockService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/api/sb/vault/rulebooks")
@RequiredArgsConstructor
public class WriteLockController {
    private final WriteLockService writeLockService;
    private final JWTService jwtService;
    
    // AC-VLT-06: Aquire Write Lock
    @PostMapping("/{id}/lock/acquire")
    public ResponseEntity<AcquireWriteLockDto> getWriteLock(
        @PathVariable("id") String rulebookId,
        @RequestHeader("Authorization") String authHeader) {
            ObjectId userId = extractUserIdFromHeader(authHeader);

        return ResponseEntity.ok(
            writeLockService.acquireWriteLock(toObjectId(rulebookId), userId)
        );
    }
    
    // AC-VLT-07: Commit Edit Delta
    @PatchMapping("/{id}/chunk/update")
    public ResponseEntity<CommitEditDeltaResponseDto> commitDelta(
        @PathVariable("id") String rulebookId,
        @RequestHeader("Authorization") String authHeader,
        @RequestBody CommitEditDeltaOrDoActionRequestDto request){
            ObjectId userId = extractUserIdFromHeader(authHeader);

            return ResponseEntity.ok(
                writeLockService.commitEditDelta(toObjectId(rulebookId), userId, request)
            );
        }

    // AC-VLT-08: Release Write Lock
    @PostMapping("/{id}/lock/release")
    public ResponseEntity<Void> releaseLock(
        @PathVariable("id") String rulebookId,
        @RequestHeader("Authorization") String authHeader){
            ObjectId userId = extractUserIdFromHeader(authHeader);

            writeLockService.releaseWriteLock(toObjectId(rulebookId), userId);
            
            return ResponseEntity.ok().build();
        }

    @PostMapping("/lock/release-all")
    public ResponseEntity<Void> releaseAllLocksForUser(@RequestHeader("Authorization") String authHeader){
            ObjectId userId = extractUserIdFromHeader(authHeader);

            writeLockService.releaseAllWriteLocksForUser(userId);
            
            return ResponseEntity.ok().build();
        }

    @PostMapping("/{id}/chunk/insert")
    public ResponseEntity<InsertNewChunkResponseDto> insertChunk(
        @PathVariable("id") String rulebookId,
        @RequestHeader("Authorization") String authHeader,
        @RequestBody InsertNewChunkRequestDto request){
            ObjectId userId = extractUserIdFromHeader(authHeader);

            return ResponseEntity.ok(
                writeLockService.insertNewChunk(toObjectId(rulebookId), userId, request)
            );
        }

    @DeleteMapping("/{id}/chunk/remove")
    public ResponseEntity<DeleteChunkResponseDto> deleteChunk(
        @PathVariable("id") String rulebookId,
        @RequestHeader("Authorization") String authHeader,
        @RequestBody DeleteChunkRequestDto request){
        ObjectId userId = extractUserIdFromHeader(authHeader);

        return ResponseEntity.ok(
                writeLockService.removeChunk(toObjectId(rulebookId), userId, request));
        }

    @PostMapping("/{id}/action/undo")
    public ResponseEntity<UndoOrRedoActionResponseDto> undoEdit(
        @PathVariable("id") String rulebookId,
        @RequestHeader("Authorization") String authHeader,
        @RequestBody CommitEditDeltaOrDoActionRequestDto request){
            ObjectId userId = extractUserIdFromHeader(authHeader);
        
            return ResponseEntity.ok(
                writeLockService.undoAction(toObjectId(rulebookId), userId, request));
    }

    @PostMapping("/{id}/action/redo")
    public ResponseEntity<UndoOrRedoActionResponseDto> redoEdit(
            @PathVariable("id") String rulebookId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CommitEditDeltaOrDoActionRequestDto request) {
        ObjectId userId = extractUserIdFromHeader(authHeader);

        return ResponseEntity.ok(
                writeLockService.redoAction(toObjectId(rulebookId), userId, request));
    }
    

    // ----- Private Helpers -----
    private ObjectId toObjectId(String id){
        try{
            return new ObjectId(id);
        }catch(IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid rulebook ID format: " + id);
        }
    }

    private ObjectId extractUserIdFromHeader(String authHeader){
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            return jwtService.extractUserId(token);
        }
        throw new IllegalArgumentException("Missing or invalid Authorization header");
    }
}
