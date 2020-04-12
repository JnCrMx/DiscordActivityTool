package de.jcm.discord.activity;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Launcher
{
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}

		DiscordActivityTool discordActivityTool = new DiscordActivityTool();

		JFrame frame = new JFrame();
		frame.setContentPane(discordActivityTool.$$$getRootComponent$$$());
		frame.pack();
		frame.setTitle("Discord Activity Tool");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				discordActivityTool.shutdownCore();
			}
		});
	}
}
