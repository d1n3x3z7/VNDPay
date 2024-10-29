package me.d1n3x3z7.vndpayment;

import me.d1n3x3z7.vndpayment.api.EDClient;
import me.d1n3x3z7.vndpayment.api.PaymentClient;
import me.d1n3x3z7.vndpayment.api.TMCClient;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public final class VNDPay extends JavaPlugin {

    // Plugin instance
    public VNDPay instance;
    // Command instance
    public PluginCommand paymentCommand;


    // Payment classes instances
    public HashMap<String, PaymentClient> paymentClients = new HashMap<>();
    // Product:Id list
    public HashMap<String, Integer> products_ids = new HashMap<>();
    // Product:Name list
    public HashMap<String, String> products_names = new HashMap<>();


    // Products list
    public List<String> products = new ArrayList<>();


    // SecretKey (EasyDonate ONLY)
    public String secret_key;
    // ServerID (EasyDonate ONLY)
    public int server_id;

    // Payment mode
    public String mode;

    // SuccessPaymentURL
    public String success_url;
    // FailedPaymentURL
    public String fail_url;

    // text vars
    static class TEXT {
        public static String PRINT_FORMAT;
        public static String version = "1.0";
    }

    public void configUpdater() {

        reloadConfig();

        FileConfiguration fc = getConfig();

        getVars(fc);

        putPaymentInstance(mode);

        paymentCommand.setExecutor(new PaymentCommand(instance));
    }

    private void putPaymentInstance(String mode) {
        switch (mode) {
            case "all":
                paymentClients.put("trademc", new TMCClient(instance));
                paymentClients.put("easydonate", new EDClient(instance));
                break;
            case "trademc":
                paymentClients.put("trademc", new TMCClient(instance));
                break;
            case "easydonate":
                paymentClients.put("easydonate", new EDClient(instance));
                break;
            default:
                this.getServer().getLogger().warning("[VNDPay:Configuration] Failed to initialize \"mode\", reload config!");
                paymentCommand.setExecutor(null);
        }
    }

    private void getVars(FileConfiguration fc) {
        secret_key = null;
        server_id = 0;
        mode = null;
        success_url = null;
        fail_url = null;

        try {
            paymentClients.clear();
            products_ids.clear();
            products_names.clear();
            products.clear();
        } catch (Exception e) {
            this.getLogger().warning("[VNDPay:Configuration] Reset unsuccessful! Skipping...");
        }

        TEXT.PRINT_FORMAT = null;

        try {

            mode = fc.getString("mode").toLowerCase();
            TEXT.PRINT_FORMAT = fc.getString("message-format");
            secret_key = fc.getString("secret-key");
            server_id = fc.getInt("server-id");
            success_url = fc.getString("success-url");
            fail_url = fc.getString("fail-url");
            products = fc.getStringList("allowed-products");

            for (String productName : products) {
                int id = fc.getInt("products." + productName + ".id");
                String name = fc.getString("products." + productName + ".name");
                products_ids.put(productName, id);
                products_names.put(productName, name);
            }

        } catch (Exception exception) {
            this.getServer().getLogger().warning("[VNDPay:Configuration] VNDPay not configured correctly!");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void versionHandler() throws IOException {
        Object content = new URL("https://api.github.com/repos/d1n3x3z7/FSIP_plugin/releases/latest")
                .openConnection()
                .getContent();

        JSONObject jo = new JSONObject(content);
        if (!Objects.equals(jo.getString("tag_name"), TEXT.version)) {
            this.getServer().getLogger().info("[VNDPay:Update] New version available! Download it on GitHub.");
        }
    }

    @Override
    public void onEnable() {

        instance = this;
        saveDefaultConfig();
        try {
            paymentCommand = getServer().getPluginCommand("vndpay");
            configUpdater();
            if (products.isEmpty()) {
                throw new Exception();
            }
            versionHandler();
        } catch (Exception e) {
            this.getServer().getLogger().warning("[VNDPay:Plugin] Config template saved. Set your settings before next start or config reload.");
            return;
        }

        this.getServer().getLogger().info("[VNDPay:Plugin] Command initialized. Usage: /vndpay\n");
    }

    @Override
    public void onDisable() {
        this.getServer().getLogger().warning("[VNDPay:Plugin] Turned off.");
    }
}