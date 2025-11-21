package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.models.Block;
import com.socialmedia.connecto.models.Role;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.BlockRepository;
import com.socialmedia.connecto.services.BlockService;
import com.socialmedia.connecto.services.FollowRequestService;
import com.socialmedia.connecto.services.FollowService;
import com.socialmedia.connecto.services.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;
import java.util.Optional;

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

        if (blockRepository.existsByBlockerAndBlocked(currentUser, target))
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

}
