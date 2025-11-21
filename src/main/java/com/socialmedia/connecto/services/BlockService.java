package com.socialmedia.connecto.services;

public interface BlockService {

    void block(Long id) throws Exception;

    void unblock(Long id);

}
