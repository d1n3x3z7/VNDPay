package me.d1n3x3z7.vndpayment.api;

import me.d1n3x3z7.vndpayment.VNDPay;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TMCClient implements PaymentClient {

    private final VNDPay instance;

    public TMCClient(VNDPay inst) {
        this.instance = inst;
    }

    @Override
    public String create(String username, int id, int count, String coupon) throws Exception {
        StringBuilder burl = new StringBuilder("https://api.trademc.org/shop.buyItems?items=");
        if (username == null | id <= 0) {
            throw new Exception();
        }
        if (count <= 0) {
            count = 1;
        }
        burl.append(id).append(':').append(count).append('&').append("buyer=").append(username);
        if (coupon!=null) {
            burl.append("&coupon=").append(coupon);
        }
        burl.append("&v=3");
        String answer = httpRequest(burl.toString());
        int cart_id = getCart(answer);
        return createLink(cart_id);
    }

    public String createLink(int id) {
        return "https://pay.trademc.org/?cart_id=" + id + "&success_url=" + instance.success_url + "&fail_url=" + instance.fail_url;
    }

    public int getCart(String json) {
        JSONObject jo = new JSONObject(json);
        JSONObject ja = jo.getJSONObject("response");
        return ja.getInt("cart_id");
    }

    public String httpRequest(String link) throws IOException, InterruptedException, URISyntaxException {

        HttpClient client = HttpClient.newHttpClient();

        URL url = new URL(link);

        URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}
