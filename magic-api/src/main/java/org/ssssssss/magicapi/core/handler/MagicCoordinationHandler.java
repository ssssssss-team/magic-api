package org.ssssssss.magicapi.core.handler;

import org.ssssssss.magicapi.core.annotation.Message;
import org.ssssssss.magicapi.core.config.MessageType;
import org.ssssssss.magicapi.core.config.WebSocketSessionManager;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.context.MagicConsoleSession;

public class MagicCoordinationHandler {

	@Message(MessageType.SET_FILE_ID)
	public void setFileId(MagicConsoleSession session, String fileId) {
		session.setAttribute(Constants.WEBSOCKET_ATTRIBUTE_FILE_ID, fileId);
		WebSocketSessionManager.sendToOther(session.getClientId(), MessageType.INTO_FILE_ID, session.getAttribute(Constants.WEBSOCKET_ATTRIBUTE_CLIENT_ID), fileId);
	}
}
