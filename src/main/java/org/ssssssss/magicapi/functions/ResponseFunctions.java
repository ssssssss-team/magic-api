package org.ssssssss.magicapi.functions;

import org.ssssssss.magicapi.provider.ResultProvider;

import java.util.List;

public class ResponseFunctions {

	private ResultProvider resultProvider;

	public ResponseFunctions(ResultProvider resultProvider) {
		this.resultProvider = resultProvider;
	}

	/**
	 * 自行构建分页结果
	 * @param total	条数
	 * @param values	数据内容
	 */
	public Object page(long total, List<Object> values){
		return resultProvider.buildPageResult(total,values);
	}
}
