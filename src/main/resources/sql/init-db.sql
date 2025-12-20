-- 数据库创建
CREATE DATABASE IF NOT EXISTS medical_research DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medical_research;

-- 1. 实验方案表
CREATE TABLE `experiment_plan` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '实验ID',
                                   `plan_name` VARCHAR(255) NOT NULL COMMENT '实验名称',
                                   `research_purpose` TEXT COMMENT '研究目的',
                                   `model_info` VARCHAR(500) COMMENT '模型信息（如：模型1=CNN，模型2=Transformer）',
                                   `creator` VARCHAR(50) DEFAULT 'admin' COMMENT '创建人',
                                   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗科研实验方案表';

-- 2. 科研数据表（关联实验方案）
CREATE TABLE `research_data` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据ID',
                                 `experiment_id` BIGINT NOT NULL COMMENT '关联实验ID',
                                 `true_label` TINYINT NOT NULL COMMENT '真实标签：1=阳性，0=阴性',
                                 `model1_score` DOUBLE NOT NULL COMMENT '模型1预测评分',
                                 `model2_score` DOUBLE NOT NULL COMMENT '模型2预测评分',
                                 `data_source` VARCHAR(255) DEFAULT 'CSV上传' COMMENT '数据来源',
                                 `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_experiment_id` (`experiment_id`),
                                 CONSTRAINT `fk_experiment_data` FOREIGN KEY (`experiment_id`) REFERENCES `experiment_plan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗科研原始数据表';

-- 3. 分析报告表（关联实验+数据）
CREATE TABLE `analysis_report` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报告ID',
                                   `experiment_id` BIGINT NOT NULL COMMENT '关联实验ID',
                                   `data_ids` VARCHAR(500) NOT NULL COMMENT '关联数据ID（多个用逗号分隔）',
                                   `test_method` VARCHAR(50) NOT NULL COMMENT '检验方法（DeLong/AUC)',
                                   `auc1` DECIMAL(10,4) COMMENT '模型1 AUC',
                                   `auc2` DECIMAL(10,4) COMMENT '模型2 AUC',
                                   `auc_diff` DECIMAL(10,4) COMMENT 'AUC差异',
                                   `std_err` DECIMAL(10,4) COMMENT '标准误',
                                   `z_value` DECIMAL(10,4) COMMENT 'Z值',
                                   `p_value` DECIMAL(10,4) COMMENT '双侧P值',
                                   `report_name` VARCHAR(255) NOT NULL COMMENT '报告名称',
                                   `pdf_path` VARCHAR(500) COMMENT 'PDF报告存储路径',
                                   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_experiment_id` (`experiment_id`),
                                   CONSTRAINT `fk_experiment_report` FOREIGN KEY (`experiment_id`) REFERENCES `experiment_plan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗科研分析报告表';

-- 4. 系统用户表
CREATE TABLE `sys_user` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
                            `password` VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
                            `real_name` VARCHAR(50) COMMENT '真实姓名',
                            `phone` VARCHAR(20) COMMENT '手机号',
                            `email` VARCHAR(50) COMMENT '邮箱',
                            `status` TINYINT DEFAULT 1 COMMENT '状态（1：启用，0：禁用）',
                            `last_login_time` DATETIME COMMENT '最后登录时间',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`) COMMENT '用户名唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 5. 系统角色表
CREATE TABLE `sys_role` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称（如管理员、科研人员）',
                            `role_code` VARCHAR(30) NOT NULL COMMENT '角色编码（唯一，如admin、researcher）',
                            `description` VARCHAR(255) COMMENT '角色描述',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_role_code` (`role_code`) COMMENT '角色编码唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 6. 用户角色关联表
CREATE TABLE `sys_user_role` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` BIGINT NOT NULL COMMENT '用户ID（关联sys_user.id）',
                                 `role_id` BIGINT NOT NULL COMMENT '角色ID（关联sys_role.id）',
                                 `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_user_role` (`user_id`,`role_id`) COMMENT '用户-角色组合唯一',
                                 INDEX `idx_user_id` (`user_id`) COMMENT '用户ID索引',
                                 INDEX `idx_role_id` (`role_id`) COMMENT '角色ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 7. 系统操作日志表
CREATE TABLE `sys_oper_log` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
                                `username` VARCHAR(50) NOT NULL COMMENT '操作用户名',
                                `oper_module` VARCHAR(50) COMMENT '操作模块（如实验方案、科研数据）',
                                `oper_type` VARCHAR(20) COMMENT '操作类型（新增/修改/删除/导入/导出）',
                                `oper_content` TEXT COMMENT '操作内容',
                                `oper_ip` VARCHAR(50) COMMENT '操作IP',
                                `oper_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                PRIMARY KEY (`id`),
                                INDEX `idx_user_id` (`user_id`),
                                INDEX `idx_oper_module` (`oper_module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-
INSERT INTO sys_user (username, password, real_name, phone, email, status) VALUES
                                                                               ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800138000', 'admin@research.com', 1),
                                                                               ('researcher01', 'e10adc3949ba59abbe56e057f20f883e', '科研人员01', '13900139000', 'researcher01@research.com', 1);

INSERT INTO sys_role (role_name, role_code, description) VALUES
                                                             ('管理员', 'admin', '拥有平台所有操作权限，可管理用户、角色、实验方案等'),
                                                             ('科研人员', 'researcher', '仅可操作本人创建的实验方案、数据及报告');

INSERT INTO sys_user_role (user_id, role_id) VALUES
                                                 (1, 1),
                                                 (2, 2);