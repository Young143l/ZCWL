package com.example.zcwl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 用户标签偏好实体类
 * 映射到数据库的user_tag_preferences表，存储用户对标签的偏好分数
 */
@Setter
@Getter
@Entity  // 声明这是一个JPA实体类，用于映射数据库表
@Table(name = "user_tag_preferences")  // 指定映射的数据库表名
public class UserTagPreferences implements Serializable {  // 实现Serializable接口，支持序列化

    @Serial
    private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

    /**
     * 复合主键
     * 使用嵌入式主键类UserTagPreferencesId
     * -- GETTER --
     *  获取复合主键*
     * -- SETTER --
     *  设置复合主键
     *
     */
    @EmbeddedId  // 声明使用嵌入式主键
    private UserTagPreferencesId id;

    /**
     * 偏好分数
     * 取值范围[-100, 100]，非空，使用@NotNull、@Min、@Max进行数据校验
     * -- GETTER --
     *  获取偏好分数
     * -- SETTER --
     *  设置偏好分数
     *
     */
    @NotNull(message = "偏好分不能为空")  // 数据校验，确保偏好分不为空
    @Min(value = -100, message = "偏好分不能小于-100")  // 数据校验，确保偏好分最小值为-100
    @Max(value = 100, message = "偏好分不能大于100")  // 数据校验，确保偏好分最大值为100
    @Column(name = "pre", nullable = false, columnDefinition = "INTEGER NOT NULL CHECK (pre BETWEEN -100 AND 100)")  // 映射到数据库的pre列，非空约束和CHECK约束
    private Integer pre;

    /**
     * 关联的用户
     * 多对一关系，使用懒加载，关联到User实体
     * -- GETTER --
     *  获取关联的用户
     * -- SETTER --
     *  设置关联的用户
     *
     */
    @ManyToOne(fetch = FetchType.LAZY)  // 声明多对一关系，使用懒加载
    @MapsId("uId")  // 映射嵌入式主键中的uId字段
    @JoinColumn(name = "u_id", referencedColumnName = "u_id", nullable = false, foreignKey = @ForeignKey(name = "user_tag_preferences_u_id_fkey", foreignKeyDefinition = "FOREIGN KEY (u_id) REFERENCES users(u_id) ON DELETE CASCADE"))  // 指定外键关联，添加ON DELETE CASCADE
    private User user;

    /**
     * 关联的标签
     * 多对一关系，使用懒加载，关联到Tags实体
     * -- GETTER --
     *  获取关联的标签
     * -- SETTER --
     *  设置关联的标签
     *
     */
    @ManyToOne(fetch = FetchType.LAZY)  // 声明多对一关系，使用懒加载
    @MapsId("tId")  // 映射嵌入式主键中的tId字段
    @JoinColumn(name = "t_id", referencedColumnName = "t_id", nullable = false, foreignKey = @ForeignKey(name = "user_tag_preferences_t_id_fkey", foreignKeyDefinition = "FOREIGN KEY (t_id) REFERENCES tags(t_id) ON DELETE CASCADE"))  // 指定外键关联，添加ON DELETE CASCADE
    private Tags tags;

    /**
     * 嵌入式主键类
     * 用于UserTagPreferences实体的复合主键
     */
    @Embeddable  // 声明这是一个嵌入式主键类
    public static class UserTagPreferencesId implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;  // 序列化版本号，用于版本控制

        /**
         * 用户ID
         * 作为复合主键的一部分，关联到users表的u_id字段
         */
        @Column(name = "u_id", length = 20, nullable = false)  // 映射到数据库的u_id列，指定长度和非空约束
        private String uId;

        /**
         * 标签ID
         * 作为复合主键的一部分，关联到tags表的t_id字段
         */
        @Column(name = "t_id", nullable = false)  // 映射到数据库的t_id列，非空约束
        private Integer tId;

        /**
         * 默认构造器
         * JPA要求必须提供默认构造器
         */
        public UserTagPreferencesId() {
        }

        /**
         * 带参数的构造器
         * 用于创建UserTagPreferencesId对象
         * @param uId 用户ID
         * @param tId 标签ID
         */
        public UserTagPreferencesId(String uId, Integer tId) {
            this.uId = uId;
            this.tId = tId;
        }

        /**
         * 获取用户ID
         * @return 用户ID
         */
        public String getuId() {
            return uId;
        }

        /**
         * 设置用户ID
         * @param uId 用户ID
         */
        public void setuId(String uId) {
            this.uId = uId;
        }

        /**
         * 获取标签ID
         * @return 标签ID
         */
        public Integer gettId() {
            return tId;
        }

        /**
         * 设置标签ID
         * @param tId 标签ID
         */
        public void settId(Integer tId) {
            this.tId = tId;
        }

        /**
         * 重写equals方法
         * 用于判断两个UserTagPreferencesId对象是否相等
         * @param o 比较的对象
         * @return 是否相等
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserTagPreferencesId that = (UserTagPreferencesId) o;

            if (!Objects.equals(uId, that.uId)) return false;
            return Objects.equals(tId, that.tId);
        }

        /**
         * 重写hashCode方法
         * 用于生成UserTagPreferencesId对象的哈希码
         * @return 哈希码
         */
        @Override
        public int hashCode() {
            int result = uId != null ? uId.hashCode() : 0;
            result = 31 * result + (tId != null ? tId.hashCode() : 0);
            return result;
        }
    }

}