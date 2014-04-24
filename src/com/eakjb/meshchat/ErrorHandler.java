package com.eakjb.meshchat;

public class ErrorHandler {
	private final Throwable err;
	private HandlerMode mode;
	
	public ErrorHandler(Throwable err) {
		this(err,HandlerMode.DEFAULT);
	}
	public ErrorHandler(Throwable err, HandlerMode mode) {
		this.err=err;
	}
	
	public static void handle(Throwable err, HandlerMode mode) {
		new ErrorHandler(err,mode).handle();
	}
	public static void handle(Throwable err) {
		handle(err,HandlerMode.DEFAULT);
	}
	
	public void handle() {
		err.printStackTrace();
		if (mode.equals(HandlerMode.CRITICAL)) {
			System.err.println("CRITICAL ERROR!");
			System.exit(-1);
		}
	}
	
	public enum HandlerMode {
		DEFAULT,
		NONCRITICAL,
		CRITICAL,
	}
	
	public Throwable getErr() {
		return err;
	}
	public HandlerMode getMode() {
		return mode;
	}
	public void setMode(HandlerMode mode) {
		this.mode = mode;
	}
}
