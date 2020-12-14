CREATE TABLE magic_group  (
id varchar(32) ,
group_name varchar2(64) ,
group_type char(1) ,
group_path varchar2(64) ,
parent_id varchar2(32)  ,
deleted char(1)
);
COMMENT ON COLUMN magic_group.group_name IS '分组名称';
COMMENT ON COLUMN magic_group.group_type IS '组类型，1：接口分组，2：函数分组';
COMMENT ON COLUMN magic_group.group_path IS '分组路径';
COMMENT ON COLUMN magic_group.parent_id IS '父级ID';
COMMENT ON COLUMN magic_group.deleted IS '是否被删除，1：是，0：否';
COMMENT ON TABLE magic_group IS 'MagicAPI分组信息表';

CREATE TABLE magic_api_info (
    id varchar2(32) NOT NULL ,
    api_method varchar2(12) ,
    api_path varchar2(512) ,
    api_script clob ,
    api_parameter clob ,
    api_option clob ,
    api_name varchar2(255) ,
    api_group_id varchar2(32) ,
    api_request_body clob ,
    api_request_header clob ,
    api_response_body clob ,
    api_response_header clob ,
    api_description varchar2(512) ,
    api_create_time number(20) ,
    api_update_time number(20)
);
COMMENT ON COLUMN magic_api_info.api_method IS '请求方法';
COMMENT ON COLUMN magic_api_info.api_path IS '请求路径';
COMMENT ON COLUMN magic_api_info.api_script IS '接口脚本';
COMMENT ON COLUMN magic_api_info.api_parameter IS '接口参数';
COMMENT ON COLUMN magic_api_info.api_option IS '接口选项';
COMMENT ON COLUMN magic_api_info.api_name IS '接口名称';
COMMENT ON COLUMN magic_api_info.api_group_id IS '分组ID';
COMMENT ON COLUMN magic_api_info.api_request_body IS '请求体';
COMMENT ON COLUMN magic_api_info.api_request_header IS '请求Header';
COMMENT ON COLUMN magic_api_info.api_response_body IS '输出结果';
COMMENT ON COLUMN magic_api_info.api_response_header IS '输出Header';
COMMENT ON COLUMN magic_api_info.api_description IS '接口描述';
COMMENT ON COLUMN magic_api_info.api_create_time IS '创建时间';
COMMENT ON COLUMN magic_api_info.api_update_time IS '修改时间';
COMMENT ON TABLE magic_api_info IS 'MagicAPI接口信息';

CREATE TABLE magic_api_info_his (
    id varchar2(32) NOT NULL ,
    api_method varchar2(12) ,
    api_path varchar2(512) ,
    api_script clob ,
    api_parameter clob ,
    api_option clob ,
    api_name varchar2(255) ,
    api_group_id varchar2(32) ,
    api_request_body clob ,
    api_request_header clob ,
    api_response_body clob ,
    api_response_header clob ,
    api_description varchar2(512) ,
    api_create_time number(20) ,
    api_update_time number(20)
);
COMMENT ON COLUMN magic_api_info_his.api_method IS '请求方法';
COMMENT ON COLUMN magic_api_info_his.api_path IS '请求路径';
COMMENT ON COLUMN magic_api_info_his.api_script IS '接口脚本';
COMMENT ON COLUMN magic_api_info_his.api_parameter IS '接口参数';
COMMENT ON COLUMN magic_api_info_his.api_option IS '接口选项';
COMMENT ON COLUMN magic_api_info_his.api_name IS '接口名称';
COMMENT ON COLUMN magic_api_info_his.api_group_id IS '分组ID';
COMMENT ON COLUMN magic_api_info_his.api_request_body IS '请求体';
COMMENT ON COLUMN magic_api_info_his.api_request_header IS '请求Header';
COMMENT ON COLUMN magic_api_info_his.api_response_body IS '输出结果';
COMMENT ON COLUMN magic_api_info_his.api_response_header IS '输出Header';
COMMENT ON COLUMN magic_api_info_his.api_description IS '接口描述';
COMMENT ON COLUMN magic_api_info_his.api_create_time IS '创建时间';
COMMENT ON COLUMN magic_api_info_his.api_update_time IS '修改时间';
COMMENT ON TABLE magic_api_info IS 'MagicAPI接口历史记录';