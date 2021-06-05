
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ModemScheduler extends Thread
{
	static Logger logger = LoggerFactory.getLogger(ModemScheduler.class);

	Modem modem;

	List<ModemSchedulerTask> scheduledTasks = new ArrayList<>();

	boolean shouldCancel = false;

    Lock lock = new ReentrantLock();

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
		this.lock.lock();
		try{
			scheduledTasks.add(task);
		} finally {
			this.lock.unlock();
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
					this.modem.getModemDriver().lock.lock();
					try{
						this.lock.lock();
						try{
							for (ModemSchedulerTask t : scheduledTasks){
								t.tryExecute();
							}
						} finally {
							this.lock.unlock();
						}
					} finally {
						this.modem.getModemDriver().lock.unlock();
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
