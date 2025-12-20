-- 创建数据库
CREATE DATABASE IF NOT EXISTS medical_research DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE medical_research;

CREATE TABLE `experiment_plan` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `plan_name` varchar(255) NOT NULL COMMENT '实验方案名称',
                                   `research_purpose` text COMMENT '研究目的',
                                   `model_info` varchar(500) DEFAULT NULL COMMENT '模型信息',
                                   `experiment_desc` text COMMENT '实验描述',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验方案表';

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
                                   `test_method` VARCHAR(50) NOT NULL COMMENT '检验方法（DeLong/AUC/配对T检验）',
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
                            `password` VARCHAR(100) NOT NULL COMMENT '密码（加密存储，默认123456加密后：$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2）',
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

-- 初始化数据
-- 初始化角色
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
                                                                     ('系统管理员', 'admin', '拥有系统所有操作权限'),
                                                                     ('科研人员', 'researcher', '拥有实验创建、数据上传、分析查看等权限');

-- 初始化管理员用户（密码：123456，BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`) VALUES
    ('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '13800138000', 'admin@medical.com');

-- 初始化用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 初始化测试实验
INSERT INTO `experiment_plan` (`plan_name`, `research_purpose`, `model_info`) VALUES
    ('肺癌CT影像良恶性诊断模型AUC比较实验', '对比传统CNN模型与Transformer模型在肺癌CT影像良恶性诊断中的性能差异，通过DeLong检验验证AUC是否存在统计学显著差异', '模型1=传统CNN模型（基于ResNet50），模型2=Transformer模型（基于ViT）');