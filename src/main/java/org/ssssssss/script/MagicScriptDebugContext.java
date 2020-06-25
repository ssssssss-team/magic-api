package org.ssssssss.script;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MagicScriptDebugContext extends MagicScriptContext{

	private String id = UUID.randomUUID().toString().replace("-","");

	private static Map<String,MagicScriptDebugContext> contextMap = new ConcurrentHashMap<>();

	public List<Integer> breakpoints;

	private BlockingQueue<String> producer = new LinkedBlockingQueue<>();

	private BlockingQueue<String> consumer = new LinkedBlockingQueue<>();

	private Object returnValue;

	private boolean running = true;

	public MagicScriptDebugContext(Map<String, Object> variables) {
		super(variables);
		contextMap.put(this.id,this);
	}

	public List<Integer> getBreakpoints() {
		return breakpoints;
	}

	public void setBreakpoints(List<Integer> breakpoints) {
		this.breakpoints = breakpoints;
	}

	public void pause() throws InterruptedException {
		consumer.offer(this.id);
		producer.take();
	}

	public  void await() throws InterruptedException {
		consumer.take();
	}
	public void singal() throws InterruptedException {
		producer.offer(this.id);
		await();
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Object returnValue) {
		this.running = false;
		this.returnValue = returnValue;
		contextMap.remove(this.id);
		consumer.offer(this.id);
	}

	public boolean isRunning() {
		return running;
	}

	public List<Map<String,Object>> getDebugInfo(){
		Map<String, Object> variables = super.getVariables();
		List<Map<String,Object>> result = new ArrayList<>();
		Set<Map.Entry<String, Object>> entries = variables.entrySet();
		for (Map.Entry<String, Object> entry : entries) {
			Object value = entry.getValue();
			Map<String,Object> variable = new HashMap<>();
			variable.put("name",entry.getKey());
			variable.put("value",value);
			if(value != null){
				variable.put("type",value.getClass());
			}
			result.add(variable);
		}
		return result;
	}

	public String getId() {
		return id;
	}

	public static MagicScriptDebugContext getDebugContext(String id){
		return contextMap.get(id);
	}
}
