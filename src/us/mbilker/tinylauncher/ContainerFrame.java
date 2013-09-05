package us.mbilker.tinylauncher;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ContainerFrame extends Frame {
	
	private static final long serialVersionUID = 8283L;
	private Applet container;
	
	public ContainerFrame(String title) throws HeadlessException {
		super(title);
		
		addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}
		});
		
		setLayout(new BorderLayout());
	}
	
	public Applet getContainerApplet() {
		return container;
	}
	
	public void setContainerApplet(ContainerApplet applet) {
		this.container = applet;
		add("Center", container);
	}

}
