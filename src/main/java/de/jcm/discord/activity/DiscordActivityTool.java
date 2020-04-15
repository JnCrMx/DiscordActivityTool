package de.jcm.discord.activity;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.Activity;
import org.apache.commons.codec.digest.DigestUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DiscordActivityTool
{
	private JButton setActivityButton;
	private JTextField stateField;
	private JPanel mainPanel;
	private JTextField detailsField;
	private JTextField largeImageField;
	private JTextField largeTextField;
	private JTextField smallImageField;
	private JTextField smallTextField;
	private JTextField partyIdField;
	private JSlider slider1;
	private JSpinner currentSpinner;
	private JSpinner maxSpinner;
	private JTextField matchSecretField;
	private JTextField joinSecretField;
	private JTextField spectateSecretField;
	private JCheckBox partyEnabled;
	private JCheckBox timestampsEnabled;
	private JCheckBox secretsEnabled;
	private JButton changeIdButton;
	private JSpinner timeSpinner;
	private JComboBox timeFieldCombo;
	private JComboBox timeTypeCombo;
	private JLabel timeUnitLabel;
	private JButton clearActivityButton;

	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> callbackFuture;
	private Core discordCore;

	private ChangeListener sliderUpdater;

	public DiscordActivityTool()
	{
		Consumer<Result> resultCallback = result ->
		{
			JOptionPane.showMessageDialog(null, "Result: " + result,
			                              "Operation completed",
			                              result == Result.OK ?
					                              JOptionPane.INFORMATION_MESSAGE :
					                              JOptionPane.ERROR_MESSAGE);
		};

		setActivityButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				try(Activity activity = new Activity())
				{
					activity.setState(stateField.getText());
					activity.setDetails(detailsField.getText());

					if(timestampsEnabled.isSelected())
					{
						long seconds = Long.parseLong(String.valueOf(timeSpinner.getValue()));
						if("relative".equals(timeTypeCombo.getSelectedItem()))
						{
							switch(String.valueOf(timeFieldCombo.getSelectedItem()))
							{
								case "elapsed":
									activity.timestamps().setStart(Instant.now().minusSeconds(seconds));
									break;
								case "remaining":
									activity.timestamps().setEnd(Instant.now().plusSeconds(seconds));
							}
						}
						if("absolute".equals(timeTypeCombo.getSelectedItem()))
						{
							switch(String.valueOf(timeFieldCombo.getSelectedItem()))
							{
								case "start time":
									activity.timestamps().setStart(Instant.ofEpochSecond(seconds));
									break;
								case "end time":
									activity.timestamps().setEnd(Instant.ofEpochSecond(seconds));
							}
						}
					}

					activity.assets().setLargeImage(largeImageField.getText().trim());
					activity.assets().setLargeText(largeTextField.getText().trim());
					activity.assets().setSmallImage(smallImageField.getText().trim());
					activity.assets().setSmallText(smallTextField.getText().trim());

					if(partyEnabled.isSelected())
					{
						activity.party().setID(partyIdField.getText().trim());
						activity.party().size().setCurrentSize((Integer) currentSpinner.getValue());
						activity.party().size().setMaxSize((Integer) maxSpinner.getValue());
					}

					if(secretsEnabled.isSelected())
					{
						activity.secrets().setMatchSecret(matchSecretField.getText().trim());
						activity.secrets().setJoinSecret(joinSecretField.getText().trim());
						activity.secrets().setSpectateSecret(spectateSecretField.getText().trim());
					}

					discordCore.activityManager().updateActivity(activity, resultCallback);
				}
			}
		});

		initCore(698611073133051974L);

		sliderUpdater = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				slider1.setMinimum(1);
				slider1.setMaximum((Integer) maxSpinner.getValue());
				slider1.setValue((Integer) currentSpinner.getValue());
			}
		};

		currentSpinner.addChangeListener(sliderUpdater);
		currentSpinner.setValue(1);
		maxSpinner.addChangeListener(sliderUpdater);
		maxSpinner.setValue(1);

		slider1.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				maxSpinner.removeChangeListener(sliderUpdater);
				currentSpinner.removeChangeListener(sliderUpdater);

				currentSpinner.setValue(slider1.getValue());

				maxSpinner.addChangeListener(sliderUpdater);
				currentSpinner.addChangeListener(sliderUpdater);
			}
		});

		Random random = new Random();
		byte[] bytes = new byte[128];

		random.nextBytes(bytes);
		partyIdField.setText(DigestUtils.md5Hex(bytes));

		random.nextBytes(bytes);
		matchSecretField.setText(DigestUtils.md5Hex(bytes));
		random.nextBytes(bytes);
		joinSecretField.setText(DigestUtils.md5Hex(bytes));
		random.nextBytes(bytes);
		spectateSecretField.setText(DigestUtils.md5Hex(bytes));

		timestampsEnabled.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				timeTypeCombo.setEnabled(timestampsEnabled.isSelected());
				timeSpinner.setEnabled(timestampsEnabled.isSelected());
				timeFieldCombo.setEnabled(timestampsEnabled.isSelected());
			}
		});
		partyEnabled.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				partyIdField.setEnabled(partyEnabled.isSelected());
				currentSpinner.setEnabled(partyEnabled.isSelected());
				maxSpinner.setEnabled(partyEnabled.isSelected());
				slider1.setEnabled(partyEnabled.isSelected());
			}
		});
		secretsEnabled.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				matchSecretField.setEnabled(secretsEnabled.isSelected());
				joinSecretField.setEnabled(secretsEnabled.isSelected());
				spectateSecretField.setEnabled(secretsEnabled.isSelected());
			}
		});

		// fire events to reach initial state
		timestampsEnabled.getActionListeners()[0].actionPerformed(null);
		partyEnabled.getActionListeners()[0].actionPerformed(null);
		secretsEnabled.getActionListeners()[0].actionPerformed(null);

		changeIdButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				String input = JOptionPane.showInputDialog(null, "Enter new Client ID:",
				                                           "Change Clinet ID", JOptionPane.QUESTION_MESSAGE);
				try
				{
					long clientId = Long.parseLong(input);
					shutdownCore();
					initCore(clientId);
				}
				catch(NumberFormatException e)
				{
					JOptionPane.showMessageDialog(null, "Invalid ID!",
					                              "Input Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		timeTypeCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				switch(String.valueOf(timeTypeCombo.getSelectedItem()))
				{
					case "relative":
						timeUnitLabel.setText("seconds");
						timeFieldCombo.setModel(new DefaultComboBoxModel<String>(new String[]{"elapsed", "remaining"}));
						timeSpinner.setValue(0);
						break;
					case "absolute":
						timeUnitLabel.setText("epoch seconds");
						timeFieldCombo
								.setModel(new DefaultComboBoxModel<String>(new String[]{"start time", "end time"}));
						timeSpinner.setValue(Instant.now().getEpochSecond());
						break;
				}
			}
		});
		clearActivityButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				discordCore.activityManager().clearActivity(resultCallback);
			}
		});
	}

	public void shutdownCore()
	{
		callbackFuture.cancel(false);
		discordCore.close();
	}

	private void initCore(long clientId)
	{
		CreateParams createParams = new CreateParams();
		createParams.setClientID(clientId);

		discordCore = new Core(createParams);
		callbackFuture = executor.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					discordCore.runCallbacks();
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
		}, 0, 16, TimeUnit.MILLISECONDS);
	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$()
	{
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayoutManager(2, 3, new Insets(5, 5, 5, 5), -1, -1));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
		mainPanel
				.add(panel1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("State");
		panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		stateField = new JTextField();
		panel1.add(stateField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Details");
		panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		detailsField = new JTextField();
		panel1.add(detailsField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Timestamps");
		panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		timeUnitLabel = new JLabel();
		timeUnitLabel.setText("seconds");
		panel2.add(timeUnitLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		timestampsEnabled = new JCheckBox();
		timestampsEnabled.setText("Enabled");
		panel2.add(timestampsEnabled, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		timeSpinner = new JSpinner();
		panel2.add(timeSpinner, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		timeFieldCombo = new JComboBox();
		final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
		defaultComboBoxModel1.addElement("elapsed");
		defaultComboBoxModel1.addElement("remaining");
		timeFieldCombo.setModel(defaultComboBoxModel1);
		panel2.add(timeFieldCombo, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		timeTypeCombo = new JComboBox();
		final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
		defaultComboBoxModel2.addElement("relative");
		defaultComboBoxModel2.addElement("absolute");
		timeTypeCombo.setModel(defaultComboBoxModel2);
		panel2.add(timeTypeCombo, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("Assets");
		panel1.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label5 = new JLabel();
		label5.setText("Large Image");
		panel3.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		largeImageField = new JTextField();
		panel3.add(largeImageField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		largeTextField = new JTextField();
		panel3.add(largeTextField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label6 = new JLabel();
		label6.setText("Large Text");
		panel3.add(label6, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label7 = new JLabel();
		label7.setText("Small Image");
		panel3.add(label7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		smallImageField = new JTextField();
		panel3.add(smallImageField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label8 = new JLabel();
		label8.setText("Small Text");
		panel3.add(label8, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		smallTextField = new JTextField();
		panel3.add(smallTextField, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label9 = new JLabel();
		label9.setText("Party");
		panel1.add(label9, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel4, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label10 = new JLabel();
		label10.setText("ID");
		panel4.add(label10, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		partyIdField = new JTextField();
		panel4.add(partyIdField, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label11 = new JLabel();
		label11.setText("Size");
		panel4.add(label11, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		slider1 = new JSlider();
		panel4.add(slider1, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		currentSpinner = new JSpinner();
		currentSpinner.setToolTipText("Current party size.");
		panel4.add(currentSpinner, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		maxSpinner = new JSpinner();
		maxSpinner.setToolTipText("Maximal party size.");
		panel4.add(maxSpinner, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		partyEnabled = new JCheckBox();
		partyEnabled.setText("Enabled");
		panel4.add(partyEnabled, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label12 = new JLabel();
		label12.setText("Secrets");
		panel1.add(label12, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel5 = new JPanel();
		panel5.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel5, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label13 = new JLabel();
		label13.setText("Match Secret");
		panel5.add(label13, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		matchSecretField = new JTextField();
		panel5.add(matchSecretField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label14 = new JLabel();
		label14.setText("Join Secret");
		panel5.add(label14, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		joinSecretField = new JTextField();
		panel5.add(joinSecretField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label15 = new JLabel();
		label15.setText("Spectate Secret");
		panel5.add(label15, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		spectateSecretField = new JTextField();
		panel5.add(spectateSecretField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		secretsEnabled = new JCheckBox();
		secretsEnabled.setText("Enabled");
		panel5.add(secretsEnabled, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		clearActivityButton = new JButton();
		clearActivityButton.setText("Clear Activity");
		mainPanel
				.add(clearActivityButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		changeIdButton = new JButton();
		changeIdButton.setText("Change Client ID");
		mainPanel
				.add(changeIdButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		setActivityButton = new JButton();
		setActivityButton.setText("Set Activity!");
		mainPanel
				.add(setActivityButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$()
	{
		return mainPanel;
	}

}
