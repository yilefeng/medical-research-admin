-- 数据库创建
CREATE DATABASE IF NOT EXISTS medical_research DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medical_research;

-- 1. 科研数据表
CREATE TABLE `research_data` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `experiment_no` VARCHAR(50) NOT NULL COMMENT '实验编号',
                                 `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
                                 `dataset` VARCHAR(100) COMMENT '数据集名称',
                                 `accuracy` DECIMAL(5,4) COMMENT '准确率',
                                 `precision` DECIMAL(5,4) COMMENT '精确率',
                                 `recall` DECIMAL(5,4) COMMENT '召回率',
                                 `f1_score` DECIMAL(5,4) COMMENT 'F1分数',
                                 `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 INDEX `idx_experiment_no` (`experiment_no`) COMMENT '实验编号索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科研数据表';

-- 2. 数据源表
CREATE TABLE `data_source` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `source_name` VARCHAR(100) NOT NULL COMMENT '数据源名称',
                               `source_type` VARCHAR(50) COMMENT '数据源类型（Excel/数据库/接口）',
                               `file_path` VARCHAR(255) COMMENT '文件路径（Excel）',
                               `db_url` VARCHAR(255) COMMENT '数据库连接地址',
                               `db_username` VARCHAR(50) COMMENT '数据库用户名',
                               `db_password` VARCHAR(50) COMMENT '数据库密码',
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源表';

-- 3. 实验方案表
CREATE TABLE `experiment_plan` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `plan_name` VARCHAR(100) NOT NULL COMMENT '实验方案名称',
                                   `experiment_no` VARCHAR(50) NOT NULL COMMENT '关联实验编号（关联research_data表）',
                                   `purpose` TEXT COMMENT '实验目的',
                                   `principal` VARCHAR(50) NOT NULL COMMENT '实验负责人',
                                   `dept` VARCHAR(50) COMMENT '所属科室',
                                   `start_time` DATETIME COMMENT '实验开始时间',
                                   `end_time` DATETIME COMMENT '实验结束时间',
                                   `status` TINYINT DEFAULT 0 COMMENT '状态（0：未开始，1：进行中，2：已完成，3：已终止）',
                                   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `user_id` BIGINT NOT NULL COMMENT '创建人ID（关联sys_user表）',
                                   PRIMARY KEY (`id`),
                                   INDEX `idx_experiment_no` (`experiment_no`) COMMENT '实验编号索引',
                                   INDEX `idx_user_id` (`user_id`) COMMENT '用户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验方案表';

-- 4. 统计模型表
CREATE TABLE `stat_model` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `model_name` VARCHAR(50) NOT NULL COMMENT '模型名称（如t检验、卡方检验）',
                              `model_code` VARCHAR(30) NOT NULL COMMENT '模型编码（唯一，如t_test、chi_square）',
                              `description` TEXT COMMENT '模型描述',
                              `params` JSON COMMENT '模型参数配置（JSON格式）',
                              `status` TINYINT DEFAULT 1 COMMENT '状态（1：可用，0：不可用）',
                              `sort` INT DEFAULT 0 COMMENT '排序权重',
                              `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_model_code` (`model_code`) COMMENT '模型编码唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统计模型表';

-- 5. 分析报告表
CREATE TABLE `analysis_report` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `report_name` VARCHAR(100) NOT NULL COMMENT '报告名称',
                                   `plan_id` BIGINT COMMENT '关联实验方案ID（关联experiment_plan表）',
                                   `model_id` BIGINT NOT NULL COMMENT '关联统计模型ID（关联stat_model表）',
                                   `stat_conditions` JSON COMMENT '统计筛选条件（JSON格式）',
                                   `report_content` LONGTEXT COMMENT '报告内容（HTML/Markdown）',
                                   `file_url` VARCHAR(255) COMMENT '报告文件存储路径（PDF/Word）',
                                   `version` VARCHAR(20) DEFAULT '1.0' COMMENT '报告版本',
                                   `status` TINYINT DEFAULT 1 COMMENT '状态（1：已生成，2：已导出，3：已作废）',
                                   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `user_id` BIGINT NOT NULL COMMENT '创建人ID（关联sys_user表）',
                                   PRIMARY KEY (`id`),
                                   INDEX `idx_plan_id` (`plan_id`) COMMENT '实验方案ID索引',
                                   INDEX `idx_model_id` (`model_id`) COMMENT '统计模型ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析报告表';

-- 6. 系统用户表
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

-- 7. 系统角色表
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

-- 8. 用户角色关联表
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

-- 9. 系统操作日志表
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

-- 初始化基础数据
INSERT INTO stat_model (model_name, model_code, description, params, sort) VALUES
                                                                               ('t检验', 't_test', '用于两组数据的均值比较，适配医疗AI模型性能对比', '{"confidence":0.95,"type":"paired"}', 1),
                                                                               ('卡方检验', 'chi_square', '用于分类数据的独立性检验，适配病灶类型分布分析', '{"df":1,"alpha":0.05}', 2),
                                                                               ('线性回归', 'linear_reg', '用于分析模型准确率与训练时长的线性关系', '{"fit_intercept":true}', 3),
                                                                               ('方差分析', 'anova', '用于多组模型性能数据的差异检验', '{"factor":"model_type","alpha":0.05}', 4);

INSERT INTO sys_user (username, password, real_name, phone, email, status) VALUES
                                                                               ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800138000', 'admin@research.com', 1),
                                                                               ('researcher01', 'e10adc3949ba59abbe56e057f20f883e', '科研人员01', '13900139000', 'researcher01@research.com', 1);

INSERT INTO sys_role (role_name, role_code, description) VALUES
                                                             ('管理员', 'admin', '拥有平台所有操作权限，可管理用户、角色、实验方案等'),
                                                             ('科研人员', 'researcher', '仅可操作本人创建的实验方案、数据及报告');

INSERT INTO sys_user_role (user_id, role_id) VALUES
                                                 (1, 1),
                                                 (2, 2);

-- 初始化测试实验方案
INSERT INTO experiment_plan (plan_name, experiment_no, purpose, principal, dept, start_time, end_time, status, user_id) VALUES
                                                                                                                            ('肺癌AI诊断模型性能测试', 'EXP2025001', '验证不同CNN模型在肺癌病灶识别中的准确率', '张医生', '放射科', '2025-01-01 09:00:00', '2025-02-01 18:00:00', 1, 2),
                                                                                                                            ('心血管疾病预测模型对比', 'EXP2025002', '对比LSTM和GRU模型在心血管疾病预测中的F1分数', '李医生', '心内科', '2025-01-10 09:00:00', '2025-03-10 18:00:00', 0, 2);

-- 初始化测试科研数据
INSERT INTO research_data (experiment_no, model_name, dataset, accuracy, `precision`, recall, f1_score) VALUES
                                                                                                            ('EXP2025001', 'ResNet50', 'LungCT-2024', 0.9250, 0.9100, 0.9300, 0.9200),
                                                                                                            ('EXP2025001', 'VGG16', 'LungCT-2024', 0.8900, 0.8800, 0.8700, 0.8750),
                                                                                                            ('EXP2025002', 'LSTM', 'CardioData-2024', 0.8850, 0.8700, 0.8900, 0.8800),
                                                                                                            ('EXP2025002', 'GRU', 'CardioData-2024', 0.9000, 0.8950, 0.9050, 0.9000);

-- 初始化测试分析报告
INSERT INTO analysis_report (report_name, plan_id, model_id, stat_conditions, report_content, version, status, user_id) VALUES
    ('肺癌模型t检验分析报告', 1, 1, '{"experimentNo":"EXP2025001","models":["ResNet50","VGG16"]}', '<h3>t检验分析结果</h3><p>ResNet50与VGG16模型在LungCT-2024数据集上的准确率存在显著差异（p=0.02<0.05）</p>', '1.0', 1, 2);