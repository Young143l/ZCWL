package com.example.zcwl.service.impl;

import com.example.zcwl.entity.UserTagPreferences;
import com.example.zcwl.repository.UserTagPreferencesRepository;
import com.example.zcwl.service.UserTagPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 用户标签偏好服务实现类
 * 实现了UserTagPreferencesService接口定义的业务逻辑方法
 */
@Service  // 声明这是一个服务类，用于业务逻辑实现
public class UserTagPreferencesServiceImpl implements UserTagPreferencesService {

    private final UserTagPreferencesRepository userTagPreferencesRepository;

    /**
     * 构造函数注入
     * @param userTagPreferencesRepository 用户标签偏好数据访问接口实例
     */
    @Autowired  // 自动注入UserTagPreferencesRepository依赖，用于数据库操作
    public UserTagPreferencesServiceImpl(UserTagPreferencesRepository userTagPreferencesRepository) {
        this.userTagPreferencesRepository = userTagPreferencesRepository;
    }

    /**
     * 创建用户标签偏好
     * @param userTagPreference 用户标签偏好实体对象，包含用户ID、标签ID和偏好分数
     * @return 创建成功的用户标签偏好实体
     */
    @Override
    public UserTagPreferences createUserTagPreference(UserTagPreferences userTagPreference) {
        // 调用Repository的save方法保存用户标签偏好实体
        return userTagPreferencesRepository.save(userTagPreference);
    }

    /**
     * 根据复合主键查询用户标签偏好
     * @param id 复合主键对象，包含用户ID和标签ID
     * @return 包含用户标签偏好实体的Optional对象，如果不存在则返回Optional.empty()
     */
    @Override
    public Optional<UserTagPreferences> getUserTagPreferenceById(UserTagPreferences.UserTagPreferencesId id) {
        // 调用Repository的findById方法根据复合主键查询
        return userTagPreferencesRepository.findById(id);
    }

    /**
     * 根据用户ID查询所有用户标签偏好
     * @param userId 用户ID
     * @return 该用户对所有标签的偏好列表
     */
    @Override
    public List<UserTagPreferences> getUserTagPreferencesByUserId(String userId) {
        // 调用Repository的findByIdUId方法根据用户ID查询
        return userTagPreferencesRepository.findByIdUId(userId);
    }

    /**
     * 根据标签ID查询所有用户标签偏好
     * @param tagId 标签ID
     * @return 所有用户对该标签的偏好列表
     */
    @Override
    public List<UserTagPreferences> getUserTagPreferencesByTagId(Integer tagId) {
        // 调用Repository的findByIdTId方法根据标签ID查询
        return userTagPreferencesRepository.findByIdTId(tagId);
    }

    /**
     * 查询所有用户标签偏好（分页）
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页后的用户标签偏好列表
     */
    @Override
    public Page<UserTagPreferences> getAllUserTagPreferences(Pageable pageable) {
        // 调用Repository的findAll方法，带分页参数
        return userTagPreferencesRepository.findAll(pageable);
    }

    /**
     * 更新用户标签偏好
     * @param id 复合主键对象，包含用户ID和标签ID
     * @param userTagPreference 更新后的用户标签偏好信息
     * @return 更新成功的用户标签偏好实体
     * @throws RuntimeException 如果用户标签偏好不存在
     */
    @Override
    public UserTagPreferences updateUserTagPreference(UserTagPreferences.UserTagPreferencesId id, UserTagPreferences userTagPreference) {
        // 先根据复合主键查询现有用户标签偏好
        Optional<UserTagPreferences> optionalUserTagPreference = userTagPreferencesRepository.findById(id);
        if (optionalUserTagPreference.isPresent()) {
            // 如果存在，更新用户标签偏好信息
            UserTagPreferences existingUserTagPreference = optionalUserTagPreference.get();
            existingUserTagPreference.setPre(userTagPreference.getPre());
            existingUserTagPreference.setUser(userTagPreference.getUser());
            existingUserTagPreference.setTags(userTagPreference.getTags());
            // 保存更新后的实体
            return userTagPreferencesRepository.save(existingUserTagPreference);
        }
        // 如果不存在，抛出运行时异常
        throw new RuntimeException("UserTagPreference not found with id: " + id);
    }

    /**
     * 删除用户标签偏好
     * @param id 复合主键对象，包含用户ID和标签ID
     */
    @Override
    public void deleteUserTagPreference(UserTagPreferences.UserTagPreferencesId id) {
        // 调用Repository的deleteById方法根据复合主键删除
        userTagPreferencesRepository.deleteById(id);
    }

}