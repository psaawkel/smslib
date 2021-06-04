
package org.smslib.gateway.modem;


public abstract class ModemSchedulerTask
{
	protected long lastExecuted = 0;
	protected long interval = 0;

	protected final Modem modem;

	public ModemSchedulerTask(Modem modem, long interval) {
		this.modem = modem;
		this.interval = interval;
	}

	public void tryExecute() throws Exception {
		long now = System.currentTimeMillis();
		if(lastExecuted+interval<now){
			this.lastExecuted = now;
			execute();
		}
	}

	protected abstract void execute() throws Exception;
}
