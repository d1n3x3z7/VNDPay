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

public class EDClient implements PaymentClient {

    private final VNDPay instance;

    public EDClient(VNDPay inst) {
        this.instance = inst;
    }

    @Override
    public String create(String username, int id, int count, String coupon) throws Exception {
        StringBuilder burl = new StringBuilder("https://easydonate.ru/api/v3/shop/payment/create?customer=");
        if (username == null | id <= 0) {
            throw new Exception();
        }
        if (count <= 0) {
            count = 1;
        }
        burl.append(username).append("&server_id=").append(instance.server_id).append("&products=").append("{\"").append(id).append("\":").append(count).append("}");
        if (coupon!=null) {
            burl.append("&coupon=").append(coupon);
        }
        burl.append("&success_url=").append(instance.success_url);

        return getUrl(httpRequest(burl.toString()));
    }

    public String httpRequest(String link) throws IOException, InterruptedException, URISyntaxException {

        HttpClient client = HttpClient.newHttpClient();

        URL url = new URL(link);

        URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Shop-Key", instance.secret_key)
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public String getUrl(String json) {
        JSONObject jo = new JSONObject(json);
        JSONObject ja = jo.getJSONObject("response");
        return ja.getString("url");
    }
}
