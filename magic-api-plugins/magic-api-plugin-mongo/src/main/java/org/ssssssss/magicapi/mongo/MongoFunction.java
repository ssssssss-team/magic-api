package org.ssssssss.magicapi.mongo;

import org.bson.types.ObjectId;
import org.ssssssss.magicapi.core.config.MagicFunction;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.annotation.Function;

import java.util.Date;

public class MongoFunction implements MagicFunction {

	@Comment("创建ObjectId")
	@Function
	public ObjectId ObjectId(String hexString){
		return new ObjectId(hexString);
	}

	@Comment("创建ObjectId")
	@Function
	public ObjectId ObjectId(){
		return new ObjectId();
	}

	@Comment("创建ObjectId")
	@Function
	public ObjectId ObjectId(byte[] bytes){
		return new ObjectId(bytes);
	}

	@Comment("创建ObjectId")
	@Function
	public ObjectId ObjectId(Date date){
		return new ObjectId(date);
	}
}
