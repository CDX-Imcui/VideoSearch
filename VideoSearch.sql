CREATE
DATABASE IF NOT EXISTS VideoSearch CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE VideoSearch;
SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;



DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`
(
    `id`            int                                NOT NULL AUTO_INCREMENT,
    `username`      varchar(255) CHARACTER SET utf8mb4 NOT NULL,
    `password`      varchar(255) CHARACTER SET utf8mb4 NOT NULL,
    `phone`         varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL,
    `email`         varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL,
    `lastLoginTime` datetime NULL DEFAULT NULL,
    `role`          varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '角色',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
    COMMENT = '用户信息表'
  ROW_FORMAT = Dynamic;

INSERT INTO `admin`
VALUES (1, '崔东旭', '123456', '1234567801', 'cuidongxu2020@outlook.com', '2024-08-23 19:13:19', 'role_admin');
INSERT INTO `admin`
VALUES (2, 'user2', 'password2', '1234567802', 'user2@example.com', '2024-08-12 09:53:05', 'role_admin');
INSERT INTO `admin`
VALUES (3, 'user3', 'password3', '1234567803', 'user3@example.com', '2024-08-12 09:55:15', 'role_admin');
INSERT INTO `admin`
VALUES (4, 'teacher1', 'password4', '1234567804', 'user4@example.com', '2024-08-22 09:33:19', 'role_teacher');
INSERT INTO `admin`
VALUES (5, 'teacher2', 'password5', '1234567805', 'user5@example.com', '2024-08-22 11:37:21', 'role_teacher');
INSERT INTO `admin`
VALUES (6, 'teacher3', 'password6', '1234567806', 'user6@example.com', '2024-01-06 15:00:00', 'role_teacher');
INSERT INTO `admin`
VALUES (7, 'user7', 'password7', '1234567807', 'user7@example.com', '2024-01-07 16:00:00', 'role_student');
INSERT INTO `admin`
VALUES (8, 'user8', 'password8', '1234567808', 'user8@example.com', '2024-01-08 17:00:00', 'role_student');
INSERT INTO `admin`
VALUES (9, 'user9', 'password9', '1234567809', 'user9@example.com', '2024-01-09 18:00:00', 'role_student');
INSERT INTO `admin`
VALUES (10, 'user10', 'password10', '1234567810', 'user10@example.com', '2024-08-20 09:43:45', 'role_student');

DROP TABLE IF EXISTS `video`;
CREATE TABLE video
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键自增 ID',
    uploader_id      BIGINT       NOT NULL COMMENT '上传用户的 ID',
    upload_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    video_guid       VARCHAR(255) NOT NULL COMMENT '用户上传时的视频标识符',
    video_name       VARCHAR(255) NOT NULL COMMENT '视频文件名',
    file_size        BIGINT       NOT NULL COMMENT '文件大小（单位：字节）',
    description_info TEXT COMMENT '处理结果的元数据，如帧率、时长、分辨率等',
    finish_time   DATETIME COMMENT '处理完成时间',
    status           ENUM('pending', 'processing', 'completed', 'failed') NOT NULL DEFAULT 'pending' COMMENT '处理状态',
    remarks          TEXT COMMENT '备注'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频处理记录表';


SET
FOREIGN_KEY_CHECKS = 1;
