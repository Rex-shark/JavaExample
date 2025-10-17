package com.rex.jpamysql.service.impl;

import com.rex.jpamysql.entity.UserBase;
import com.rex.jpamysql.repository.UserRepository;
import com.rex.jpamysql.service.UserService;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserRepository userRepository;

    @Override
    public UserBase create(UserBase user) {
        System.out.println("user = " + user);
        if (user.getCreatedUserId() == null) {
            //user.setCreatedUserId(1L);
            throw new IllegalArgumentException("createdUserId is mandatory");
        }

        if (user.getUuid() == null || user.getUuid().isEmpty()) {
            user.setUuid(UUID.randomUUID().toString());
        } else {
            if (userRepository.existsByUuid(user.getUuid())) {
                throw new IllegalArgumentException("uuid already exists");
            }
        }

        if (user.getAccount() != null && userRepository.existsByAccount(user.getAccount())) {
            throw new IllegalArgumentException("account already exists");
        }

        return userRepository.save(user);
    }

    @Override
    public Optional<UserBase> findByUuid(String uuid) {
        return userRepository.findByUuid(uuid);
    }

    @Override
    public List<UserBase> findAll() {
        return userRepository.findAll();
    }

    @Override
    public UserBase update(String uuid, UserBase user) {
        Optional<UserBase> opt = userRepository.findByUuid(uuid);
        if (opt.isEmpty()) {
            throw new EntityNotFoundException("User not found: " + uuid);
        }
        UserBase exist = opt.get();

        if (user.getAccount() != null && !user.getAccount().equals(exist.getAccount())) {
            if (userRepository.existsByAccount(user.getAccount())) {
                throw new IllegalArgumentException("account already exists");
            }
            exist.setAccount(user.getAccount());
        }

        if (user.getPassword() != null) {
            exist.setPassword(user.getPassword());
        }
        if (user.getRemark() != null) {
            exist.setRemark(user.getRemark());
        }
        if (user.getUpdateUserId() != null) {
            exist.setUpdateUserId(user.getUpdateUserId());
        }

        return userRepository.save(exist);
    }

    @Override
    public void deleteByUuid(String uuid) {
        Optional<UserBase> opt = userRepository.findByUuid(uuid);
        if (opt.isEmpty()) {
            throw new EntityNotFoundException("User not found: " + uuid);
        }
        userRepository.delete(opt.get());
    }
}
