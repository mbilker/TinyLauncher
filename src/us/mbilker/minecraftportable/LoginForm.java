package us.mbilker.minecraftportable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.MatteBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class LoginForm extends TransparentPanel
{
  //private static final int PANEL_SIZE = 100;
  private static final long serialVersionUID = 1L;
  private static final Color LINK_COLOR = new Color(8421631);

  public JTextField userName = new JTextField(20);
  public JPasswordField password = new JPasswordField(20);
  private JScrollPane scrollPane;
  private TransparentCheckbox rememberBox = new TransparentCheckbox("Remember password");
  private TransparentButton launchButton = new TransparentButton("Login");
  private TransparentButton retryButton = new TransparentButton("Try again");
  private TransparentButton offlineButton = new TransparentButton("Play offline");
  private TransparentLabel errorLabel = new TransparentLabel("", 0);
  private List<String> jarList = new ArrayList<String>();
  public static JComboBox jarBox = new JComboBox();
  private LauncherFrame launcherFrame;
  private boolean outdated = false;

  private boolean playOfflineAsDemo = false;

  public LoginForm(final LauncherFrame paramLauncherFrame) {
    launcherFrame = paramLauncherFrame;

    BorderLayout localBorderLayout = new BorderLayout();
    setLayout(localBorderLayout);

    add(buildMainLoginPanel(), "Center");

    readUsername();

    ActionListener local2 = new ActionListener() {
      @Override
	public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
        doLogin();
      }
    };
    userName.addActionListener(local2);
    password.addActionListener(local2);

    retryButton.addActionListener(new ActionListener() {
      @Override
	public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
        errorLabel.setText("");
        removeAll();
        add(LoginForm.this.buildMainLoginPanel(), "Center");
        validate();
      }
    });
    offlineButton.addActionListener(new ActionListener() {
      @Override
	public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
        paramLauncherFrame.playCached(userName.getText(), playOfflineAsDemo);
      }
    });
    launchButton.addActionListener(local2);
  }

  public void doLogin() {
    setLoggingIn();
    new Thread()
    {
      @Override
	public void run() {
        try {
          launcherFrame.login(userName.getText(), new String(password.getPassword()));
        } catch (Exception localException) {
          setError(localException.toString());
        }
      }
    }
    .start();
  }

  private void readUsername()
  {
    try
    {
      File localFile = new File(Util.getWorkingDirectory(), "lastlogin");

      Cipher localCipher = getCipher(2, "passwordfile");
      DataInputStream localDataInputStream;
      if (localCipher != null)
        localDataInputStream = new DataInputStream(new CipherInputStream(new FileInputStream(localFile), localCipher));
      else {
        localDataInputStream = new DataInputStream(new FileInputStream(localFile));
      }
      userName.setText(localDataInputStream.readUTF());
      password.setText(localDataInputStream.readUTF());
      rememberBox.setSelected(password.getPassword().length > 0);
      localDataInputStream.close();
    } catch (Exception localException) {
      localException.printStackTrace();
    }
  }

  private void writeUsername() {
    try {
      File localFile = new File(Util.getWorkingDirectory(), "lastlogin");

      Cipher localCipher = getCipher(1, "passwordfile");
      DataOutputStream localDataOutputStream;
      if (localCipher != null)
        localDataOutputStream = new DataOutputStream(new CipherOutputStream(new FileOutputStream(localFile), localCipher));
      else {
        localDataOutputStream = new DataOutputStream(new FileOutputStream(localFile));
      }
      localDataOutputStream.writeUTF(userName.getText());
      localDataOutputStream.writeUTF(rememberBox.isSelected() ? new String(password.getPassword()) : "");
      localDataOutputStream.close();
      
      MinecraftPortable.config.set("Settings.lastjar", jarBox.getSelectedItem());
      MinecraftPortable.saveConfig();
    } catch (Exception localException) {
      localException.printStackTrace();
    }
  }

  private Cipher getCipher(int paramInt, String paramString) throws Exception {
    Random localRandom = new Random(43287234L);
    byte[] arrayOfByte = new byte[8];
    localRandom.nextBytes(arrayOfByte);
    PBEParameterSpec localPBEParameterSpec = new PBEParameterSpec(arrayOfByte, 5);

    SecretKey localSecretKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(paramString.toCharArray()));
    Cipher localCipher = Cipher.getInstance("PBEWithMD5AndDES");
    localCipher.init(paramInt, localSecretKey, localPBEParameterSpec);
    return localCipher;
  }

  private JScrollPane getUpdateNews() {
    if (scrollPane != null) return scrollPane;
    try
    {
      final JTextPane local7 = new JTextPane()
      {
        private static final long serialVersionUID = 1L;
      };
      local7.setEditable(false);
      local7.setMargin(null);
      local7.setBackground(Color.DARK_GRAY);
      local7.setContentType("text/html");
      local7.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>Loading update news..</h1></center></font></body></html>");
      local7.addHyperlinkListener(EXTERNAL_HYPERLINK_LISTENER);

      new Thread()
      {
        @Override
		public void run() {
          try {
            local7.setPage(new URL("http://mcupdate.tumblr.com/"));
          } catch (Exception localException) {
            localException.printStackTrace();
            local7.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>Failed to update news</h1><br>" + localException.toString() + "</center></font></body></html>");
          }
        }
      }
      .start();

      scrollPane = new JScrollPane(local7);
      scrollPane.setBorder(null);

      scrollPane.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));
    } catch (Exception localException) {
      localException.printStackTrace();
    }

    return scrollPane;
  }
  
	public static boolean arraySearch(String[] haystack, String needle) {
		for (String element : haystack) {
			if (element.equals(needle)) {
				return true;
			}
		}
		return false;
	}

  private JPanel buildMainLoginPanel() {
    TransparentPanel localTransparentPanel = new TransparentPanel(new BorderLayout());
    localTransparentPanel.add(getUpdateNews(), "Center");

    TexturedPanel localTexturedPanel = new TexturedPanel();
    localTexturedPanel.setLayout(new BorderLayout());
    localTexturedPanel.add(new LogoPanel(), "West");
    localTexturedPanel.add(new TransparentPanel(), "Center");
    localTexturedPanel.add(center(buildLoginPanel()), "East");
    localTexturedPanel.setPreferredSize(new Dimension(100, 130));

    localTransparentPanel.add(localTexturedPanel, "South");
    return localTransparentPanel;
  }

  private JPanel buildLoginPanel() {
    TransparentPanel localTransparentPanel1 = new TransparentPanel();
    localTransparentPanel1.setInsets(4, 0, 4, 0);

    BorderLayout localBorderLayout = new BorderLayout();
    localBorderLayout.setHgap(0);
    localBorderLayout.setVgap(8);
    localTransparentPanel1.setLayout(localBorderLayout);

    GridLayout localGridLayout1 = new GridLayout(0, 1);
    localGridLayout1.setVgap(2);
    GridLayout localGridLayout2 = new GridLayout(0, 1);
    localGridLayout2.setVgap(2);
    GridLayout localGridLayout3 = new GridLayout(0, 1);
    localGridLayout3.setVgap(2);

    TransparentPanel localTransparentPanel2 = new TransparentPanel(localGridLayout1);
    TransparentPanel localTransparentPanel3 = new TransparentPanel(localGridLayout2);

    localTransparentPanel2.add(new TransparentLabel("Username:", 4));
    localTransparentPanel2.add(new TransparentLabel("Password:", 4));
    localTransparentPanel2.add(new TransparentLabel("JAR: ", 4));
    localTransparentPanel2.add(new TransparentLabel("", 4));
    
    File aDirectory = new File(MinecraftPortable.clientDir, "bin");
  	String[] filesInDir = aDirectory.list();
	if (filesInDir != null && filesInDir.length > 2) {
		Arrays.sort(filesInDir);
		// have everything i need, just print it now
		for (int i = 0; i < filesInDir.length; i++) {
			if (filesInDir[i].contains(".jar")
					&& !arraySearch(MinecraftPortable.ignore, filesInDir[i])) {
			jarList.add(filesInDir[i]);
				//Main.log("Found JAR: %s", filesInDir[i]);
			}
		}
	}
	jarBox.setEditable(true);
	for (String s : jarList) {
		jarBox.addItem(s);
	}
	if (jarList.contains(MinecraftPortable.config.getString("Settings.lastjar", "minecraft.jar"))) {
		jarBox.setSelectedItem(MinecraftPortable.config.getString("Settings.lastjar", "minecraft.jar"));
	} else {
		jarBox.setSelectedItem("minecraft.jar");
	}

    localTransparentPanel3.add(userName);
    localTransparentPanel3.add(password);
    localTransparentPanel3.add(jarBox);
    localTransparentPanel3.add(rememberBox);

    localTransparentPanel1.add(localTransparentPanel2, "West");
    localTransparentPanel1.add(localTransparentPanel3, "Center");

    TransparentPanel localTransparentPanel4 = new TransparentPanel(new BorderLayout());

    TransparentPanel localTransparentPanel5 = new TransparentPanel(localGridLayout3);
    localTransparentPanel2.setInsets(0, 0, 0, 4);
    localTransparentPanel5.setInsets(0, 10, 0, 10);

    localTransparentPanel5.add(launchButton);
    try
    {
      Object localObject;
      if (outdated) {
        localObject = getUpdateLink();
        localTransparentPanel5.add((Component)localObject);
      } else {
        localObject = new TransparentLabel("Need account?")
        {
          private static final long serialVersionUID = 0L;

          @Override
		public void paint(Graphics paramAnonymousGraphics) {
            super.paint(paramAnonymousGraphics);

            int i = 0;
            int j = 0;

            FontMetrics localFontMetrics = paramAnonymousGraphics.getFontMetrics();
            int k = localFontMetrics.stringWidth(getText());
            int m = localFontMetrics.getHeight();

            if (getAlignmentX() == 2.0F) i = 0;
            else if (getAlignmentX() == 0.0F) i = getBounds().width / 2 - k / 2;
            else if (getAlignmentX() == 4.0F) i = getBounds().width - k;
            j = getBounds().height / 2 + m / 2 - 1;

            paramAnonymousGraphics.drawLine(i + 2, j, i + k - 2, j);
          }

          @Override
		public void update(Graphics paramAnonymousGraphics)
          {
            paint(paramAnonymousGraphics);
          }
        };
        ((TransparentLabel)localObject).setCursor(Cursor.getPredefinedCursor(12));
        ((TransparentLabel)localObject).addMouseListener(new MouseAdapter()
        {
          @Override
		public void mousePressed(MouseEvent paramAnonymousMouseEvent) {
            try {
              Util.openLink(new URL("http://www.minecraft.net/register.jsp").toURI());
            } catch (Exception localException) {
              localException.printStackTrace();
            }
          }
        });
        ((TransparentLabel)localObject).setForeground(LINK_COLOR);
        localTransparentPanel5.add((Component)localObject);
      }
    }
    catch (Error localError) {
    }
    localTransparentPanel4.add(localTransparentPanel5, "Center");
    localTransparentPanel1.add(localTransparentPanel4, "East");

    errorLabel.setFont(new Font(null, 2, 16));
    errorLabel.setForeground(new Color(16728128));
    errorLabel.setText("");
    localTransparentPanel1.add(errorLabel, "North");

    return localTransparentPanel1;
  }

  private TransparentLabel getUpdateLink() {
    TransparentLabel local11 = new TransparentLabel("You need to update the launcher!")
    {
      private static final long serialVersionUID = 0L;

      @Override
	public void paint(Graphics paramAnonymousGraphics) {
        super.paint(paramAnonymousGraphics);

        int i = 0;
        int j = 0;

        FontMetrics localFontMetrics = paramAnonymousGraphics.getFontMetrics();
        int k = localFontMetrics.stringWidth(getText());
        int m = localFontMetrics.getHeight();

        if (getAlignmentX() == 2.0F) i = 0;
        else if (getAlignmentX() == 0.0F) i = getBounds().width / 2 - k / 2;
        else if (getAlignmentX() == 4.0F) i = getBounds().width - k;
        j = getBounds().height / 2 + m / 2 - 1;

        paramAnonymousGraphics.drawLine(i + 2, j, i + k - 2, j);
      }

      @Override
	public void update(Graphics paramAnonymousGraphics)
      {
        paint(paramAnonymousGraphics);
      }
    };
    local11.setCursor(Cursor.getPredefinedCursor(12));
    local11.addMouseListener(new MouseAdapter()
    {
      @Override
	public void mousePressed(MouseEvent paramAnonymousMouseEvent) {
        try {
          Util.openLink(new URL("http://www.minecraft.net/download.jsp").toURI());
        } catch (Exception localException) {
          localException.printStackTrace();
        }
      }
    });
    local11.setForeground(LINK_COLOR);
    return local11;
  }

  private JPanel buildMainOfflinePanel(boolean paramBoolean) {
    TransparentPanel localTransparentPanel = new TransparentPanel(new BorderLayout());
    localTransparentPanel.add(getUpdateNews(), "Center");

    TexturedPanel localTexturedPanel = new TexturedPanel();
    localTexturedPanel.setLayout(new BorderLayout());
    localTexturedPanel.add(new LogoPanel(), "West");
    localTexturedPanel.add(new TransparentPanel(), "Center");
    localTexturedPanel.add(center(buildOfflinePanel(paramBoolean)), "East");
    localTexturedPanel.setPreferredSize(new Dimension(100, 100));

    localTransparentPanel.add(localTexturedPanel, "South");
    return localTransparentPanel;
  }

  private Component center(Component paramComponent) {
    TransparentPanel localTransparentPanel = new TransparentPanel(new GridBagLayout());
    localTransparentPanel.add(paramComponent);
    return localTransparentPanel;
  }

  private TransparentPanel buildOfflinePanel(boolean paramBoolean) {
    TransparentPanel localTransparentPanel1 = new TransparentPanel();
    localTransparentPanel1.setInsets(0, 0, 0, 20);

    BorderLayout localBorderLayout = new BorderLayout();
    localTransparentPanel1.setLayout(localBorderLayout);

    TransparentPanel localTransparentPanel2 = new TransparentPanel(new BorderLayout());

    GridLayout localGridLayout = new GridLayout(0, 1);
    localGridLayout.setVgap(2);
    TransparentPanel localTransparentPanel3 = new TransparentPanel(localGridLayout);
    localTransparentPanel3.setInsets(0, 8, 0, 0);

    if (paramBoolean)
      offlineButton.setText("Play Demo");
    else {
      offlineButton.setText("Play Offline");
    }

    localTransparentPanel3.add(retryButton);
    localTransparentPanel3.add(offlineButton);

    localTransparentPanel2.add(localTransparentPanel3, "East");

    boolean bool = (launcherFrame.canPlayOffline(userName.getText())) || (paramBoolean);
    offlineButton.setEnabled(bool);
    if (!bool) {
      localTransparentPanel2.add(new TransparentLabel("(Not downloaded)", 4), "South");
    }
    localTransparentPanel1.add(localTransparentPanel2, "Center");

    TransparentPanel localTransparentPanel4 = new TransparentPanel(new GridLayout(0, 1));
    errorLabel.setFont(new Font(null, 2, 16));
    errorLabel.setForeground(new Color(16728128));
    localTransparentPanel4.add(errorLabel);
    if (outdated) {
      TransparentLabel localTransparentLabel = getUpdateLink();
      localTransparentPanel4.add(localTransparentLabel);
    }

    localTransparentPanel2.add(localTransparentPanel4, "Center");

    return localTransparentPanel1;
  }

  public void setError(String paramString) {
    removeAll();
    add(buildMainLoginPanel(), "Center");
    errorLabel.setText(paramString);
    validate();
  }

  public void loginOk() {
    writeUsername();
  }

  public void setLoggingIn() {
    removeAll();
    JPanel localJPanel = new JPanel(new BorderLayout());
    localJPanel.add(getUpdateNews(), "Center");

    TexturedPanel localTexturedPanel = new TexturedPanel();
    localTexturedPanel.setLayout(new BorderLayout());
    localTexturedPanel.add(new LogoPanel(), "West");
    localTexturedPanel.add(new TransparentPanel(), "Center");
    TransparentLabel localTransparentLabel = new TransparentLabel("Logging in...                      ", 0);
    localTransparentLabel.setFont(new Font(null, 1, 16));
    localTexturedPanel.add(center(localTransparentLabel), "East");
    localTexturedPanel.setPreferredSize(new Dimension(100, 100));

    localJPanel.add(localTexturedPanel, "South");

    add(localJPanel, "Center");
    validate();
  }

  public void setNoNetwork(boolean paramBoolean) {
    playOfflineAsDemo = paramBoolean;

    removeAll();
    add(buildMainOfflinePanel(paramBoolean), "Center");
    validate();
  }

  public void setOutdated() {
    outdated = true;
  }
}