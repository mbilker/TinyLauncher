package us.mbilker.tinylauncher.util;

import java.io.File;

import us.mbilker.tinylauncher.TinyLauncher;

public class Util {
  private static File workDir = null;

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
}