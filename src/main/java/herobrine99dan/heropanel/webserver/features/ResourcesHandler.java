package herobrine99dan.heropanel.webserver.features;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

import com.sun.management.OperatingSystemMXBean;

public class ResourcesHandler {

	private static final Logger LOGGER = Logger.getLogger(ResourcesHandler.class.getName());
	private HeroPanel panel;

	public ResourcesHandler(HeroPanel panel) {
		this.panel = panel;
	}

	public void setup() {
	}

	class CPUUsageResult {
		private final double cpuUsage;
		private final String methodUsed;

		CPUUsageResult(double cpuUsage, String methodUsed) {
			this.cpuUsage = cpuUsage;
			this.methodUsed = methodUsed;
		}

		public double getCpuUsage() {
			return cpuUsage;
		}

		public String getMethodUsed() {
			return methodUsed;
		}
	}

	public CPUUsageResult getCPUUsageMethod() {
		OperatingSystemMXBean operatingSystemObject = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		if (operatingSystemObject.getProcessCpuLoad() > 0) {
			System.out.println((double) operatingSystemObject.getProcessCpuLoad() + "  " + operatingSystemObject.getName());
			return new CPUUsageResult((double) operatingSystemObject.getProcessCpuLoad(), "CPU-Usage-Server");
		} else if (operatingSystemObject.getSystemCpuLoad() > 0) {
			return new CPUUsageResult(operatingSystemObject.getSystemCpuLoad(), "CPU-Usage-System");
		}
		return new CPUUsageResult(-1, "cpuLoad");
	}

	public long getFreeMemory() {
		return Runtime.getRuntime().freeMemory();
	}

	public long getTotalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	public long getMaxMemory() {
		return Runtime.getRuntime().maxMemory();
	}

	public void gc() {
		Runtime.getRuntime().gc();
	}

	public int availableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}

	public String getArch() {
		OperatingSystemMXBean operatingSystemObject = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		return operatingSystemObject.getArch();
	}

	public String getName() {
		OperatingSystemMXBean operatingSystemObject = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		return operatingSystemObject.getName();
	}

	public String getVersion() {
		OperatingSystemMXBean operatingSystemObject = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		return operatingSystemObject.getVersion();
	}

	private static final File rootDirectory = new File("./");

	public long getFreeDiskSpace() {
		return rootDirectory.getFreeSpace();
	}

	public long getTotalDiskSpace() {
		return rootDirectory.getTotalSpace();
	}

	public long getUsableDiskSpace() {
		return rootDirectory.getUsableSpace();
	}

	public long getSwapSpaceSize() {
		OperatingSystemMXBean operatingSystemObject = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		return operatingSystemObject.getTotalSwapSpaceSize();
	}

}
