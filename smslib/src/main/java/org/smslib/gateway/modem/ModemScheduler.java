
package org.smslib.gateway.modem;

import org.ajwcc.pduUtils.gsm3040.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;
import org.smslib.core.Settings;
import org.smslib.gateway.AbstractGateway.Status;
import org.smslib.gateway.modem.DeviceInformation.Modes;
import org.smslib.helper.Common;
import org.smslib.message.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class ModemScheduler extends Thread
{
	static Logger logger = LoggerFactory.getLogger(ModemScheduler.class);

	Modem modem;

	List<ModemSchedulerTask> scheduledTasks = new ArrayList<>();

	boolean shouldCancel = false;

	public Object _LOCK_ = new Object();

	public ModemScheduler(Modem modem)
	{
		this.modem = modem;
	}

	public void cancel()
	{
		logger.debug("Cancelling!");
		this.shouldCancel = true;
	}

	public void addTask(ModemSchedulerTask task){
		synchronized (this._LOCK_)
		{
			scheduledTasks.add(task);
		}
	}

	@Override
	public void run()
	{
		logger.debug("Started!");
		while (!this.shouldCancel)
		{
			if (this.modem.getStatus() == Status.Started)
			{
				try
				{
					synchronized (this.modem.getModemDriver()._LOCK_)
					{
						synchronized (this._LOCK_)
						{
							for (ModemSchedulerTask t : scheduledTasks){
								t.tryExecute();
							}
						}
					}
				}
				catch (Exception e)
				{
					logger.error("Exception running modem scheduled task", e);
					//this.cancel();
					modem.error();
				}
			}
			if (!this.shouldCancel)
			{
				Common.countSheeps(5000);
			}
		}
		logger.debug("Stopped!");
	}
}
