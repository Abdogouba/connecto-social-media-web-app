package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.services.BlockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> block(@PathVariable Long id) throws Exception {
        blockService.block(id);
        return ResponseEntity.status(HttpStatus.OK).body("User blocked successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity unblock(@PathVariable Long id) {
        blockService.unblock(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
