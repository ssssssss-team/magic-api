SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `magic_group`  (
 `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
 `group_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组名',
 `group_type` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组类型，1：接口分组，2：函数分组',
 `group_path` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组路径',
 `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父级ID',
 `deleted` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '是否被删除，1：是，0：否',
 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'MagicAPI分组信息表' ROW_FORMAT = Dynamic;

CREATE TABLE `magic_api_info`  (
 `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
 `api_method` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法',
 `api_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求路径',
 `api_script` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '接口脚本',
 `api_parameter` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '接口参数',
 `api_option` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '接口选项',
 `api_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口名称',
 `api_group_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组ID',
 `api_request_body` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求体',
 `api_request_header` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求Header',
 `api_response_body` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '输出结果',
 `api_response_header` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '输出Header',
 `api_description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口描述',
 `api_create_time` bigint(20) NULL DEFAULT NULL COMMENT '创建时间',
 `api_update_time` bigint(20) NULL DEFAULT NULL COMMENT '修改时间',
 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'MagicAPI接口信息' ROW_FORMAT = Dynamic;

CREATE TABLE `magic_api_info_his`  (
 `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api_id',
 `api_method` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法',
 `api_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求路径',
 `api_script` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '接口脚本',
 `api_parameter` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '接口参数',
 `api_option` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '接口选项',
 `api_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口名称',
 `api_group_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组ID',
 `api_request_body` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求体',
 `api_request_header` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求Header',
 `api_response_body` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '输出结果',
 `api_response_header` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '输出Header',
 `api_description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口描述',
 `api_create_time` bigint(20) NULL DEFAULT NULL COMMENT '创建时间',
 `api_update_time` bigint(20) NULL DEFAULT NULL COMMENT '修改时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'MagicAPI接口历史记录' ROW_FORMAT = Dynamic;

CREATE TABLE `magic_function`
(
    `id`                   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '主键',
    `function_name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '函数名称',
    `function_path`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '函数路径',
    `function_parameter`   mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL COMMENT '参数列表',
    `function_return_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '返回值类型',
    `function_script`      mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL COMMENT '脚本',
    `function_group_id`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '所属分组',
    `function_description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '函数描述',
    `function_create_time` bigint(20)                                                    NULL DEFAULT NULL COMMENT '创建时间',
    `function_update_time` bigint(20)                                                    NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = 'MagicAPI 函数表'
  ROW_FORMAT = Dynamic;

CREATE TABLE `magic_function_his`
(
    `id`                   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'function_id',
    `function_name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '函数名称',
    `function_path`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '函数路径',
    `function_parameter`   mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL COMMENT '参数列表',
    `function_return_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '返回值类型',
    `function_script`      mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL COMMENT '脚本',
    `function_group_id`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '所属分组',
    `function_description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '函数描述',
    `function_create_time` bigint(20)                                                    NULL DEFAULT NULL COMMENT '创建时间',
    `function_update_time` bigint(20)                                                    NULL DEFAULT NULL COMMENT '修改时间'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = 'MagicAPI 函数历史记录'
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;