package net.simplyrin.bungeerestart;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeerestart.utils.Config;
import net.simplyrin.bungeerestart.utils.ThreadPool;

/**
 * Created by SimplyRin on 2018/11/20.
 *
 * Copyright (c) 2018 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class Main extends Plugin {

	private Configuration config;

	@Override
	public void onEnable() {
		File folder = this.getDataFolder();
		if (!folder.exists()) {
			folder.mkdirs();
		}

		File file = new File(folder, "config.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
			}

			Configuration config = Config.getConfig(file);

			config.set("restart-script", "start.bat");
			config.set("restart-time", 15);
			config.set("restart-alert-times", Arrays.asList(15, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1));
			config.set("restart-alert-message", "&aProxy 再起動まで {time} 秒...");
			config.set("restart-kick-message", "&aProxy 再起動中...。すぐに再接続できます。");

			Config.saveConfig(config, file);
		}

		this.config = Config.getConfig(file);

		this.getProxy().getPluginManager().registerCommand(this, new Command("bungeerestart", null, "brestart") {

			@Override
			public void execute(CommandSender sender, String[] args) {
				if (!sender.hasPermission("bungeerestart")) {
					sender.sendMessage(ChatColor.RED + "You don't have access to this command");
					return;
				}

				ThreadPool.run(() -> {
					int time = config.getInt("restart-time");
					List<Integer> ints = (List<Integer>) config.getList("restart-alert-times");

					while (true) {
						if (ints.contains(time)) {
							String message = getRestartMessage().replace("{time}", String.valueOf(time));

							getProxy().broadcast(message);
						}

						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}

						if (time == 0) {
							break;
						}

						time--;
					}

					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							try {
								Runtime.getRuntime().exec("cmd /c start " + config.getString("restart-script"));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

					getProxy().stop(ChatColor.translateAlternateColorCodes('&', config.getString("restart-kick-message")));
				});
			}

		});
	}

	public String getRestartMessage() {
		return ChatColor.translateAlternateColorCodes('&', this.config.getString("restart-alert-message"));
	}

}
