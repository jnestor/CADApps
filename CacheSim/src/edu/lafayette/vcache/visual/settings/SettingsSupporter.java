package edu.lafayette.vcache.visual.settings;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SettingsSupporter {
	public void settingsChanged(Settings settings);
	public void settingChanged(Setting<?> affectedSetting);
	public List<Settings> getSettingsList();
}
