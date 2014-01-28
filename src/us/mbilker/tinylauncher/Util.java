package us.mbilker.tinylauncher;

import java.io.File;

public class Util {
  private static File workDir = null;

  public static OS getPlatform() {
    String str = System.getProperty("os.name").toLowerCase();
    if (str.contains("win")) return OS.WINDOWS;
    if (str.contains("mac")) return OS.MACOS;
    if (str.contains("solaris")) return OS.SOLARIS;
    if (str.contains("sunos")) return OS.SOLARIS;
    if (str.contains("linux")) return OS.LINUX;
    if (str.contains("unix")) return OS.LINUX;
    return OS.UNKNOWN;
  }

  public static File getWorkingDirectory() {
    if (workDir == null) workDir = getWorkingDirectory("minecraft");
    return workDir;
  }

  public static File getWorkingDirectory(String paramString) {
    File localFile = TinyLauncher.instance.clientDir;
    if ((!localFile.exists()) && (!localFile.mkdirs()))
      throw new RuntimeException("The working directory could not be created: " + localFile);
    return localFile;
  }

  public static boolean isEmpty(String paramString) {
    return (paramString == null) || (paramString.length() == 0);
  }

  public static enum OS {
    LINUX, 
    SOLARIS, 
    WINDOWS, 
    MACOS, 
    UNKNOWN;
  }
}