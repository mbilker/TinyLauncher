package us.mbilker.tinylauncher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.mbilker.tinylauncher.util.OS;

import com.beust.jcommander.JCommander;

public class Main {
	private static final int MIN_HEAP = 1024;
	private static final int RECOMMENDED_HEAP = 2048;
	
	public static void main(String[] args) {
		CommandOptions params = new CommandOptions();
		JCommander cmd = new JCommander(params, args);
		
		float f = (Runtime.getRuntime().maxMemory() / 1024L / 1024L);
		if (f < 512.0F) {
			if (params.help) {
				cmd.usage();
				return;
			}
			
			try {
				String str = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				List<String> localArrayList = new ArrayList<String>();
				if (OS.getCurrentPlatform().equals(OS.WINDOWS)) {
					localArrayList.add("javaw");
				} else {
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
				System.out.println("Restarting minecraft with necessary RAM and options " + localArrayList);
				ProcessBuilder localProcessBuilder = new ProcessBuilder(localArrayList);
				Process localProcess = localProcessBuilder.start();
				if (localProcess == null) throw new Exception("!");
				else {
					try {
						InputStream input = localProcess.getInputStream();
		                int d;
		                while ((d = input.read()) != -1) {
		                    System.out.write(d);
		                }
		            } catch (IOException ex) {
		            }

					localProcess.waitFor();
				}
			} catch (Exception e) {
				e.printStackTrace();
		        new TinyLauncher(args, cmd, params).launchMinecraft();
			}
		} else {
			new TinyLauncher(args, cmd, params).launchMinecraft();
		}
	}

}
