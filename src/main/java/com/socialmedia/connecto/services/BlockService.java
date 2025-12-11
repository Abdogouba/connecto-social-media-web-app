package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.BlockedUserDTO;
import com.socialmedia.connecto.dtos.PagedDTO;

public interface BlockService {

    void block(Long id) throws Exception;

    void unblock(Long id);

    PagedDTO<BlockedUserDTO> getBlockedUsers(int page, int size);

    boolean isBlocked(Long blockerId, Long blockedId);

}
