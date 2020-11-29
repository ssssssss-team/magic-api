-- 创建分组表
CREATE TABLE `magic_group`  (
    `id` varchar(32) NOT NULL,
    `group_name` varchar(64) NULL COMMENT '组名',
    `group_type` varchar(1) NULL COMMENT '组类型，1：接口分组，2：函数分组',
    `group_path` varchar(64) NULL COMMENT '分组路径',
    `parent_id` varchar(32) NULL COMMENT '父级ID',
    `deleted` char(1) NULL DEFAULT 0 COMMENT '是否被删除，1：是，0：否',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'MagicAPI分组信息表' ROW_FORMAT = Dynamic;
-- 插入分组数据
insert into magic_group select md5(uuid()),api_group_name,'1',api_group_prefix,'0','0' from magic_api_info group by api_group_name,api_group_prefix;
-- 修改字段
ALTER TABLE `magic_api_info` ADD COLUMN `api_group_id` varchar(32) NULL COMMENT '分组ID' AFTER `api_name`;
ALTER TABLE `magic_api_info` CHANGE COLUMN `api_output` `api_response_body` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '输出结果' AFTER `api_group_id`, ADD COLUMN `api_response_header` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '输出Header' AFTER `api_response_body`;
ALTER TABLE `magic_api_info_his` ADD COLUMN `api_group_id` varchar(32) NULL COMMENT '分组ID' AFTER `api_name`;
ALTER TABLE `magic_api_info_his` CHANGE COLUMN `api_output` `api_response_body` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '输出结果' AFTER `api_group_id`, ADD COLUMN `api_response_header` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '输出Header' AFTER `api_response_body`;

-- 赋值api_group_id字段
UPDATE magic_api_info mai JOIN magic_group mg ON mg.group_name = mai.api_group_name AND mg.group_path = mai.api_group_prefix SET mai.api_group_id = mg.id;
-- 对关联不上的，归根节点
UPDATE magic_api_info SET api_group_id = '0' where api_group_id IS NULL;
-- 删除字段
ALTER TABLE `magic_api_info` DROP COLUMN `api_group_name`,DROP COLUMN `api_group_prefix`;
ALTER TABLE `magic_api_info_his` DROP COLUMN `api_group_name`,DROP COLUMN `api_group_prefix`;

