package com.rex.dockerdemo.service;



import com.rex.dockerdemo.entity.UserBase;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserBase create(UserBase user);
    Optional<UserBase> findByUuid(String uuid);
    List<UserBase> findAll();
    UserBase update(String uuid, UserBase user);
    void deleteByUuid(String uuid);
}

