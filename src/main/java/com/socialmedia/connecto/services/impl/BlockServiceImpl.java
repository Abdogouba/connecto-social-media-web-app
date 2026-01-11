package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.BlockedUserDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.models.Block;
import com.socialmedia.connecto.enums.Role;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.BlockRepository;
import com.socialmedia.connecto.services.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BlockServiceImpl implements BlockService {

    private final UserService userService;
    private final BlockRepository blockRepository;
    private final FollowService followService;
    private final FollowRequestService followRequestService;

    public BlockServiceImpl(UserService userService, BlockRepository blockRepository, FollowService followService, FollowRequestService followRequestService) {
        this.userService = userService;
        this.blockRepository = blockRepository;
        this.followService = followService;
        this.followRequestService = followRequestService;
    }

    @Override
    @Transactional
    public void block(Long id) throws Exception {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getId().equals(id))
            throw new IllegalArgumentException("User cannot block himself");

        User target = userService.getUserById(id)
                .orElseThrow(() -> new NoSuchElementException("User to be blocked not found"));

        if (!target.getRole().equals(Role.USER))
            throw new AccessDeniedException("User cannot block admins");

        if (blockRepository.existsByBlockerIdAndBlockedId(currentUser.getId(), target.getId()))
            throw new IllegalStateException("This user is already blocked");

        Block block = new Block();
        block.setBlocker(currentUser);
        block.setBlocked(target);
        blockRepository.save(block);

        followService.removeFollowRelationshipsIfExists(currentUser, target);
        followRequestService.removeFollowRequestRelationshipsIfExists(currentUser, target);
    }

    @Override
    @Transactional
    public void unblock(Long id) {
        User currentUser = userService.getCurrentUser();
        blockRepository.deleteByBlockerIdAndBlockedId(currentUser.getId(), id);
    }

    @Override
    public PagedDTO<BlockedUserDTO> getBlockedUsers(int page, int size) {
        User user = userService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);

        Page<Block> blockPage = blockRepository.findAllByBlockerIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<BlockedUserDTO> dtos = blockPage.getContent().stream().map(b -> {
            BlockedUserDTO dto = new BlockedUserDTO();
            dto.setId(b.getBlocked().getId());
            dto.setName(b.getBlocked().getName());
            dto.setBlockedAt(b.getCreatedAt());
            return dto;
        }).toList();

        return new PagedDTO<BlockedUserDTO>(
                dtos,
                blockPage.getNumber(),
                blockPage.getTotalPages(),
                blockPage.getTotalElements()
        );
    }

    @Override
    public boolean isBlocked(Long blockerId, Long blockedId) {
        return blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

}
