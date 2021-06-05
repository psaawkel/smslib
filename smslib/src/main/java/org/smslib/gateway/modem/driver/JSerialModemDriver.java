
package org.smslib.gateway.modem.driver;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.gateway.modem.Modem;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class JSerialModemDriver extends AbstractModemDriver
{
	@SuppressWarnings("hiding")
	static Logger logger = LoggerFactory.getLogger(JSerialModemDriver.class);

	String portName;

	int baudRate;

	SerialPort serialPort;

	public JSerialModemDriver(Modem modem, String port, int baudRate)
	{
		super(modem);
		this.portName = port;
		this.baudRate = baudRate;
	}

	@Override
	public void openPort() throws NumberFormatException, IOException
	{
		Optional<SerialPort> o = Arrays.asList(SerialPort.getCommPorts()).stream().filter(p->portName.equals(p.getSystemPortName())).findFirst();

		if(o.isPresent()){
			serialPort = o.get();
		}else{
			throw new IOException("Port not found on system");
		}
		serialPort.setBaudRate(baudRate);
		serialPort.openPort();
		this.in = serialPort.getInputStream();
		this.out = serialPort.getOutputStream();
		this.pollReader = new PollReader();
		this.pollReader.start();
	}

	@Override
	public void closePort() throws IOException, InterruptedException
	{
		try {
			logger.debug("Closing comm port: " + getPortInfo());
			if(this.pollReader!=null){
				this.pollReader.cancel();
				try {
					this.pollReader.join();
				} catch (InterruptedException ex) {
					logger.error("PollReader closing exception: {}");
				}
			}
			if(in!=null){
				this.in.close();
				this.in = null;
			}
			if(out!=null){
				this.out.close();
				this.out = null;
			}
			if(serialPort!=null){
				this.serialPort.closePort();
				this.serialPort = null;
			}
		} catch (Exception e) {
			logger.error("Closing port exception:\n{}",e);
		}
	}

	@Override
	public String getPortInfo()
	{
		return this.portName + ":" + this.baudRate;
	}

}
