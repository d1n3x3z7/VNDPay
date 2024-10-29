package me.d1n3x3z7.vndpayment;

import me.d1n3x3z7.vndpayment.api.PaymentClient;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PaymentCommand implements CommandExecutor, TabCompleter {

    private final VNDPay instance;

    public PaymentCommand(VNDPay inst) {
        this.instance = inst;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.hasPermission("vnd.pay")) {
            if (strings.length == 0 | strings.length > 5) {
                return false;
            }
            if (strings[0].equals("reload")) {
                instance.configUpdater();
                commandSender.sendMessage("[VNDPay:Command:Config] Config updated successfully!");
                return true;
            }

            String nickname = null;
            String product = null;
            String count = null;
            String coupon = null;
            String mode = null;

            AtomicReference<String> url = new AtomicReference<>("EMPTY");

            for (String string:strings) {
                if (string.startsWith("nickname:") & nickname == null) {
                    try {
                        nickname = string.split(":")[1];
                    } catch (Exception e) {
                        return false;
                    }
                } else if (string.startsWith("count:") & count == null) {
                    try {
                        count = string.split(":")[1];
                    } catch (Exception e) {
                        return false;
                    }
                } else if (string.startsWith("coupon:") & coupon == null) {
                    try {
                        coupon = string.split(":")[1];
                    } catch (Exception e) {
                        return false;
                    }
                } else if (string.startsWith("mode:") & mode == null) {
                    try {
                        mode = string.split(":")[1];
                    } catch (Exception e) {
                        return false;
                    }
                } else if (string.startsWith("product:") & product == null) {
                    product = string.split(":")[1];
                    if (!instance.products.contains(product)) {
                        commandSender.sendMessage("[VNDPay:Command] Non-valid product: " + product);
                        return false;
                    }
                } else {
                    return false;
                }
            }

            if (nickname == null & commandSender instanceof Player) {
                nickname = commandSender.getName();
            } else if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("[VNDPay:Command] Payment can't be generated from RCON.");
                return false;
            }

            if (count == null) {
                count = "1";
            }

            if (product == null) {
                commandSender.sendMessage("[VNDPay:Command] There is no required parameter: product");
                return false;
            }

            if (!instance.paymentClients.containsKey(mode)) {
                commandSender.sendMessage("[VNDPay:Command] There is no \"" + mode + "\" mode.");
                return false;
            }

            PaymentClient client = instance.paymentClients.get(mode);
            // n2 the shittiest thing
            try {
                String finalNickname = nickname;
                String finalProduct = product;
                String finalCount = count;
                String finalCoupon = coupon;

                Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                    try {
                        url.set(client.create(finalNickname, instance.products_ids.get(finalProduct), Integer.parseInt(finalCount), finalCoupon));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    Bukkit.getScheduler().runTask(instance, () ->
                            commandSender.sendMessage(formatter(finalNickname, url.get(), finalCount, instance.products_names.get(finalProduct), finalCoupon)));
                });

                return true;
            } catch (Exception e) {
                instance.getLogger().warning("[VNDPay:Command:"+ instance.mode +"] Payment link can't be generated!");
                instance.getLogger().warning(String.valueOf(e));
                return false;
            }
        }
        return false;
    }

    public String formatter(String nickname, String url, String count, String product, String coupon) {
        return VNDPay.TEXT.PRINT_FORMAT
                .replaceAll("<username>", nickname)
                .replaceAll("<payment>", url)
                .replaceAll("<product>", product)
                .replaceAll("<count>", count)
                .replaceAll("<coupon>", coupon);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.hasPermission("vnd.pay")) {
            List<String> list = Arrays.asList("nickname:", "count:", "product:", "coupon:", "mode:");
            return list.stream().filter(line -> line.startsWith(strings[0])).collect(Collectors.toList());
        }
        return null;
    }
}
