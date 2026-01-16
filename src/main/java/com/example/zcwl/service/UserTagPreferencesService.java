package com.example.zcwl.service;

import com.example.zcwl.entity.UserTagPreferences;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 用户标签偏好服务接口
 * 提供用户对标签偏好分数的业务逻辑操作
 */
public interface UserTagPreferencesService {

    /**
     * 创建用户标签偏好
     * @param userTagPreference 用户标签偏好实体对象，包含用户ID、标签ID和偏好分数
     * @return 创建成功的用户标签偏好实体对象
     */
    UserTagPreferences createUserTagPreference(UserTagPreferences userTagPreference);

    /**
     * 根据复合主键查询用户标签偏好
     * @param id 复合主键对象，包含用户ID和标签ID
     * @return 包含用户标签偏好实体的Optional对象，如果未找到则返回Optional.empty()
     */
    Optional<UserTagPreferences> getUserTagPreferenceById(UserTagPreferences.UserTagPreferencesId id);

    /**
     * 根据用户ID查询所有用户标签偏好
     * @param userId 用户ID
     * @return 该用户对所有标签的偏好列表
     */
    List<UserTagPreferences> getUserTagPreferencesByUserId(String userId);

    /**
     * 根据标签ID查询所有用户标签偏好
     * @param tagId 标签ID
     * @return 所有用户对该标签的偏好列表
     */
    List<UserTagPreferences> getUserTagPreferencesByTagId(Integer tagId);

    /**
     * 查询所有用户标签偏好（分页）
     * @param pageable 分页参数，包含页码、每页大小等信息
     * @return 包含用户标签偏好实体的分页结果
     */
    Page<UserTagPreferences> getAllUserTagPreferences(Pageable pageable);

    /**
     * 更新用户标签偏好
     * @param id 复合主键对象，包含用户ID和标签ID
     * @param userTagPreference 包含更新信息的用户标签偏好实体对象
     * @return 更新后的用户标签偏好实体对象
     */
    UserTagPreferences updateUserTagPreference(UserTagPreferences.UserTagPreferencesId id, UserTagPreferences userTagPreference);

    /**
     * 删除用户标签偏好
     * @param id 复合主键对象，包含用户ID和标签ID
     */
    void deleteUserTagPreference(UserTagPreferences.UserTagPreferencesId id);

}