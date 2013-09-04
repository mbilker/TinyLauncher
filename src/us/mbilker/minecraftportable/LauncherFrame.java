package us.mbilker.minecraftportable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class LauncherFrame extends Frame
{
  public static final int VERSION = 13;
  private static final long serialVersionUID = 1L;
  public Map<String, String> customParameters = new HashMap<String, String>();
  public Launcher launcher;
  public LoginForm loginForm;

  public LauncherFrame() {
    super("Minecraft Launcher");

    setBackground(Color.BLACK);
    loginForm = new LoginForm(this);
    JPanel localJPanel = new JPanel();
    localJPanel.setLayout(new BorderLayout());
    localJPanel.add(loginForm, "Center");

    localJPanel.setPreferredSize(new Dimension(854, 480));

    setLayout(new BorderLayout());
    add(localJPanel, "Center");

    pack();
    setLocationRelativeTo(null);
    try {
    	setIconImage(ImageIO.read(LauncherFrame.class.getResource("Minecraft.png")));
    } catch (IOException localIOException) {
      localIOException.printStackTrace();
    }

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent paramAnonymousWindowEvent) {
        new Thread() {
          @Override
          public void run() {
            try {
              Thread.sleep(30000L);
            } catch (InterruptedException localInterruptedException) {
              localInterruptedException.printStackTrace();
            }
            System.out.println("FORCING EXIT!");
            System.exit(0);
          }
        }.start();

        if (launcher != null) {
          launcher.stop();
          launcher.destroy();
        }
        System.exit(0);
      }
    });
  }

  public void playCached(String paramString, boolean paramBoolean) {
    try {
      if ((paramString == null) || (paramString.length() <= 0)) {
        paramString = "Player";
      }
      launcher = new Launcher();
      launcher.customParameters.putAll(customParameters);
      launcher.customParameters.put("userName", paramString);
      launcher.customParameters.put("demo", "" + paramBoolean);
      launcher.customParameters.put("sessionId", "1");
      launcher.init();
      removeAll();
      add(launcher, "Center");
      validate();
      launcher.start();
      loginForm = null;
      setTitle("Minecraft");
    } catch (Exception localException) {
      localException.printStackTrace();
      showError(localException.toString());
    }
  }

  public void login(String paramString1, String paramString2) {
    try {
      Map<String, Object> localHashMap = new HashMap<String, Object>();
      localHashMap.put("user", paramString1);
      localHashMap.put("password", paramString2);
      localHashMap.put("version", Integer.valueOf(13));
      String str = Util.executePost("https://login.minecraft.net/", localHashMap);
      if (str == null) {
    	System.out.println(localHashMap);
        showError("Can't connect to minecraft.net");
        loginForm.setNoNetwork(false);
        return;
      }
      if (!str.contains(":")) {
        boolean bool = false;

        if (str.trim().equals("Bad login")) {
          showError("Login failed");
        } else if (str.trim().equals("Old version")) {
          loginForm.setOutdated();
          showError("Outdated launcher");
        } else if (str.trim().equals("User not premium")) {
          showError(str);
          bool = true;
        } else {
          showError(str);
        }
        loginForm.setNoNetwork(bool);
        return;
      }
      String[] arrayOfString = str.split(":");

      launcher = new Launcher();
      launcher.customParameters.putAll(customParameters);
      launcher.customParameters.put("userName", arrayOfString[2].trim());
      launcher.customParameters.put("latestVersion", arrayOfString[0].trim());
      launcher.customParameters.put("downloadTicket", arrayOfString[1].trim());
      launcher.customParameters.put("sessionId", arrayOfString[3].trim());
      launcher.init();

      removeAll();
      add(launcher, "Center");
      validate();
      launcher.start();
      loginForm.loginOk();
      loginForm = null;
      setTitle("Minecraft");
    } catch (Exception localException) {
      localException.printStackTrace();
      showError(localException.toString());
      loginForm.setNoNetwork(false);
    }
  }

  private void showError(String paramString) {
    removeAll();
    add(loginForm);
    loginForm.setError(paramString);
    validate();
  }

  public boolean canPlayOffline(String paramString) {
    Launcher localLauncher = new Launcher();
    localLauncher.customParameters.putAll(customParameters);
    localLauncher.init(paramString, null, null, "1");
    return localLauncher.canPlayOffline();
  }

  public static void main(Map<String, String> args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception localException) {
    }
    System.setProperty("java.net.preferIPv4Stack", "true");
    System.setProperty("java.net.preferIPv6Addresses", "false");

    LauncherFrame localLauncherFrame = new LauncherFrame();
    localLauncherFrame.setVisible(true);
    localLauncherFrame.customParameters.put("stand-alone", "true");
   
    if (args.containsKey("server")) {
    	localLauncherFrame.customParameters.put("server", args.get("server"));
    }
    if (args.containsKey("noupdate")) {
    	localLauncherFrame.customParameters.put("noupdate", args.get("noupdate"));
    }
    if (args.containsKey("port")) {
    	localLauncherFrame.customParameters.put("port", args.get("port"));
    }
    if (args.containsKey("user")) {
    	localLauncherFrame.customParameters.put("username", args.get("user"));
        localLauncherFrame.loginForm.userName.setText(args.get("user"));
    }
    if (args.containsKey("password")) {
    	localLauncherFrame.customParameters.put("password", args.get("password"));
        localLauncherFrame.loginForm.password.setText(args.get("password"));
    }
  }
}