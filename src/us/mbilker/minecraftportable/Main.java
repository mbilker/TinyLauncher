package us.mbilker.minecraftportable;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

public class Main {
	
	private static Logger logger = Logger.getLogger("MinecraftPortable");
	
	private static final int MIN_HEAP = 512;
	private static final int RECOMMENDED_HEAP = 1024;
	
	public static void log(String formatString, Object... params) {
		logger.info(String.format(formatString, params));
	}
	
	public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            public void print(final String string) {
                logger.info(string);
            }
            public void println(final String string) {
                logger.info(string);
            }
        };
    }
	
	public static void main(String[] args) {
		boolean doLaunch = false;
		
		float f = (Runtime.getRuntime().maxMemory() / 1024L / 1024L);
		if (f > 511.0F)
			doLaunch = true;
		else {
			doLaunch = false;
			try {
				String str = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				List<String> localArrayList = new ArrayList<String>();
				if (Util.getPlatform().equals(Util.OS.WINDOWS))
					localArrayList.add("javaw");
				else {
					localArrayList.add("java");
				}
				localArrayList.add("-Xms" + MIN_HEAP + "M");
				localArrayList.add("-Xmx" + RECOMMENDED_HEAP + "M");
				localArrayList.add("-Dsun.java2d.noddraw=true");
				localArrayList.add("-Dsun.java2d.d3d=false");
				localArrayList.add("-Dsun.java2d.opengl=false");
				localArrayList.add("-Dsun.java2d.pmoffscreen=false");
				localArrayList.add("-cp");
				localArrayList.add(str);
				localArrayList.add(Main.class.getName());
				localArrayList.addAll(Arrays.asList(args));
				Logger.getLogger("MinecraftPortable").warn("Restarting minecraft with necessary RAM and options " + localArrayList);
				ProcessBuilder localProcessBuilder = new ProcessBuilder(localArrayList);
				Process localProcess = localProcessBuilder.start();
				if (localProcess == null) throw new Exception("!");
				else {
					try {
		                int d;
		                while ((d = localProcess.getInputStream().read()) != -1) {
		                    System.out.write(d);
		                }
		            } catch (IOException ex) {
		            }

					localProcess.waitFor();
				}
			} catch (Exception e) {
				e.printStackTrace();
		        new MinecraftPortable(args);
			}
		}
		
		if (doLaunch == true) {
			System.setOut(createLoggingProxy(System.out));
			System.setErr(createLoggingProxy(System.err));
			new MinecraftPortable(args);
		}
	}

}
