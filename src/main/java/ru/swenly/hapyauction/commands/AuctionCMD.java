package ru.swenly.hapyauction.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.swenly.hapyauction.HapyAuction;
import ru.swenly.hapyauction.config.ConfigSystem;
import ru.swenly.hapyauction.gui.AuctionItemsGUI;
import ru.swenly.hapyauction.utils.AuctionUtils;

public class AuctionCMD implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            AuctionItemsGUI.openInventory(player, true);
            return true;
        }
        else {
            if (args[0].equals("about")) {
                sender.sendMessage(aboutCMD());
                return true;
            }
            else if (args[0].equals("sell")) {
                if (args.length == 1) {
                    sender.sendMessage("§cОшибка! Вы не указали цену предмета!");
                    return true;
                }
                else if (args.length > 2) {
                    sender.sendMessage("§cОшибка! Слишком много аргументов!");
                    return true;
                }
                else {
                    double price = 0.0;

                    try {
                        price = Double.parseDouble(args[1]);
                    } catch (NumberFormatException exception) {
                        sender.sendMessage("§cОшибка! Укажите цену в виде числа!");
                        return true;
                    }

                    try {
                        String reason = AuctionUtils.sellItem(player, price);

                        if (reason.equals("air")) {
                            sender.sendMessage("§cОшибка! Вы не можете продать воздух!");
                        }

                        return true;
                    } catch (Exception ignored) {
                        return true;
                    }
                }
            }
            else if (args[0].equals("searcher")) {

                return true;
            }
            else if (args[0].equals("reload") && sender.hasPermission("hapyauction.reload")) {
                ConfigSystem.reloadConfig();
                player.sendMessage("§aУспешно! §eВы перезагрузили конфиг плагина!");

                return true;
            }
            else {
                sender.sendMessage(helpCMD());
                return true;
            }
        }
    }

    public String aboutCMD() {
        String about_message = "§eDiscord §8-§7 Swenly#8002\n§eGithub §8-§7 https://github.com/SwenlyYT\n§eYouTube §8-§7 \n§eMy Discord Server §8-§7 https://discord.gg/HM5MkXU33T";
        return about_message;
    }

    public String helpCMD() {
        String help_message = "§7---------- §8[ §eHapyAuction §8] §7----------" + "\n§e/ah §8-§7 Показывает аукцион\n§e/ah sell <price> §8-§7 Продает предмет\n§e/ah about §8-§7 Информация о плагине\n§e/ah help §8-§7 Список команд плагина";
        return help_message;
    }
}
